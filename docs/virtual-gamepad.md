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

### One device per player

Each assigned player gets its own device, `Joy-Con Virtual Gamepad <N>`. The name carries the
player number, but Android numbers input devices by enumeration order: with P1, P2 and P4 (no P3),
P4 is the third pad, `Android/3/Joy-Con Virtual Gamepad 4`. That's why `DolphinGcpadConfig` keys
`Device = Android/<id>/…` on enumeration rank while the port stays on the player number.

## Report layout

14 bytes:

| Bytes | Content |
|---|---|
| 0–1 | 15 button bits, then one padding bit |
| 2 | hat switch (low nibble: 0 = N … 7 = NW, `0xF` = centre), padding |
| 3–10 | left X, left Y, right X, right Y — int16, −32767..32767, Y inverted (up is negative) |
| 11 | left trigger / brake — digital, 0 or 255 |
| 12 | right trigger / accelerator — digital, 0 or 255 |
| 13 | overflow buttons, in a trailing vendor-defined collection: bit 0 GR, bit 1 C |

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
- **GR and C overflow.** One gamepad collection carries 15 buttons — a 16th lands on `0x13F`, which
  no key layout names — and the Switch 2 controllers have 17. For a Button usage outside a
  pointer/joystick/gamepad collection Linux falls back to `BTN_MISC + n - 1`, which key layouts name
  `BUTTON_1..16`: GR is **188**, C is **189**. Firmware that re-publishes pads (see
  [Device identity](#device-identity)) forwards only keys it knows, so it may drop these two.
- **Triggers are Brake (left) and Accelerator (right)**, never reversed. Android aliases
  `AXIS_LTRIGGER` to `AXIS_BRAKE` and `AXIS_RTRIGGER` to `AXIS_GAS`, and re-publishing firmware
  synthesises `L2`/`R2` from those axes — reversed, a ZR pull arrives as L2.
- **D-pad** is the hat (`AXIS_HAT_X` 15, `AXIS_HAT_Y` 16). Sticks are axes 0/1 (left) and 11/14
  (right). Eden's config uses numeric keycodes; Dolphin's uses names (`Button L2` = ZL,
  `Select` = −, …).

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

## Emulator config

- **Dolphin** sees each player's UHID pad as a distinct Android input device and qualifies its
  bindings `Android/<controllerNumber>/Joy-Con Virtual Gamepad <player>`. `DolphinGcpadConfig`'s
  name tables are Dolphin's own fixed names for each Android keycode and hat direction, captured
  from a real mapping rather than derived.
- **Every stick direction is its own Dolphin input**, so a stick target can mix tilts and buttons
  freely without the tilts losing their analog range.
- **Eden nests whole bindings inside one value** for a stick assembled from digital inputs, so
  `EdenControls` escapes their `:`, `,` and `$` as `$0`, `$1` and `$2` — exactly as Eden's own
  `ParamPackage` serializes them.
- **The relay remaps by orientation** before anything reaches an emulator (see
  [`SidewaysMapper`](#sidewaysmapper)), so which physical button arrives at a given Android control
  differs between a sideways single Joy-Con and a pair. Both Dolphin and Eden configs resolve a
  customized source to what that body actually emits.

## Device identity

An emulator addresses a pad by `port` — its enumeration rank, not the player number — plus, for
Eden, a `guid` built from vendor/product IDs. Both are read from the live input-device list on every
setup, never derived. A guessed number binds a config to the wrong device or to none, and any
handheld with a built-in controller already occupies the low numbers.

**Every emulator picks its own quantity, and none of them is the player number.** Each rule below is
read from that emulator's own source and mirrored in `VirtualGamepadIdentity`:

- **Dolphin** takes the id in its `Android/<id>/<name>` qualifier from
  `InputDevice.getControllerNumber()`, Android's gamepad enumeration counter.
  `ControllerInterface::AddDevice` prefers `GetPreferredId()`, which the Android backend fills from
  `getControllerNumber()`, falling back to a duplicate-name index only for non-gamepads.
- **Eden** (yuzu lineage) numbers `port` by walking `InputDevice.getDeviceIds()` and counting
  *every* physical game controller it passes, so any built-in pad shifts ours along. In
  `InputHandler.getDevices()` a controller number already registered is skipped but still consumes
  a port, which `edenGamepadPorts` reproduces.

The same goes for the vendor/product ids behind Eden's `guid`: some handheld firmware re-publishes
an external gamepad under the built-in controller's ids (AYN's Odin/Thor line does), leaving two
devices carrying our name, and a binding with the wrong guid is silently ignored. So every field of
a player's identity is taken from one and the same `InputDevice` — the last match, which is the
republished one where that happens.

Each setup also clears the player's old keys, so a layout or port change can't leave a stale
binding firing on another player's port.
