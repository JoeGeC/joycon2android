# DSU motion server

How motion reaches emulators, and why the emulator bindings are shaped the way they are.

## The server

`DsuServer` implements the [cemuhook protocol](https://v1993.github.io/cemuhook-protocol/) over UDP
on port 26760.

**Bound to IPv4 `127.0.0.1`.** `getLoopbackAddress()` resolves to IPv6 `::1` on Android, and a
socket there never sees the `127.0.0.1` datagrams emulators send.

| Class | Job |
|---|---|
| `DsuPacketEncoder` | 100-byte pad packets and version/port-info replies, into a reused buffer |
| `DsuSlots` | players → the four slots, including pairs' second hands |
| `DsuClientRegistry` | sends each slot only to that slot's subscribers |
| `GyroCalibrator` | learns and subtracts each controller's gyro bias |
| `MotionConverter` | raw IMU → cemuhook's DS4 frame |
| `SidewaysMotion` | turns a lone Joy-Con's IMU into its sideways grip |

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

- **Raw x is the controller's right.** A rail-down pose (SL/SR on the table, gravity toward the
  controller's right) reads +1 g on the wire's *left* axis (2026-09). Roll mirrors with x, since
  angular velocity is a pseudovector.
- **Yaw is the one sign no measurement pins.** It turns about gravity, so neither a static pose nor
  the accel/gyro consistency check sees it; it follows the pointer's horizontal response. Mirroring x
  strictly implies mirroring yaw too, so if horizontal pointing ever reads backwards, flip yaw rather
  than re-deriving the frame.
- **Left Joy-Con and Pro are assumed to share the raw frame** — unverified. Recalibrate with
  `tools/dsu_client` if their motion feels rotated.

### Gyro bias

Joy-Con 2 gyros idle with a constant offset (+0.2 °/s yaw, +0.9 °/s roll observed), which clients
integrate into pointer drift. Whenever a controller's gyro stays within ~2.4 °/s for 240 samples
(hand tremor exceeds that), `GyroCalibrator` adopts the window mean as its bias, as the Switch does.

### Sideways Joy-Cons

A lone Joy-Con's buttons and stick arrive already rotated into its sideways grip
([virtual-gamepad.md](virtual-gamepad.md#sidewaysmapper)), and Eden presents it as a Pro Controller,
so `SidewaysMotion` turns its motion 90° about the button face to match. Without that, tilt reads as
if the nose pointed at the screen.

- **The direction was measured** (Eden, Mario Kart 8) and is the *opposite* of the stick's turn: the
  IMU axes don't line up with the stick's.
- **A pair's second hand isn't turned**, though it streams alone on its slot (`DsuStream.heldSideways`).
- **A [single Joy-Con](virtual-gamepad.md#single-joy-con) isn't turned** either: Eden sees a Joy-Con
  and the game orients its motion.
- **Dolphin turns it back.** Its emulated Wii Remote is the Joy-Con's own body, so
  `DolphinWiimoteConfig` maps a lone Joy-Con's IMU inputs back about the button face (table in the
  [README](../README.md#manual-setup)), putting the nose on the shoulder edge the player aims. The
  bodies rotate into their grips opposite ways, so their tables are half a turn apart.
- **Measure the pointer, don't reason about it.** `tools/dsu_client` plus a replay of
  `EmulateIMUCursor` settles in minutes what guessing costs days. Posed captures mislead: asked to
  hold an "aim up", a player makes a different rotation from the one they make while playing, so
  compare a captured session against candidate tables by how much cursor travel each yields.

### Wii controller

What a Wii player holds (`WiiController`): **Wiimote**, **Sideways** (a
[sideways remote](#playing-as-a-sideways-wii-remote)) or **Classic** (a
[Classic Controller](#classic-controller) in the remote). One choice, as the three exclude each other.

- **Seeded by the layout, overridable per player** (`MappingLayout.wiiController`,
  `WiiControllerRepository`; applying a layout clears the override). Sideways for both Mario Kart
  layouts, Classic for the Classic Controller layout.
- **Only a lone Joy-Con turns sideways.** A pair is offered Wiimote and Classic, and a
  layout's Sideways reads as Wiimote on it (`WiiController.heldBy`), so the layout still matches.
- **Classic holds a lone Joy-Con sideways too** (`heldSideways`): its stick, keyboard and mouse
  directions and its pointer axes turn as Sideways's do.
- **A first Classic gets the Classic Controller layout's bindings**; after that, both the remote's and
  the Classic's targets stay stored, so switching back and forth loses neither.
- **Stored in the `sideways_remote` DataStore**, which it replaced in place. `WiiControllerMigration`
  turned its booleans and the old `CLASSIC_CONTROLLER` binding entry into the enum.

### Playing as a sideways Wii Remote

- **Only a right Joy-Con turns.** A game written for that grip reads gravity against a remote whose
  nose points left, where a left Joy-Con's L/ZL edge already points. A right Joy-Con gives up its own
  body to steer true, and with it R/ZR as the nose: aiming moves to the tail.
- **Up follows the grip.** Off, a lone Joy-Con's stick reads up toward its L/R edge; on, toward its
  rail, where the relay already puts it (`emittedDirection`).
- **The D-pad turns a quarter on both bodies**, since the player's up is a sideways remote's right.
  That is Dolphin's own `dpad_sideways_bitmasks`, written into the bindings so Dolphin's *Sideways
  Wii Remote* option stays off — the option would turn the accelerometer a second time.
- **Pointing and a wheel want a right Joy-Con's nose half a turn apart**, and no Dolphin option
  bridges them: `GetOrientation()` turns a quarter (Sideways) or a quarter about the left axis
  (Upright), and reaches only the accelerometer, never `GetTotalTransformation()` and so never the
  pointer. Hence the choice lives in the layout.

### Tricks and wheelies

Mario Kart Wii tricks and pops wheelies off a flick, and reads only the accelerometer (no
MotionPlus). A Joy-Con flick is mostly rotation — captures peak past 1200 °/s summed with barely a g
of linear jerk, where jerking a real Wii Wheel throws the whole thing — so on its own it never
reaches the game. Playing sideways, the flick is read from the gyroscope and delivered as a shake of
the accelerometer, the path steering proves reaches the game. `Shake` is also a mapping target, so a
button can trick too.

`DolphinWiimoteConfig` appends this to `IMUAccelerometer/Up`, and to `/Down` with Up and Down
swapped:

```
+ pulse(<flick>, 0.6) * max(sin(timer(0.15) * 6.2832), 0) * 50
<flick> = (`Gyro Pitch Up` / 9) & not(pulse(`Gyro Pitch Down` / 9, 0.4))
```

- **Pitch only.** Over three captures (2026-09-22, right Joy-Con, 15 ms stream) a flick is 59–89%
  pitch on a lone sideways Joy-Con and a pair alike, while steering is roll:

  | Gesture | Raw pitch |
  |---|---|
  | steering, hard, 25 s | ≤ 2.8 rad/s |
  | wheelie flicks | 7.0 – 10.7 |
  | trick flicks | 8.8 – 14.5 |

  `pulse()` fires as its input crosses a half, so the threshold is 4.5 rad/s: 1.6× above the worst
  steering, 1.6× below the weakest flick. Separating by axis beats a slew limiter, which, fast enough
  to reject a turn, ate all but 0.9 rad/s of the slow wheelie down-flick.
- **Direction matters, because a wheelie is a state**: an up-flick starts one, a down-flick drops it.
  The remote is jerked the way it was flicked (positive wire pitch is up on both bodies), and the wave
  is half-rectified; a full one would cancel the wheelie four times a second.
- **Each direction locks the other out for 0.4 s.** Every flick rebounds the opposite way
  0.12–0.32 s later, often stronger than a genuine flick (8.5 against 7.0), so only order tells them
  apart. Gating `pulse()`'s input rather than its output lets a running shake finish.
- **A shake, not a push**: 0.6 s at ~6.7 Hz, 50 m/s² — past what an emulated remote reports. Shaking
  a Joy-Con hard for about a second landed tricks by hand where a single held push didn't.
- In Dolphin's expressions `|` is a max and binds looser than `/` (checked against its source, 2026-09).

Two approaches fail, so don't retry them:

- **Amplifying the accelerometer's own transient.** An emulated Wii Remote saturates around
  +3.9/−4.9 g (`ACCEL_ZERO_G` 0x80, `ACCEL_ONE_G` 0x9A over 8 bits) and the push already exceeds it,
  so a bigger number only clips sooner.
- **Dolphin's `Shake` group.** A full 7 g oscillation bound to a key never landed a trick (2026-09),
  though `m_shake_state.acceleration` does reach the reported acceleration.

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
sensors from Eden. `DeviceMotionBlockPolicy` (`:app`) holds it while the virtual gamepad or DSU is on.
The override lives in `system_server` until reset or reboot, so it is lifted when both turn off
(Disconnect All and the service stopping included), and again at launch in case the app was killed.
With only the virtual gamepad, Eden then has no motion source at all.

## Dolphin Wii Remote mapping

What `DolphinWiimoteConfig` writes, and why:

- **Swing is bound only with [Thrusts](#thrusts)** on.
- **The Nunchuk reads another pad.** Dolphin splits a control on its last colon, so
  `DSUClient/<slot>/Joycon2:Accel Up` reads the second hand's slot. A real Nunchuk has no gyro.
- **A remote no player uses is disconnected** (`Source = 0`) if it still reads our server. Left
  emulated, it reads whatever streams on its slot, such as a pair's second hand, and shows as another
  player with its own pointer. Remotes on any other device are left alone.
- **Recenter** is a mapping target, R by default (L on a lone left Joy-Con). Gyro pointing drifts, and pressing it while aiming
  at the screen centre is what summons the pointer.
- **`IMUIR/Total Yaw` is 60°.** Dolphin's 25° clamps the cursor after ±12.5° of turn, which a
  hand-held aim overruns; the clamp reads as the pointer sticking.

### Classic Controller

[Wii controller](#wii-controller) **Classic** plugs one into the remote, and
`DolphinClassicController` writes that remote instead:

- **`Extension = Classic`, its own targets only.** Classic targets are stored as `ClassicA`,
  `ClassicLeftStick_UP`, …, beside the remote's, which stay stored but aren't written. That includes
  the remote's Shake and Recenter: `ClassicShake` and `ClassicRecenter` stand in for them, so a change
  to one mode's binding leaves the other's alone.
- **The remote still points.** A real Classic Controller hangs off a remote that keeps its IR and
  accelerometer, and the Wii Menu and some games point in their menus, so the IMU lines are written
  as for a remote, with Sensitivity, Recenter and a bound Shake. No Swing or flick trick: no Classic
  game reads one, and Swing would jolt with every press. The layout puts Recenter where the remote's
  is, R (SR on a lone Joy-Con), which also presses the Classic Controller's R: no Classic game reads
  the pointer in play. Shake is left unbound. A GameCube controller has no motion, so it gets none. Pointing with
  Classic on is untested on hardware.
- **L and R are digital** (`Classic/Triggers/L`). DSU carries no trigger travel; Dolphin's mixed
  trigger reads a press as fully pulled.
- **Pro and GameCube controllers are written too**, but only in this layout. Otherwise setup leaves
  their remote to the player's own Dolphin mapping.
- **A lone Joy-Con plays it sideways**, buttons as `SwitchProSidewaysLayout` places them, its stick
  as the left stick. The d-pad is left unbound.

### Thrusts

A per-player switch in the mapping editor's Motion section (`MappingLayout.thrust`, `ThrustRepository`), off in
every shipped layout and overridable like [Sideways](#playing-as-a-sideways-wii-remote). On, it binds
Dolphin's Swing to the thrust axis.

- **Swing carries thrusts.** Dolphin moves the emulated remote only through Swing — the IMU path is
  rotation alone — so games that read a push as distance to the sensor bar (Wii Play Billiards'
  cue strength) see nothing from accel and gyro. A pair thrusts along `Accel Forward`; a sideways
  Joy-Con out through its face, `Accel Up`.
- **Off by default, because Swing adds to the accelerometer.** Dolphin reports
  `m_swing_state.acceleration + IMU accel`, rotated by the swing's angle, so every thrust reaches
  the game twice. A pendulum swing's centripetal pull lies along the remote's long axis, which is
  the thrust axis, so Wii Sports Golf putts read far too strong and Bowling misses the swing
  (2026-10, right Joy-Con; clearing Swing/Forward fixed both).
- **The swing input is signed and high-passed.** Pairing each input with its opposite makes it
  signed (Dolphin clamps a single input at zero). An accelerometer can't tell gravity from
  acceleration, so a tilted grip parks up to 1 g on the axis; subtracting a
  [slew-limited copy](#high-pass-without-smooth) cancels a held tilt within ~0.3 s while an ~80 ms
  thrust outruns it.
- **Range 7%, dead zone 20%.** Inputs arrive at 9.8 per g, so full range saturates on a twitch; 7%
  puts a full lunge at ~1.5 g. The dead zone keeps a tilted grip's leak from nudging the remote.

### Motion sensitivity

A per-player slider in the mapping editor's Motion section, 50–200% in steps of 5 (`RemoteMotion.sensitivityPercent`,
`SensitivityRepository`), 110% in every shipped layout and saved with custom ones. Below or above
100%, `DolphinWiimoteConfig` adds a [high-passed](#high-pass-without-smooth) copy of each
accelerometer axis to that axis:

```
IMUAccelerometer/Up = `Accel Up` + 0.25 * <high-pass of (`Accel Up` - `Accel Down`)>
```

- **Why not a gain.** `Accel Up * 1.25` also makes gravity read 1.25 g at rest, which bends tilt.
  The same 0.03 s slew limit as the [thrust](#thrusts) input passes swings and drops held tilt, so
  only motion is scaled.
- **The IMU pointer reads the boost too.** `EmulateIMUCursor` corrects its pitch and roll from the
  same accelerometer group at 2% per update, so a boost term that doesn't settle to zero at rest
  drags and turns the pointer.
- **One control per axis.** Dolphin reads each axis as one control minus its opposite and clamps
  neither, so the term goes on Up, Left and Forward only; on both it would count twice.
- **Why it exists.** Wii Sports Golf and Bowling read swings slightly soft (2026-10, right Joy-Con).
  Likely causes: one sample per 15 ms report against the Wii Remote's 100 Hz clips swing peaks, and a
  Joy-Con is shorter and lighter than a Wii Remote. A flat 1.25 gain felt slightly too strong; 110%
  felt right, hence the default.
- **The Nunchuk is scaled too**, from the second hand's slot, so both fists in Wii Sports Boxing read alike.

### High-pass without smooth()

`DolphinHighPass` builds the slew limiter that [Thrusts](#thrusts) and
[Motion sensitivity](#motion-sensitivity) subtract, from Dolphin variables:

```
$c_now  = timer(1000)
$c_step = clamp(($c_now - $c_last) * 1000, 0, 0.1) / 0.03   // seconds since last read, as slew
$c_last = $c_now
$c_slew = $c_slew + clamp(<x> - $c_slew, 0 - $c_step, $c_step)
<x> - $c_slew                                               // the expression's value
```

- **Dolphin's `smooth()` can't settle on newer phones.** While a game has set the PowerPC's
  non-IEEE mode (Wii Sports does), Dolphin leaves `FPCR.AH` set on the CPU thread
  (`Common/ArmFPURoundMode.cpp`), and that thread evaluates input expressions. On CPUs with
  FEAT_AFP (Armv8.7+), `AH` stops `FNEG` flipping a NaN's sign, and clang builds `copysign`'s mask
  as `movi #0xff…; fneg`, so `copysign` loses its sign. `smooth()` then only steps one way and runs
  away at ~33 units/s; `deadzone()` and `relative()` use `copysign` too.
- **Measured 2026-10:** `smooth()`'s code with Dolphin's FPCR ran away on a Galaxy S25 (Snapdragon 8
  Elite) and Pixel 9 (Tensor G4), and settled on an AYN Thor (Snapdragon 8 Gen 2, no FEAT_AFP),
  whose CPU ignores `AH`. Comparisons, `+ - * /`, `clamp()` and `timer()` are unaffected.
- **Variables are per Wii Remote**, shared by all its controls and its Nunchuk, so each control
  prefixes its own with the control's name (`$IMUAccelerometer_Up_slew`).
- **The step is capped at 0.1 s**, which bounds the first read (`$c_last` starts at 0), a pause, and
  the read where `timer(1000)` wraps (a negative step, clamped to 0).

## MotionPlus tutorial replays

A MotionPlus game replaying its tutorial video every boot isn't a mapping problem. The game records
"video seen" as `MPLS.MOVIE` in `Wii/shared2/sys/SYSCONF`, but `SysConf::~SysConf` writes back the
entry list Dolphin loaded at launch, dropping what the game appended. Set the flag with Dolphin
closed so the next launch loads it.
