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

The device uses `BUS_USB` with generic IDs `0x1234:0x5678` so the kernel's `hid-generic` driver
binds it. Nintendo's IDs would let `hid-nintendo` claim it and reject it.

Each assigned player gets its own device, `Joy-Con Virtual Gamepad <N>`, named by player number.
Emulators address it by enumeration rank instead: with P1, P2 and P4, P4 is the third pad,
`Android/3/Joy-Con Virtual Gamepad 4` ([Device identity](#device-identity)).

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
mapping of the console each output's card is set to, while that output runs.

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

## Sideways Joy-Cons

### Why they're set up as Pro Controllers

Eden doesn't translate a sideways Joy-Con. For a `JoyconLeft` / `JoyconRight` npad it copies the raw
button bits and only sets an `is_horizontal` flag; on a real Switch the game's own `nn::hid` does the
rotation. Eden also masks an npad by type — a `JoyconLeft` has no A/B/X/Y at all, a `JoyconRight` no
D-pad or left stick.

So a single Joy-Con is configured as a **Pro Controller**, rotated on our side. It loses the
single-Joy-Con icon but every input works in every game. Dolphin's GameCube pad has no sideways
concept either, so `DolphinGcpadConfig` binds the same rotated inputs.

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

Motion is turned too, but for DSU only — see [dsu-motion.md](dsu-motion.md#sideways-joy-cons).

### Analog triggers

A controller with analog triggers (`ControllerModel.hasAnalogTriggers`, the NSO GameCube controller)
drives bytes 11/12 with its calibrated L/R travel instead of ZL/ZR
([calibration](protocol.md#analog-triggers)). `AnalogTriggerMapper` moves its other shoulder buttons
so each lands where Android expects that control:

| Button | Bits | Keycodes |
|---|---|---|
| ZL / Z (the ZR bit) | 6 / 7 | `BUTTON_L1` / `BUTTON_R1` |
| L / R click at the bottom | 13 / 14 | `BUTTON_THUMBL` / `BUTTON_THUMBR` |

Confirmed on a Retroid Pocket Nova (Android 13) with an NSO GameCube controller, 2026-10-13.

- **Nothing goes on `BUTTON_L2`/`BUTTON_R2`:** they can follow the trigger axes. Retroid Pocket Nova,
  Android 13, 2026-10-08: with Z on R2, pulling R fired Dolphin's `Button R2` and Z fired nothing.
  The Nova's L2/R2 setting wasn't the cause.
- **The stick-click bits are free to reuse:** the controller has no stick clicks. So no binding of
  its can use LS/RS: `emittedFor` resolves them to nothing for it, and its Switch Pro layout leaves
  the stick-click targets unbound.

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
