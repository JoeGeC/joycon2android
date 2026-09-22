#!/usr/bin/env python3
"""Works out what a flick actually looks like on the wire, so the trick threshold can be set
from a hand rather than from an assumption.

Feed it a capture from dsu_client with the motion interval set to 0 (every packet):

    adb shell /data/local/tmp/dsu_client 127.0.0.1 20 0 > flick.log
    tools/flick_stats.py flick.log

Capture twice — once flicking as you would to trick, once steering as hard as you would
race — and compare. Dolphin sees `rate`, the sum of the six one-way gyro inputs, which is
|pitch| + |yaw| + |roll| in rad/s. DolphinWiimoteConfig subtracts a slew limiter from it and
fires a trick when what is left passes half of FLICK_RADIANS, so `residual` below is the
number that decides whether a flick lands.
"""
import argparse
import math
import re
import statistics
import sys

LINE = re.compile(
    r"\[\s*([\d.]+)\] slot=(\d) accel=\([^)]*\)g gyro\(pitch,yaw,roll\)=\(([^)]*)\)dps"
)


def samples(path, slot):
    """(timestamp, rate, axes) per packet — rate is what Dolphin sums from the six one-way inputs,
    axes keeps the signed pitch/yaw/roll so a gesture's direction can be read back."""
    for line in open(path):
        found = LINE.match(line.strip())
        if not found or int(found.group(2)) != slot:
            continue
        axes = tuple(math.radians(float(v)) for v in found.group(3).split(","))
        yield float(found.group(1)), sum(abs(a) for a in axes), axes


def residuals(rates, settle):
    """What survives Dolphin's `rate - smooth(rate, settle)`: a limiter moving 1/settle per second."""
    state = rates[0][1]
    for previous, (at, rate, axes) in zip(rates, rates[1:]):
        most = (at - previous[0]) / settle
        state += max(-most, min(most, rate - state))
        yield at, rate, rate - state, axes


def peaks(measured, floor, apart):
    """One entry per burst, so a single flick is not counted as several."""
    burst = []
    for sample in measured:
        if sample[2] < floor:
            continue
        if burst and sample[0] - burst[-1][0] > apart:
            yield max(burst, key=lambda it: it[2])
            burst = []
        burst.append(sample)
    if burst:
        yield max(burst, key=lambda it: it[2])


AXES = ("pitch", "yaw", "roll")


def direction(axes):
    """The turn the gesture mostly is, named as Dolphin names its two one-way inputs for that axis."""
    size = max(range(3), key=lambda i: abs(axes[i]))
    ends = {"pitch": ("Pitch Up", "Pitch Down"), "yaw": ("Yaw Left", "Yaw Right"),
            "roll": ("Roll Left", "Roll Right")}[AXES[size]]
    share = abs(axes[size]) / sum(abs(a) for a in axes)
    return f"{ends[0] if axes[size] > 0 else ends[1]:11} {share:.0%} of the turn"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("capture")
    parser.add_argument("--slot", type=int, default=0)
    parser.add_argument("--settle", type=float, default=0.01, help="FLICK_SETTLE_SECONDS")
    parser.add_argument("--floor", type=float, default=2.0, help="rad/s of residual worth reporting")
    parser.add_argument("--apart", type=float, default=0.3, help="seconds between separate events")
    args = parser.parse_args()

    rates = list(samples(args.capture, args.slot))
    if len(rates) < 3:
        sys.exit(f"{args.capture}: no motion lines for slot {args.slot} — run dsu_client with interval 0")

    gaps = [b[0] - a[0] for a, b in zip(rates, rates[1:])]
    span = rates[-1][0] - rates[0][0]
    print(f"{len(rates)} packets over {span:.1f} s")
    print(
        f"interval: mean {statistics.mean(gaps) * 1e3:.1f} ms, "
        f"median {statistics.median(gaps) * 1e3:.1f} ms, worst {max(gaps) * 1e3:.1f} ms"
    )
    print("  a flick lasts 40-80 ms, so anything near that is sampling it once or twice\n")

    measured = list(residuals(rates, args.settle))
    found = list(peaks(measured, args.floor, args.apart))
    if not found:
        sys.exit(f"nothing above {args.floor} rad/s of residual — flick harder, or lower --floor")

    plural = "" if len(found) == 1 else "s"
    print(f"{len(found)} event{plural} (peak residual >= {args.floor}, at least {args.apart}s apart):")
    for at, rate, residual, axes in found:
        signed = " ".join(f"{n} {v:+6.1f}" for n, v in zip(AXES, axes))
        print(f"  t={at:8.2f}  residual {residual:6.1f}  [{signed}]  {direction(axes)}")

    weakest = min(it[2] for it in found)
    print(f"\nweakest event leaves {weakest:.1f} rad/s of residual.")
    print(f"  to catch every one of these, FLICK_RADIANS <= {2 * weakest:.0f}")
    print("  run this over a steering-only capture too: FLICK_RADIANS must stay above twice its")
    print("  largest residual, or racing will fire tricks. No gap between the two means the")
    print("  limiter is the wrong discriminator, not the threshold.")


if __name__ == "__main__":
    main()
