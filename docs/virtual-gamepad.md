# Virtual gamepad

How each player becomes a system gamepad, and why emulator bindings look the way they do.

## UHID

The app creates gamepads through Linux's UHID (user-space HID) interface:

- **`uhid_relay.c`** (`app/src/main/cpp`) — a small native binary that opens `/dev/uhid` and writes
  UHID events. It runs as the shell user (`u:r:shell:s0`), which has `/dev/uhid` access.
- **`UhidRelay`** — launches the relay through a `PrivilegedShell`, sends `UHID_CREATE2` (a
  4380-byte struct holding the HID descriptor), then streams `UHID_INPUT2` events over stdin.
  `PrivilegedAccess` supplies the shell via Shizuku's `IShizukuService.newProcess()`.
- **`ReportMapper`** — turns a `PlayerState` into a HID input report.
- **`GamepadManager`** — owns one relay per player and drives reports at input rate.

The device uses `BUS_USB` with generic IDs `0x1234:0x5678` (the [mouse](#mouse) `0x5679`, the [keyboard](#keyboard) `0x567A`) so the kernel's `hid-generic` driver
binds it. Nintendo's IDs would let `hid-nintendo` claim it and reject it.

Each assigned player gets its own device, `Joy-Con Virtual Gamepad <N>`, named by player number.
Emulators address it by enumeration rank instead: with P1, P2 and P4, P4 is the third pad,
`Android/3/Joy-Con Virtual Gamepad 4` ([Device identity](#device-identity)).

## Mouse

The **Joy-Con mouse** setting adds one more UHID device, `Joy-Con Virtual Mouse`, shared by every
Joy-Con 2. `PrivilegedDevicePolicy` keeps it up while the setting is on and Shizuku is running, independent of
the gamepad and DSU. A Joy-Con drives it while its distance reading says it rests on a surface
([sensor](protocol.md#mouse-0x100x17)) **and** it stands on its rail; lifted, it is a plain
controller again.

- **Distance alone mistakes a hand for a desk.** AYN Thor, 2026-10-23, a pair held to play: fingers
  round the rail read as a surface in 1–2 s bursts, moving the pointer and taking the stick for the
  wheel. So `JoyconMouse` also wants raw accel x, which reads +1 g on whichever axis points up, at
  ≥ 0.71 g away from the rail (−x on a left Joy-Con, +x on a right): within 45° of rail-down. A
  grip leaves x level, and a sideways hold turns the rail up.

| Input | Mouse | Notes |
|---|---|---|
| X/Y counters | REL_X / REL_Y, one count a unit | the change since the last report, int16-wrapped; +Y is towards the player, down the screen |
| L, R | primary button | |
| ZL, ZR | secondary button | |
| Stick Y | wheel | rate set by tilt, up to 20 detents/s past a 25% dead zone, timed by the packet clock |

- **A separate device, not a collection on the pad.** Pointer axes on the gamepad's evdev node would
  have Android class it as a mouse too.
- **What the mouse spends, the game never sees.** `MouseRouter` runs before `AndroidKeyRouter` and
  takes L/ZL/R/ZR and the stick off a Joy-Con on a surface, for both outputs.
- **Movement is summed, never conflated.** A pad report replaces the last; a mouse delta skipped is
  lost, so `PendingMouseFrame` adds deltas up until the relay writes.
- Only `PacketParser` (Joy-Con 2, `Side.LEFT`/`RIGHT`) fills the sensor; Pro, GameCube and
  console-protocol controllers have none.
- **Faster controller updates** counts the mouse as an output, doubling its report rate.
- `UhidOutput` owns the shared-device lifecycle for the mouse and the [keyboard](#keyboard).

## Keyboard

`Joy-Con Virtual Keyboard` is one UHID device shared by every player, up while Shizuku runs and the
gamepad or DSU is on (`PrivilegedDevicePolicy`). It carries the mapping's **Keyboard** keys, and the
[Android keys](#android-keys) the pad does not, in place of the shell.

| Report | Bytes | Carries |
|---|---|---|
| ID 1, Keyboard | modifiers (Ctrl, Shift, Alt), reserved, 6 key slots | keyboard keys; Screenshot as `0x46` Print Screen → `KEY_SYSRQ` |
| ID 2, Consumer | bit 0 AC Home, bit 1 AC Back | Home and Back, as on the pad |

- **Two descriptors, one up at a time.** The key slots span `0x1E`–`0x65`, past the letters, until a
  binding on either output's console uses a letter; then `0x00`–`0x65`. `KeyboardOutput` swaps
  devices when `KeyboardDeclarationPolicy` reports the change.
- **Keys are positions.** `KeyboardUsages` sends HID usages, which Android reads through the system
  layout, so on AZERTY the `Q` binding types `A`.
- **Buttons only, withheld like Android keys.** `KeyboardKeyRouter` runs after `AndroidKeyRouter`,
  each output withholding what its own console's mapping binds.
- **Six keys at once**, the boot-keyboard limit; Screenshot takes a slot. Modifiers don't.

AYN Thor, Android 13, 2026-10-23:

- **Print Screen takes a screenshot here**, unlike on the pad. Home, Back and Screenshot all fire,
  bound with either output.
- **A letter key hides the on-screen keyboard.** Android classes a keyboard with `KEY_Q` as
  alphabetic (`ALPHAKEY`, `KeyboardType: 2`) and Gboard stays hidden, even with
  `show_ime_with_hard_keyboard` on. So the key slots start at `0x1E` (`1`), past the letters;
  with no `KEY_Q` it is `KeyboardType: 1` and Gboard shows.
- Keys felt as quick as the pad's, with the gamepad or DSU alone. Not timed.
- Binding a letter swaps to the letters device (`ALPHAKEY`) and hides Gboard; clearing it swaps back
  and Gboard returns. Bound buttons reach the keyboard and never the pad.

## Report layout

15 bytes:

| Bytes | Content |
|---|---|
| 0–1 | 15 button bits, then one padding bit |
| 2 | hat switch (low nibble: 0 = N … 7 = NW, `0xF` = centre), padding |
| 3–10 | left X, left Y, right X, right Y — int16, −32767..32767, Y inverted (up is negative) |
| 11 | left trigger / brake — ZL as 0 or 255, or analog L travel ([below](#analog-triggers)) |
| 12 | right trigger / accelerator — ZR as 0 or 255, or analog R travel |
| 13 | overflow buttons, in a trailing vendor-defined collection: bit 0 GR, bit 1 C |
| 14 | Android keys, in a trailing Consumer Control collection: bit 0 Home, bit 1 Back ([below](#android-keys)) |

## Buttons and keycodes

Linux maps HID `Button n` in a Game Pad collection to `BTN_GAMEPAD + n - 1` (`BTN_A, BTN_B, BTN_C,
BTN_X, BTN_Y, BTN_Z, BTN_TL…`), and Android's key layout names that fixed sequence. `ReportMapper`
gives each button the bit whose keycode carries its own name, so nothing downstream deals in a
shift:

| Button | Bit | Keycode | Button | Bit | Keycode |
|---|---|---|---|---|---|
| A | 0 | 96 `BUTTON_A` | ZL | 8 | 104 `BUTTON_L2` |
| B | 1 | 97 `BUTTON_B` | ZR | 9 | 105 `BUTTON_R2` |
| Capture | 2 | 98 `BUTTON_C` | − | 10 | 109 `BUTTON_SELECT` |
| X | 3 | 99 `BUTTON_X` | + | 11 | 108 `BUTTON_START` |
| Y | 4 | 100 `BUTTON_Y` | Home | 12 | 110 `BUTTON_MODE` |
| GL | 5 | 101 `BUTTON_Z` | LS | 13 | 106 `BUTTON_THUMBL` |
| L | 6 | 102 `BUTTON_L1` | RS | 14 | 107 `BUTTON_THUMBR` |
| R | 7 | 103 `BUTTON_R1` | | | |

- **Capture and GL** take `BUTTON_C` / `BUTTON_Z`, the two slots with no Switch equivalent.
- **A controller with analog triggers moves its shoulders:** ZL/ZR onto L1/R1, the trigger clicks onto
  the stick clicks ([below](#analog-triggers)).
- **GR and C overflow.** One gamepad collection carries 15 buttons — a 16th lands on `0x13F`, which
  no key layout names — and the Switch 2 controllers have 17. For a Button usage outside a
  pointer/joystick/gamepad collection Linux falls back to `BTN_MISC + n - 1`, which key layouts name
  `BUTTON_1..16`: GR is **188**, C is **189**. The collection is vendor-defined so nothing
  interprets it. Firmware that re-publishes pads (see
  [Device identity](#device-identity)) forwards only keys it knows, so it may drop these two.
- **Triggers are Brake (left) and Accelerator (right)**, never reversed. On Android 14+ these arrive
  as `AXIS_LTRIGGER`/`AXIS_RTRIGGER` with compat copies as `AXIS_BRAKE` (23) and `AXIS_GAS` (22);
  Android 7–13 report only 23 and 22. Re-publishing firmware synthesises `L2`/`R2` from those axes —
  reversed, a ZR pull arrives as L2.
- **D-pad** is the hat (`AXIS_HAT_X` 15, `AXIS_HAT_Y` 16). Sticks are axes 0/1 (left) and 11/14
  (right). Eden's config uses numeric keycodes; Dolphin's uses names (`Button L2` = ZL,
  `Select` = −, …).

## Android keys

Each player's mapping, per console, has an **Android** section: buttons bound to Home, Back or
Screenshot act as the device's own keys and never reach the game. `AndroidKeyRouter` applies the
mapping of the console each output's card is set to, while that output runs. A key the pad does not
send goes through the [keyboard](#keyboard), or the shell if the keyboard failed to come up.

| Key | Virtual gamepad | DSU only |
|---|---|---|
| Home | byte 14 bit 0: Consumer `0x223` AC Home → `KEY_HOMEPAGE` → `HOME` | shell `input keyevent` |
| Back | byte 14 bit 1: Consumer `0x224` AC Back → `KEY_BACK` → `BACK` | shell `input keyevent` |
| Screenshot | shell `input keyevent 120` (`SYSRQ`), once per press | same |

- **Withheld before either output.** `AndroidKeyBindings.withheldFrom` drops bound buttons from the
  player's input as the controller reports it, so the same rule covers sideways Joy-Cons and both
  outputs.
- **Home and Back ride the pad when it runs**, even for a DSU binding, so a key bound on both
  screens fires once.
- **The shell is the slow path.** AYN Thor, Android 13, 2026-10-12: `input keyevent` took 50 ms to
  1.5 s and twice hung; the pad's Consumer keys are immediate. DSU never reaches Android, so it has
  no other path, and needs Shizuku.
- **Screenshot skips the pad.** Retroid Pocket Nova, Android 13, 2026-10-09: a Keyboard `0x46`
  Print Screen usage on the pad took no screenshot; `input keyevent 120` through the shell did.
- **Android handles `HOME` itself** and never passes it to the app in front, so a bound Home always
  leaves the game.

## Dolphin stick calibration

Dolphin setup writes `Main Stick/Calibration = 100.00` and `C-Stick/Calibration = 100.00` for each
stick it binds, keeping any `Calibration` the section already has. Setup leaves a Pro Controller
player's section to the player, so there it only adds both calibration keys, and only to a section
that already exists.

- **Uncalibrated, Dolphin passes the stick straight through:** a full ±1.0 tilt becomes GameCube ±127.
  A real pad's gate stops near ±101 (main) and ±92 (C-stick), so every push lands about a quarter
  further than on hardware, and walking or fine aiming is hard.
- **Calibrated to a full circle,** Dolphin maps the rim onto its `Gate Size` octagon: 79.37 (main)
  and 72.21 (C-stick) by default, averaged from genuine controllers.
- Dolphin for Android has no Calibrate button, and nothing else sets it. Sources: Dolphin's
  `GCPadEmu.h` and `StickGate.cpp`, and Dolphin PR #8963.
- Confirmed on a Retroid Pocket Nova with NSO GameCube controllers, 2026-10-11: walking and scope
  aiming feel like an original controller.

## Sideways Joy-Cons

### Why they're set up as Pro Controllers

Eden doesn't translate a sideways Joy-Con. For a `JoyconLeft` / `JoyconRight` npad it copies the raw
button bits and only sets an `is_horizontal` flag; on a real Switch the game's own `nn::hid` does the
rotation. Eden also masks an npad by type — a `JoyconLeft` has no A/B/X/Y at all, a `JoyconRight` no
D-pad or left stick.

So a single Joy-Con with the [Pro Controller](#single-joy-con) switch on, or on Dolphin, is
configured as a **Pro Controller**, rotated on our side. It loses the single-Joy-Con icon but every
input works in every game. Dolphin's GameCube pad has no sideways concept either, so
`DolphinGcpadConfig` binds the same rotated inputs.

### `SidewaysMapper`

Left Joy-Con turned 90° counter-clockwise, right 90° clockwise, as on a Switch:

| | Left Joy-Con | Right Joy-Con |
|---|---|---|
| Stick | `(4096 − rawY, rawX)` | `(rawY, 4096 − rawX)`, reported as LS |
| Button cluster → faces | Right → X, Down → A, Left → B, Up → Y | Y → X, X → A, A → B, B → Y |
| SL / SR | R / ZR | L / ZL |

- The cluster lands on **real face buttons, never the hat**. A sideways Joy-Con has no D-pad, and
  anything that binds a hat axis without its sign (Eden's press-to-detect does) can't tell left from
  right.
- **SL/SR fill the shoulders the body lacks** — a left Joy-Con already has L/ZL — which is why a
  single Joy-Con's `button_l` isn't keycode 102 on both sides. ZL/ZR get no default: held sideways,
  the body's own shoulders point away from the player.
- **A left Joy-Con's − is the Pro Controller's +** in both sideways layouts: a lone Joy-Con has only
  one of the two, and games pause on +.

Motion is turned too, but for DSU only — see [dsu-motion.md](dsu-motion.md#sideways-joy-cons).

### Single Joy-Con

Eden's default for a lone Joy-Con: it reaches Eden as itself and the game rotates it, as on a Switch.
The Eden mapping card's **Pro Controller** switch (off by default) sets up the sideways grip instead.

- **The switch is a mapping entry.** `PRO_CONTROLLER` = `true` among the body's bindings
  (`ProControllerEntry`), so layouts, saved sets and matching carry it with no extra store. The two
  grips bind different targets, so flipping it (`SetProControllerUseCase`) applies that grip's
  shipped layout — `SwitchProMapping` (Standard) or `SwitchProSidewaysMapping` (Pro Controller,
  which clears every other target so Standard's bindings layered beneath can't fire) — keeping the
  Android keys. `SwitchProSidewaysTriggersMapping` is the same grip with SL/SR on ZL/ZR, not L/R.
- **Targets** (`SwitchProTargets`): only the body's own buttons — left: D-pad, L, ZL, −, stick click,
  Capture; right: A/B/X/Y, R, ZR, +, stick click, Home — plus **SL/SR**, which `EdenControls.buttonKey`
  writes to `button_slleft`/`button_srleft` or `button_slright`/`button_srright`. Its one stick is
  `lstick` or `rstick`. The sideways grip offers neither SL nor SR.
- **Eden-only.** `ObserveSingleJoyconBodiesUseCase` reads which bodies are single; `EdenSingleJoycons`
  (`:app`) sets `PlayerState.asSingleJoycon` only on an output whose selected emulator is Eden, and on
  the players handed to the Eden generators. Dolphin keeps the sideways grip.
- **No rotation.** `GamepadState` passes buttons and stick through; a right Joy-Con's stick stays on
  the right-stick axes. `SidewaysMotion` leaves motion alone (`DsuStream.heldSideways` is false).
- **SL/SR ride the shoulders the body lacks** (`SingleJoyconMapper`), the same keycodes the sideways
  grip uses: left SL/SR → R/ZR (103/105), right SL/SR → L/ZL (102/104). The HID report is unchanged.
- **Npad type** `LeftJoycon` (2) / `RightJoycon` (3). Eden masks by type, which is why only the
  body's own buttons are targets.
- **Games that need one Joy-Con per player** (Super Mario Party Jamboree's motion games) won't take a
  Pro Controller. Games without single-Joy-Con support refuse a Joy-Con, and some read it upright
  (Moving Out, Eden 2026-10): Eden never rotates a `JoyconLeft`/`JoyconRight` and reports hold type
  `Vertical` unless the game sets `Horizontal`. For those the player turns on Pro Controller.
- **Older mappings** predate the switch and were all sideways. `LegacyGripMigration` (stored
  bindings) and `LayoutJson` (layouts without `format` 2) turn the switch on for them.
- Changing it needs Eden setup run again: the npad type lives in Eden's config.

### Analog triggers

A controller with analog triggers (`ControllerModel.hasAnalogTriggers`, the NSO GameCube controller)
drives bytes 11/12 with its calibrated L/R travel instead of ZL/ZR
([calibration](protocol.md#analog-triggers)). `AnalogTriggerMapper` moves its other shoulder buttons
so each lands where Android expects that control:

| Button | Bits | Keycodes |
|---|---|---|
| ZL / Z (the ZR bit) | 6 / 7 | `BUTTON_L1` / `BUTTON_R1` |
| L / R click at the bottom | 13 / 14 | `BUTTON_THUMBL` / `BUTTON_THUMBR` |

Confirmed in Dolphin on a Retroid Pocket Nova (Android 13) with an NSO GameCube controller,
2026-10-13. Eden is untested on hardware.

- **Nothing goes on `BUTTON_L2`/`BUTTON_R2`:** they can follow the trigger axes. Retroid Pocket Nova,
  Android 13, 2026-10-08: with Z on R2, pulling R fired Dolphin's `Button R2` and Z fired nothing.
  The Nova's L2/R2 setting wasn't the cause.
- **The stick-click bits are free to reuse:** the controller has no stick clicks. So no binding of
  its can use LS/RS: `emittedFor` resolves them to nothing for it, and its Switch Pro layout leaves
  the stick-click targets unbound.
- **DSU carries no trigger travel.** Its packet has analog L2/R2 bytes, but `DsuPacketEncoder` fills
  each analog byte from its button, 0 or 255, and this controller's ZL/ZR bits are moved, so L2/R2
  stay 0. Its buttons reach DSU as they reach the pad: ZL/Z as L1/R1, the clicks as L3/R3.

For such a player, Dolphin setup adds ``Triggers/L-Analog = `Axis 23+` `` and
``Triggers/R-Analog = `Axis 22+` ``. Dolphin names Android axes by number, and a trigger has only a `+`
half. Those two ids exist on every Android version the app supports. `Triggers/L`/`R` stay on the
click: while it is held, Dolphin reports the analog value as full.

## Emulator config

- **Dolphin's names** for each Android keycode and hat direction (`DolphinGcpadConfig`) are
  captured from a real mapping, not derived.
- **Every stick direction is its own Dolphin input**, so a stick target can mix tilts and buttons
  freely without the tilts losing their analog range.
- **Eden nests whole bindings inside one value** for a stick assembled from digital inputs, so
  `EdenControls` escapes their `:`, `,` and `$` as `$0`, `$1` and `$2` — exactly as Eden's own
  `ParamPackage` serializes them.
- **Sources resolve to what the body emits.** The relay rotates a lone Joy-Con
  ([`SidewaysMapper`](#sidewaysmapper)) before anything reaches an emulator, so both generators map a
  chosen source through that rotation.

## Device identity

An emulator addresses a pad by `port` — its enumeration rank, not the player number — plus, for
Eden, a `guid` built from vendor/product IDs. Both are read from the live input-device list on every
setup, never derived: a guessed number binds a config to the wrong device or to none, and a handheld's
built-in controller already occupies the low numbers. A player whose pad isn't enumerated yet is
skipped.

Each emulator counts differently. Each rule below is read from that emulator's source and mirrored
in `VirtualGamepadIdentity`:

- **Dolphin** takes the id in its `Android/<id>/<name>` qualifier from
  `InputDevice.getControllerNumber()`, Android's gamepad enumeration counter.
  `ControllerInterface::AddDevice` prefers `GetPreferredId()`, which the Android backend fills from
  `getControllerNumber()`, falling back to a duplicate-name index only for non-gamepads.
- **Eden** (yuzu lineage) numbers `port` by walking `InputDevice.getDeviceIds()` and counting
  *every* physical game controller it passes, so any built-in pad shifts ours along. In
  `InputHandler.getDevices()` a controller number already registered is skipped but still consumes
  a port, which `edenGamepads` reproduces.

Eden's `guid` is product then vendor ID, each a 16-digit hex half. Some handheld firmware
re-publishes an external gamepad under the built-in controller's IDs (AYN's Odin/Thor line does),
leaving two devices with our name, and a binding with the wrong guid is silently ignored. So every
field of a player's identity comes from one `InputDevice`: the last match, the republished one where
that happens.

Each setup also clears the player's old keys, so a layout or port change can't leave a stale
binding firing on another player's port.
