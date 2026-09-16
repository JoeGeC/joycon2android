# DSU motion server

How motion reaches emulators, and why the emulator bindings are shaped the way they are.

## The server

`DsuServer` implements the [cemuhook protocol](https://v1993.github.io/cemuhook-protocol/) over UDP
on port 26760.

- **Bound to IPv4 `127.0.0.1`.** `getLoopbackAddress()` resolves to IPv6 `::1` on Android, and a
  socket there never sees the `127.0.0.1` datagrams emulators send.
- **Pad batches ride a buffered channel**, not a `StateFlow` — conflation would drop motion samples.

| Class | Job |
|---|---|
| `DsuPacketEncoder` | 100-byte pad packets and version/port-info replies, into a reused buffer |
| `DsuSlots` | players → the four slots, including pairs' second hands |
| `DsuClientRegistry` | sends each slot only to that slot's subscribers |
| `GyroCalibrator` | learns and subtracts each controller's gyro bias |
| `MotionConverter` | raw IMU → cemuhook's DS4 frame |
| `SidewaysMotion` | turns a lone Joy-Con's IMU into its sideways grip |
| `DsuMotionPolicy` (`:app`) | applies the motion settings only while DSU runs |

Routing matters: DSU clients, Dolphin included, overwrite their pad state with every packet without
checking its slot, so server-side routing is what keeps players independent.

## Slots

A packet carries one accelerometer and gyroscope. Player N streams on slot N−1, so only P1–P4 are
served. A player holding two Joy-Cons streams its **right** Joy-Con there, and its **left** on the
highest free slot, which is how Dolphin's Nunchuk and Eden's second motion input read that hand.
Players always win their own slot; pairs take what's left, so four players leave no second hands.

## Motion frame

Scale factors are the Switch 1 values, verified on Joy-Con 2: accel 4096 LSB per g, gyro
0.061 °/s per LSB. The axis mapping was measured against Dolphin's Wii pointer — the
`MotionConverter` KDoc records the frames and signs, and [tools/README.md](../tools/README.md) the
calibration workflow.

**Gyro bias.** Joy-Con 2 gyros idle with a constant offset (+0.2 °/s yaw, +0.9 °/s roll observed),
which clients integrate into pointer drift. Whenever a controller stays within ~2.4 °/s for ~2 s,
`GyroCalibrator` adopts the window mean as its bias, as the Switch does.

### Sideways Joy-Cons

A lone Joy-Con's buttons and stick already arrive rotated into its sideways grip
([virtual-gamepad.md](virtual-gamepad.md#sidewaysmapper)), and Eden presents it as a Pro
Controller, so its motion is turned 90° about the button face to match. Without it, tilting read as
if the Joy-Con's nose pointed at the screen.

- **The direction was measured**, in Eden's Mario Kart 8, and is the *opposite* of the stick's turn —
  the IMU axes don't line up with the stick's.
- **A pair's second hand isn't turned**, even though it streams alone on its slot (`DsuStream.heldSideways`).
- **Dolphin maps it back.** Its emulated Wii Remote is the Joy-Con's own body, so
  `DolphinWiimoteConfig` swaps a lone Joy-Con's IMU inputs back (table in the
  [README](../README.md#manual-setup)).
- **Don't enable Dolphin's "Sideways Wii Remote"** with that mapping. It rotates IMU input by 90°
  itself (`Wiimote::GetOrientation`), so it would turn motion twice.

## Report rate

The Joy-Con reports once per BLE connection interval. Android's balanced priority settled on 30 ms
(~33 Hz) on an AYN Thor, which reads as stutter at 60 fps. **Faster motion updates** requests
`CONNECTION_PRIORITY_HIGH` while DSU runs — 15 ms (~67 Hz) on the same Thor — at a battery cost on
both ends.

## Eden reads the device's own motion

Eden's Android build feeds the device's gyro and accelerometer into Player 1 on top of any mapped
motion, so readings alternate with the Joy-Con's and aiming stutters (and follows the handheld).
Moving the pad to another player doesn't escape it: games that open the controller applet
(Splatoon 2) put Player 1 back as a Pro Controller.

**Ignore this device's motion in Eden** (on by default) runs
`cmd sensorservice set-uid-state <eden package> idle` through Shizuku, which withholds continuous
sensors from Eden. The override lives in `system_server` until reset or reboot, so it is lifted when
DSU stops, and again at launch in case the app was killed.

## Dolphin Wii Remote mapping

What `DolphinWiimoteConfig` writes, and why:

- **Swing carries thrusts.** Dolphin moves the emulated remote only through Swing — the IMU path is
  rotation alone — so games that read a push as distance to the sensor bar (Wii Play Billiards'
  cue strength) see nothing from accel and gyro. A pair thrusts along `Accel Forward`; a sideways
  Joy-Con out through its face, `Accel Up`.
- **The swing input is signed and high-passed.** Pairing each input with its opposite makes it
  signed (Dolphin clamps a single input at zero). An accelerometer can't tell gravity from
  acceleration, so a tilted grip parks up to 1 g on the axis; `smooth()` is a slew limiter, so
  subtracting it cancels a held tilt within ~0.3 s while an ~80 ms thrust outruns it.
- **Range 7%, dead zone 20%.** Inputs arrive at 9.8 per g, so full range saturates on a twitch; 7%
  puts a full lunge at ~1.5 g. The dead zone keeps a tilted grip's leak from nudging the remote.
- **The Nunchuk reads another pad.** Dolphin splits a control on its last colon, so
  `DSUClient/<slot>/Joycon2:Accel Up` reads the second hand's slot. A real Nunchuk has no gyro.
- **Recenter** is R1 (L1 on a lone left Joy-Con). Gyro pointing drifts, and pressing it while aiming
  at the screen centre is what summons the pointer.

## MotionPlus tutorial replays

A MotionPlus game replaying its tutorial video every boot isn't a mapping problem. The game records
"video seen" as `MPLS.MOVIE` in `Wii/shared2/sys/SYSCONF`, but `SysConf::~SysConf` writes back the
entry list Dolphin loaded at launch, dropping what the game appended. Set the flag with Dolphin
closed so the next launch loads it.
