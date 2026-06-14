# Joycon2Android

Use Nintendo Switch 2 **Joy-Con 2** controllers as system-wide gamepads on Android over BLE.

Joy-Con 2 controllers use BLE with a custom GATT service (not standard HID-over-GATT), so Android can't pair them through normal Bluetooth settings. This app connects over GATT, sends vendor init commands, parses raw notification packets, and creates virtual gamepad devices via UHID so any Android app can use them.

## Features

- Connect multiple Joy-Con 2 controllers simultaneously
- Assign controllers to up to 4 players (left, right, or paired)
- **Virtual gamepad output** — each assigned player becomes its own standard HID gamepad, so apps see one controller per player
- **DSU motion server** — gyro/accel + full pad state for emulators (Dolphin, Cemu, …) over UDP, up to 4 independent players, with automatic gyro bias calibration
- **One-tap Dolphin setup** — writes Dolphin's config to match the current assignment (DSU + Wii Remote mappings, or GameCube controller mappings)
- Dual Joy-Con layout when both L+R assigned to one player
- Sideways single Joy-Con layout with rotated inputs (stick, d-pad, face buttons)
- Live display of buttons, sticks, IMU (accelerometer + gyroscope), and battery

## Setup Guide

### Prerequisites

- Android device running API 24+ with BLE support
- A privileged path for the virtual gamepad — either [Shizuku](https://shizuku.rikka.app/) installed and running, **or** the app's built-in wireless-debugging pairing (no Shizuku; set up in-app from the Gamepad card)
- Joy-Con 2 controller(s)

### Step 1: Install Shizuku

> Optional — skip this if you'd rather use the app's built-in **wireless debugging** path (no
> Shizuku). The Gamepad card walks you through pairing when you enable the gamepad without Shizuku.

1. Install Shizuku from [Google Play](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) or [GitHub](https://github.com/RikkaApps/Shizuku/releases)
2. Open Shizuku and start it using one of:
   - **Wireless debugging (recommended, no root):** Enable Developer Options → Wireless Debugging → pair Shizuku via the notification shade pairing method
   - **ADB:** Run `adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh` from a computer
   - **Root:** Tap "Start" in Shizuku (if rooted)
3. Verify Shizuku shows "Running" with a green status

### Step 2: Install Joycon2Android

Build from source or install the APK. Grant Bluetooth permissions when prompted.

### Step 3: Connect Controllers

1. Put your Joy-Con 2 into pairing mode by pressing the SYNC button
2. Tap "Scan" in the app — the controller should appear within a few seconds
3. Once connected, assign it to a player slot (P1–P4)

### Step 4: Enable Virtual Gamepad

1. With at least one controller assigned, toggle the **Gamepad** switch
2. Grant the privileged path access — Shizuku permission when prompted (first time only), or
   pair via wireless debugging from the Gamepad card if you're not using Shizuku
3. Each assigned player appears as its **own** input device named `Joy-Con Virtual Gamepad <N>`

Every assigned player becomes a separate standard gamepad — P1, P2, … are distinct devices, so
multiplayer "just works" and emulators can map each to a different port. Any app that supports
gamepads (games, emulators, etc.) sees them. The app runs a foreground service to keep the
connection alive in the background.

**Dolphin shortcut:** with the gamepad on, the Gamepad card offers **Map Dolphin GameCube
controllers**, which writes Dolphin's `GCPadNew.ini` (one section per player, with the right
mappings for a left, right, or paired layout) and sets each player's GameCube port to a Standard
Controller. Restart Dolphin afterward.

### Step 5 (optional): DSU Motion Server for emulators

The virtual gamepad can't carry motion (HID gamepads have no motion channel). For gyro
aiming in emulators, enable the DSU ([cemuhook](https://v1993.github.io/cemuhook-protocol/))
server — UDP port 26760, no Shizuku needed.

1. Assign controllers to players. Player N streams on DSU slot N−1 (P1–P4 only).
   A Joy-Con pair streams motion from its **right** Joy-Con.
2. Toggle **DSU Motion Server** in the app.
3. Point the emulator's DSU/cemuhook input source at the address the app shows
   (`127.0.0.1:26760`) — the server serves emulators running on this phone.
4. Rest each controller on a surface for ~2 s — the server learns the gyro's resting bias
   and re-learns it automatically whenever the controller is still.

DSU and the Virtual Gamepad are independent — enable either or both. With both on, an
emulator sees the controller twice (system gamepad + DSU device); map inputs from one,
and turn the Virtual Gamepad off while mapping so input detection doesn't grab it.

SL, SR and C are not streamed — DSU carries exactly the DS4 button set and every bit is
taken. Exception: a solo sideways Joy-Con's SL/SR arrive as its shoulder buttons.

#### DSU input names (DS4 conventions)

| Joy-Con | DSU name | Joy-Con | DSU name |
|---|---|---|---|
| A | `Circle` | B | `Cross` |
| X | `Triangle` | Y | `Square` |
| L / R | `L1` / `R1` | ZL / ZR | `L2` / `R2` |
| − / + | `Share` / `Options` | LS / RS | `L3` / `R3` |
| Home | `PS` | Camera | `Touch` |
| D-Pad | `Pad N/S/E/W` | Sticks | `Left X±/Y±`, `Right X±/Y±` |

#### Dolphin setup

**Automatic:** with DSU on, the DSU card's **Set up Dolphin and Wiimote mapping** button writes
both `DSUClient.ini` (the server entry) and `WiimoteNew.ini` (per-player Wii Remote mappings + the
accelerometer/gyro motion input) to match the current assignment, then prompts you to restart
Dolphin. It needs the privileged path (Shizuku / wireless debugging) connected; if the write fails
(some OEM builds block writing into another app's `Android/data`), fall back to the manual steps.

**Manual** (no DSU settings UI — configure by file):

1. Create `Config/DSUClient.ini` inside Dolphin's user folder
   (`Android/data/org.dolphinemu.dolphinemu/files/`, containing exactly:

   ```ini
   [Server]
   Enabled = True
   Entries = Joycon2:127.0.0.1:26760;
   ```

2. Restart Dolphin and open Wii Remote N → Emulated. A `DSUClient/<slot>/Joycon2` device
   appears per assigned player. Dolphin starts its DSU client lazily — open an
   input-mapping screen or a game first.
3. **Map every input manually** (long-press a control → Advanced Mapping → pick from the
   input list). Dolphin Android's press-to-detect only sees Android input devices, never
   DSU; motion inputs are not auto-detectable on any platform.
4. Under Motion Input, map all Accelerometer and Gyroscope entries name-to-name, and map
   **Recenter** — gyro pointing drifts, so pressing Recenter in-game while aiming at the
   screen centre is what summons the pointer.
5. Solo horizontal Joy-Con: enable "Sideways Wii Remote".

### Troubleshooting

| Issue | Fix |
|---|---|
| "Shizuku is not running" | Open Shizuku app and start the service |
| "Shizuku permission denied" | Open Shizuku → Apps → grant permission to Joycon2Android |
| Controller not found during scan | Press SYNC again; move closer to device |
| Gamepad not appearing in games | Check `adb shell getevent -p` for "Joy-Con Virtual Gamepad" |
| Controller stops responding | Press SYNC to reset, then reconnect |
| No DSUClient device in the emulator | Check the ini, restart the emulator, open a mapping screen; `adb logcat -s DsuServer` shows whether requests arrive |
| Emulator doesn't detect DSU button presses | Map manually — detection never sees DSU devices; turn the Virtual Gamepad off while mapping |
| Pointer drifts or starts off-screen | Rest the controller ~2 s to recalibrate, then press Recenter |

---

## Architecture

```
BLE notify ─→ Joycon2Manager ─→ SessionCoordinator ─→ AppUiState ─→ Compose UI
                                       ├──→ UHID relay ─→ /dev/uhid ─→ Android input system
                                       └──→ DSU server ─→ UDP :26760 ─→ emulators (cemuhook)
```

Single-activity Compose app, built as a **Gradle multi-module** project split by **feature ×
layer**. Each feature (`connection`, `assignment`, `gamepad`, `dsu`) has `domain` / `data` /
`presentation` modules, over shared `:core` modules and a thin `:app` composition root. The
split enforces the dependency rules at compile time — a ViewModel can't reach a repository
implementation, because presentation and data are separate modules sharing only domain.

State flows from BLE notifications through `Joycon2Manager` into the `SessionCoordinator`, which
combines connections and player assignments into an immutable `AppUiState` for the UI, and feeds
the same state to the gamepad and DSU outputs on a synchronous per-packet path.

**For contributors:** [`docs/architecture.md`](docs/architecture.md) is the living reference
(module graph, layers, dependency rules, composition root). [`docs/adding-a-feature.md`](docs/adding-a-feature.md)
is the recipe for adding or changing one. The sections below cover the hardware-level detail
those docs link back to.

### Virtual Gamepad (UHID)

The app creates system-wide virtual gamepads using Linux's UHID (User-space HID) interface:

1. **`uhid_relay.c`** — A small native binary that opens `/dev/uhid` and writes UHID events using `write()`. Runs as a shell-uid process (`u:r:shell:s0` SELinux context, which has `/dev/uhid` access).

2. **`UhidRelay.kt`** — Launches the relay binary through a `PrivilegedShell`, sends a UHID_CREATE2 event (4380-byte struct with HID report descriptor), then streams UHID_INPUT2 events through the stdin pipe. `PrivilegedAccess` supplies that shell from whichever privileged path is available — Shizuku's `IShizukuService.newProcess()`, or the in-app wireless-debugging ADB connection's `exec:` (`AdbShell`) — so neither the relay nor the rest of the app cares which granted the privilege.

3. **`ReportMapper.kt`** — Converts `PlayerState` into a 13-byte HID input report: 14 buttons + hat switch + 2x 16-bit sticks + 2x 8-bit triggers.

4. **`GamepadManager.kt`** — Manages per-player relay instances and collects from `StateFlow<PlayerState>` to drive reports at input rate.

One UHID device is created **per assigned player**, not one shared pad — each is its own
`/dev/uhid` node named `Joy-Con Virtual Gamepad <N>` (N = player number), so Android exposes them
as independent input devices and apps see a separate gamepad per player. Note that the device
*name* carries the player number, but Android assigns each device an `InputDevice` id by
enumeration order, not by player number — so P4 alongside P1 and P2 (no P3) is the 3rd pad,
`Android/3/Joy-Con Virtual Gamepad 4`.
(This is why `DolphinGcpadConfig` keys its `Device = Android/<id>/…` line on enumeration rank while
the section/port stays on the player number.)

The virtual device uses BUS_USB with generic vendor/product IDs (0x1234:0x5678) to ensure the kernel's `hid-generic` driver binds it (Nintendo VID/PID causes the `hid-nintendo` driver to claim and reject the device).

### DSU Motion Server

`DsuServer` is a cemuhook UDP server (port 26760, bound to IPv4 `127.0.0.1`). Pad batches
ride a buffered channel off the BLE state path — StateFlow conflation would drop motion
samples. Collaborators:

- **`DsuPacketEncoder`** — the 100-byte pad packets (and version/port-info responses),
  written into a reused buffer at ~120 Hz.
- **`MotionConverter`** — raw Joy-Con IMU frame → cemuhook's DS4 frame. Axes, signs, and
  scale factors were verified on hardware against Dolphin's Wii pointer; see the class
  docs for the measured frames and `tools/README.md` for the calibration workflow.
- **`GyroCalibrator`** — learns each controller's gyro bias whenever it rests and
  subtracts it, mirroring the Switch's own runtime recalibration.
- **`DsuClientRegistry`** — routes each slot's packets only to that slot's subscribers.
  This matters: DSU clients (Dolphin included) overwrite their pad state with every
  received packet without checking the slot, so server-side routing is what keeps
  multiple players independent.

Debug DSU clients for wire inspection and IMU calibration live in `tools/`.

## Requirements

- Android API 24+ (minSdk 24, targetSdk 36)
- BLE-capable device
- Joy-Con 2 controller(s) in pairing mode (press SYNC)
- A privileged path for the virtual gamepad — Shizuku running, or the in-app wireless-debugging pairing

### Permissions

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.INTERNET" /> <!-- DSU UDP server -->
```

---

## BLE Protocol Reference

All values extracted from the confirmed-working macOS implementation (`Joycon2BLEReceiver.mm`). Where this disagrees with community READMEs, trust this file.

### Identifiers

| Thing | Value |
|---|---|
| Manufacturer ID (advertising) | `0x0553` (Nintendo) |
| Input service | `ab7de9be-89fe-49ad-828f-118f09df7fd0` |
| Notify characteristic (input packets) | `ab7de9be-89fe-49ad-828f-118f09df7fd2` |
| Write characteristic (commands) | `649d4ac9-8eb7-4e6c-af44-1ea54fe5f005` |
| CCCD descriptor | `0x2902` |

The write characteristic is **WRITE WITHOUT RESPONSE**. Writing to the wrong characteristic (`...fdf`) produces an enabled subscription but no data.

### Advertising

Joy-Con 2 advertisements carry manufacturer data for ID `0x0553`. Bytes `[10..15]` hold the
**bonded host's MAC address**: a button press wakes a synced controller into a short-lived
reconnect advertisement carrying that address, while holding SYNC (pairing mode) zeroes the
field. The scanner only accepts controllers with a zeroed host field — otherwise every stray
button press on a nearby synced Joy-Con would flash in and out of the device list.

```
pairing:  01 00 03 7E 05 66 20 00 01 00 [00 00 00 00 00 00] 0F ...
wake:     01 00 03 7E 05 66 20 00 01 00 [09 A7 9A 55 E2 98] 0F ...   <- host MAC
```

**Reconnect-on-button-press is console-exclusive** (investigated 2026-06): during a wake
advertisement the Joy-Con refuses GATT connections from anyone but its bonded host
(immediate status 133). Standard SMP bonding is rejected (the controller drops the
connection, status 22). The console pairs at the application layer instead — report
`0x15` cmd `0x01` "PairingSetAddress" exists, but a lone SetAddress write doesn't change
the stored host (the reply just echoes the controller's own MAC), and the rest of the
handshake (cmds `0x02`–`0x04`, presumably the LTK exchange backing the `ConsoleMacA/B` /
`LtkA/B` SPI slots) is undocumented. Even with it documented, reconnect likely requires
link-layer encryption with that LTK, which Android's BLE API cannot inject. Hence: SYNC
is required for every (re)connection.

### Connection Sequence

```
connectGatt(TRANSPORT_LE)
 └ onConnectionStateChange(CONNECTED)
     └ requestMtu(247)
         └ onMtuChanged
             └ discoverServices()
                 └ onServicesDiscovered
                     ├ find write char 649D4AC9..., notify char ...FD2
                     ├ enqueue: write CCCD(...FD2) = ENABLE_NOTIFICATION
                     ├ enqueue: write cmd1 to 649D4AC9... (NO_RESPONSE)
                     └ enqueue: write cmd2 to 649D4AC9... (NO_RESPONSE)
 └ onCharacteristicChanged(...FD2) → parse 63 bytes → emit input
```

Init commands (12 bytes each, written without response, 500ms gap between):

```
Command 1 (buttons/standard):  0C 91 01 02 00 04 00 00 FF 00 00 00
Command 2 (IMU/extended):      0C 91 01 04 00 04 00 00 FF 00 00 00
```

### Packet Layout (63 bytes, little-endian)

| Field | Offset | Type | Notes |
|---|---|---|---|
| PacketID | 0x00 | uint24 | sequence counter |
| Buttons | 0x03 | uint32 | bitmap (see below) |
| Left Stick | 0x0A | 3 bytes | 12-bit X = val & 0xFFF, Y = (val>>12) & 0xFFF |
| Right Stick | 0x0D | 3 bytes | same packing |
| Mouse X/Y | 0x10-0x13 | int16 x2 | |
| Mouse Unk | 0x14 | int16 | |
| Mouse Distance | 0x16 | int16 | |
| Mag X/Y/Z | 0x18-0x1D | int16 x3 | |
| Battery Voltage | 0x1F | uint16 | volts = raw / 1000 |
| Battery Current | 0x28 | int16 | mA = raw / 100 |
| Temperature | 0x2E | int16 | C = 25 + raw/127 |
| Accel X/Y/Z | 0x30-0x35 | int16 x3 | 4096 = 1G |
| Gyro X/Y/Z | 0x36-0x3B | int16 x3 | 48000 = 360 deg/s |
| Trigger L | 0x3C | uint8 | analog |
| Trigger R | 0x3D | uint8 | analog |

### Button Bitmask (uint32 at offset 0x03)

```
0x80000000 ZL          0x40000000 L           0x00010000 - (Select)
0x00080000 LS          0x01000000 Dpad Down   0x02000000 Dpad Up
0x04000000 Dpad Right  0x08000000 Dpad Left   0x00200000 Camera
0x10000000 SR (L)      0x20000000 SL (L)      0x00100000 Home
0x00400000 Chat        0x00020000 + (Start)   0x00001000 SR (R)
0x00002000 SL (R)      0x00004000 R           0x00008000 ZR
0x00040000 RS          0x00000100 Y           0x00000200 X
0x00000400 B           0x00000800 A
```

### Per-Controller Notes

Each Joy-Con is an independent BLE peripheral with its own connection and notification stream. Side detection is by peripheral name: `(L)` = Left, `(R)` = Right, `Pro Controller2` = Pro.

Left Joy-Con's right-stick bytes are garbage (ignored); Right Joy-Con's left-stick bytes are garbage.

---

## HID Report Descriptor

The virtual gamepad uses a standard HID gamepad descriptor (13-byte reports):

| Field | Bits | Range | Mapping |
|---|---|---|---|
| Buttons 1-14 | 14 | 0/1 | A, B, X, Y, L, R, ZL, ZR, -, +, LS, RS, Home, Camera |
| Padding | 2 | - | |
| Hat switch | 4 | 0-7 or null | D-pad (0=N, 1=NE, 2=E, ..., 7=NW, 0xF=center) |
| Padding | 4 | - | |
| Left Stick X | 16 | -32767..32767 | |
| Left Stick Y | 16 | -32767..32767 | inverted (up = negative) |
| Right Stick X | 16 | -32767..32767 | |
| Right Stick Y | 16 | -32767..32767 | inverted |
| Left Trigger | 8 | 0-255 | digital: 0 or 255 |
| Right Trigger | 8 | 0-255 | digital: 0 or 255 |

---

## Android BLE Gotchas

1. **MTU first.** Default ATT MTU 23 truncates 63-byte notifications. `requestMtu(247)` after CONNECTED, wait for `onMtuChanged`, then discover services.
2. **One GATT op at a time.** Android serializes writes/descriptor-writes. Queue ops and advance only on the matching callback (`GattOpQueue` handles this).
3. **CCCD required.** `setCharacteristicNotification(true)` alone won't deliver notifications — must also write descriptor `0x2902`.
4. **TRANSPORT_LE.** Pass to `connectGatt` so it doesn't attempt classic Bluetooth.
5. **Connect cooldown.** Repeated quick connect attempts make the Joy-Con stop responding. Press SYNC to re-advertise; wait if unresponsive.
6. **Deprecated API usage.** The `.value =` pattern for writes is used intentionally for API 24+ compatibility. On API 33+ the newer overloads are functionally identical.

---

## Sideways Mode

When a single Joy-Con is assigned to a player, it's held sideways (L rotated 90 CCW, R rotated 90 CW). The UI remaps:

- **Sticks:** L = `(4096 - rawY, rawX)`, R = `(rawY, 4096 - rawX)`
- **D-pad (L):** visual up = Right press, down = Left, left = Up, right = Down
- **Face buttons (R):** Y = top, X = right, A = bottom, B = left
- **SL/SR** become the top rail buttons (like shoulder buttons when held sideways)
