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
  `DolphinWiimoteConfig` turns a lone Joy-Con's IMU inputs back about the button face (table in the
  [README](../README.md#manual-setup)), putting the nose on the shoulder edge the player aims. The
  bodies rotate into their grips opposite ways, so their tables are each other half a turn.
- **A player can play as a sideways Wii Remote** — a switch on their card in the mapping editor,
  seeded by their layout (`MappingLayout.sidewaysRemote`, true only for **Mario Kart**) and
  overridable per player (`SidewaysRemoteRepository`; applying a layout clears the override). A game written for that grip reads gravity against a remote whose nose points
  left, which is where a *left* Joy-Con's L/ZL edge already points — so only a right Joy-Con turns,
  giving up its own body (and with it R/ZR as the nose: aiming moves to the tail) to steer true.
  Both bodies also turn their four D-pad bindings a quarter, since the player's up is a sideways
  remote's right. That is Dolphin's own `dpad_sideways_bitmasks`, applied here so its *Sideways Wii
  Remote* option can stay off — the option would also turn the accelerometer, which we have turned
  already.
- **A sideways body turns a flick into a trick.** Mario Kart Wii tricks off a flick, and a flick of
  something Joy-Con sized is mostly rotation: captured ones peak past 1200 °/s summed while carrying
  barely a g of linear jerk, where jerking a real Wii Wheel throws the whole thing. The game has no
  MotionPlus and reads only the accelerometer, so the flick never reaches it — on hardware it
  wouldn't either. The gyroscope therefore fires it, which hardware could not do: each axis summed
  with its opposite input gives |rate| (Dolphin clamps one of a pair at zero), over `/15` and a half
  dead zone, which fires above 11 rad/s and leaves the sharpest measured steering (6.5) and aiming
  (4.1) a wide berth. `Shake` is a mapping target of its own too, so a pair — which has no sideways
  flick to read — can trick from a button.

  **Dolphin's own `Shake` group is not how it is delivered.** Bound straight to a key in Dolphin's
  config, a full 7 g oscillation of it never once landed a trick (tested 2026-09), so the group is
  not written at all. The accelerometer is the path that demonstrably reaches the game, since
  steering is read from it, and the jerk goes there instead:
  `pulse(deadzone(trigger, 0.2), 0.6) * sin(timer(0.15) * 2π) * 50` added to every
  `IMUAccelerometer` input, the three opposites carrying a half-turn of phase.

  It is a *shake*, not a push: an oscillation held for 0.6 s at about 6.7 Hz, each input of a pair
  swung half a cycle apart so the remote is thrown back and forth rather than leaned on. That shape
  is what landed a trick by hand — shaking a Joy-Con hard for about a second — where a single held
  push did not. Amplitude is not the lever: an emulated Wii Remote saturates around +3.9/−4.9 g
  (`ACCEL_ZERO_G` 0x80, `ACCEL_ONE_G` 0x9A over 8 bits), which the 50 m/s² already passes, so a
  bigger number only clips sooner. `pulse()` gives a flick and a held button the same shake however
  long either lasted.

  **A rate alone cannot tell a flick from a turn**, because steering a lone Joy-Con held as a wheel
  *is* rotation — which is why only single Joy-Cons suffered for it, a pair steering from the
  Nunchuk's stick with its remote hand still. The trigger therefore subtracts a slew limiter,
  `(rate − smooth(rate, 0.01)) / 5`, leaving only what climbs faster than the limiter can follow.

  Both numbers are measured, from a capture of flicks and a capture of hard steering read back by
  [`tools/flick_stats.py`](../tools/README.md#flick-measurement) (2026-09-22, right Joy-Con, 15 ms
  stream):

  | | peak rate | residual after the limiter |
  |---|---|---|
  | flicks (4) | 11–16 rad/s | 5.6, 6.1, 8.3, 9.1 |
  | hard steering (25 s) | 3.6 rad/s | ≤ 1.2 |

  A *slower* limiter is worse, not better: it lifts a flick's residual but lifts steering's faster,
  and the ratio between them — all that matters — falls from 4.7 at 0.01 to 3.8 at 0.02 and 2.0 at
  0.04. `pulse()` fires as its input crosses a half, so the threshold is 2.5 rad/s of residual:
  2.1× above the worst steering and 2.2× below the weakest flick. Erring low is right anyway — a
  trick fired by accident costs nothing, since the game only tricks a kart already airborne, while
  one fired *while steering* costs plenty, the shake landing on the very accelerometer the wheel is
  read from. Every body flicks, a pair included: its remote hand is still while the Nunchuk's
  stick steers. Only a layout that plays as a sideways remote flicks at all, so no other game is
  handed a shake it never asked for when its remote is swung.

  Verified against Dolphin's source (2026-09): `|` is a max and binds looser than `/`;
  `m_shake_state.acceleration` is added to the reported acceleration unconditionally, so binding
  `IMUAccelerometer` does not disable the Shake group — it simply never produced a trick.
- **Pointing and a wheel want the nose half a turn apart on a right Joy-Con**, and no Dolphin option
  bridges them: `GetOrientation()` turns a quarter (Sideways) or a quarter about the left axis
  (Upright), and it reaches only the accelerometer the game reads, never
  `GetTotalTransformation()` and so never the pointer. Hence the choice lives in the layout.
- **Measure the pointer, don't reason about it.** `tools/dsu_client` plus a replay of
  `EmulateIMUCursor` settles in minutes what guessing costs days. Posed captures mislead: asked to
  hold an "aim up", a player produces a different rotation from the one they make while playing —
  compare a captured session against candidate tables by how much cursor travel each yields.
- **`IMUIR/Total Yaw` is widened to 60°.** Dolphin's 25° clamps the cursor after ±12.5° of turn,
  which a hand-held aim overruns constantly; the clamp reads as the pointer sticking.
- **Leave Dolphin's "Sideways Wii Remote" off.** A sideways layout already writes that quarter turn
  itself, into both the motion and the D-pad; the option would apply it twice.

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
