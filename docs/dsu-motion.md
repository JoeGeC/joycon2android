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

Scale factors are the Switch 1 values, verified on Joy-Con 2 (2026-06: at rest gravity reads
exactly −1.00 g): accel ±8 g over 4096 LSB per g, gyro ±2000 °/s at 0.06103 °/s per LSB.
[tools/README.md](../tools/README.md) has the calibration workflow.

**The two frames.** `MotionConverter` turns one into the other, and neither is guessable:

| | x | y | z |
|---|---|---|---|
| Joy-Con (R) raw, measured | the controller's **right** | toward the **tail** | out of the **button face** |
| cemuhook wire | **left** | **down** through the controller | toward the **player** |

Flat at rest reads accel `(0, −1, 0)`; nose up reads accel z = −1 and gyro pitch −; turning right
reads +yaw; rolling right reads +roll.

**The signs are DS4 hardware history, not a consistent right-handed frame**, so derive nothing from
them — verify any change against Dolphin's on-screen pointer, testing fast and slow movements
separately. Its complementary filter makes the *accelerometer* the authority on sustained pitch, so
gyro signs cannot be judged from pointer direction alone; gyro shows up in the fast response, accel
in the settled position.

- **Raw x was documented as "left" until 2026-09**, when a rail-down static pose — SL/SR against the
  table, so gravity points toward the controller's right — read +1 g on the wire's left axis.
  Mirrored. Left/right tilt had been reaching games reversed, and because angular velocity is a
  pseudovector, roll had to mirror with it to stay physically consistent, which is why both flipped
  together.
- **Yaw is the one sign no measurement here pins.** It turns about gravity, so a static pose cannot
  see it and neither can the accel/gyro consistency check. It is kept as the pointer's horizontal
  response reports it. Mirroring x strictly implies mirroring yaw too, so if horizontal pointing
  ever reads backwards, flip yaw rather than re-deriving the frame.
- **Left Joy-Con and Pro are assumed to share the raw frame** — unverified. Recalibrate with
  `tools/dsu_client` if their motion feels rotated.

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

  **Two ways of delivering it do not work, and both were tried.** *Amplifying the accelerometer's
  own transient* does nothing, because an emulated Wii Remote saturates around +3.9/−4.9 g
  (`ACCEL_ZERO_G` 0x80, `ACCEL_ONE_G` 0x9A over 8 bits) and the push already passes that, so a
  bigger number only clips sooner. *Dolphin's `Shake` group* does nothing either: bound straight to
  a key in Dolphin's own config, a full 7 g oscillation of it never once landed a trick (tested
  2026-09) — and not for want of reaching the game, since `m_shake_state.acceleration` is added to
  the reported acceleration unconditionally, whether or not `IMUAccelerometer` is bound. Neither is
  written any more.

  **What is written goes into the accelerometer**, the path that demonstrably reaches the game since
  steering is read from it: `pulse(flick, 0.6) * sin(timer(0.15) * 2π) * 50` added to every
  `IMUAccelerometer` input, the three opposites carrying a half-turn of phase. It is a *shake*, not
  a push — an oscillation held for 0.6 s at about 6.7 Hz, each input of a pair swung half a cycle
  apart so the remote is thrown back and forth rather than leaned on. That shape is what landed a
  trick by hand, shaking a Joy-Con hard for about a second, where a single held push did not.
  `pulse()` gives a flick and a held button the same shake however long either lasted.

  **The flick reads pitch, and only pitch.** Measured over three captures (2026-09-22, right
  Joy-Con, 15 ms stream), a flick is 59–89% pitch on *both* bodies — a lone sideways Joy-Con and a
  pair alike, despite a lone one being rotated into its grip before it reaches the wire — while
  steering a wheel is roll and never exceeds 2.8 rad/s of pitch:

  | | raw pitch, per gesture |
  |---|---|
  | steering, hard, 25 s | ≤ 2.8 rad/s |
  | wheelie flicks | 7.0 – 10.7 |
  | trick flicks | 8.8 – 14.5 |

  Reading pitch alone therefore separates a flick from a turn by axis rather than by rate, which no
  slew limiter could: the wheelie's down-flick is a slow gesture, and a limiter fast enough to
  reject a turn ate all but 0.9 rad/s of it. `pulse()` fires as its input crosses a half, so the
  threshold is 4.5 rad/s — 1.6× above the worst steering, 1.6× below the weakest gesture.

  **Direction matters, because a wheelie is a state.** An up-flick starts one and a down-flick drops
  it, where a trick takes any direction and only one per jump. So the remote is jerked the way it was
  flicked — positive wire pitch is up on both bodies — and the wave is *half* rectified
  (`max(sin(…), 0)`), since a full one would cancel the wheelie it just started four times a second.

  **Each direction locks the other out for 0.4 s**, because every flick rebounds the opposite way
  0.12–0.32 s later, and a rebound is often stronger than a genuine flick elsewhere in the same
  capture — 8.5 against 7.0 — so only order can tell them apart. The gate sits on the pulse's input
  rather than its output, so a jerk already running finishes.

  One more thing verified against Dolphin's source (2026-09), since the expressions depend on it:
  `|` is a max, and it binds looser than `/`.
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

## Eden's cemuhook bindings

Eden's cemuhook engine addresses a pad by `guid`, `port` and `pad`, and nothing else:

- **`guid`** is the server's IPv4 as a 32-bit integer in hex, right-aligned in an otherwise-zero
  UUID written raw, no dashes — so loopback is `0000000000000000000000007f000001`.
- **`port`** is the UDP port, not a controller index.
- **`pad`** is a global index, `client * 4 + slot`, so with our server as Eden's only client it is
  the DSU slot itself.

`EdenDsuConfig`'s button table is protocol wiring, not preference: cemuhook's two button bytes
packed low-then-high are exactly Eden's `PadButton` values, with Home and the touchpad click riding
the bytes above them. Sticks arrive as raw bytes that Eden reads as `(v − 127) / 127`, so the axis
pairs need no inversion.

**Motion is the one thing a pad cannot share.** A pad packet carries a single accelerometer and
gyroscope, so `motion` is always index 0 and each hand streams on a slot of its own ([Slots](#slots)).
The hand holding the player's own slot lands on `motionright` and its second hand on `motionleft`;
one Joy-Con, or a pair that ran out of slots, binds both to the same pad — which is what Eden's own
auto-mapping does for every device.

A cemuhook pad carries the full DS4 button set and both sticks, so nothing else is needed for a
player to play: the Virtual Gamepad is an alternative route to the same keys, not a prerequisite.

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
