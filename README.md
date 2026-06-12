# Joycon2Android

Use Nintendo Switch 2 **Joy-Con 2** controllers as system-wide gamepads on Android over BLE.

Joy-Con 2 controllers use BLE with a custom GATT service (not standard HID-over-GATT), so Android can't pair them through normal Bluetooth settings. This app connects over GATT, sends vendor init commands, parses raw notification packets, and creates virtual gamepad devices via UHID so any Android app can use them.

## Features

- Connect multiple Joy-Con 2 controllers simultaneously
- Assign controllers to up to 4 players (left, right, or paired)
- **Virtual gamepad output** — appears as a standard HID gamepad to all apps
- **DSU motion server** — streams motion + pad state to DSU/cemuhook-aware emulators (Dolphin, Cemu) over UDP
- Dual Joy-Con layout when both L+R assigned to one player
- Sideways single Joy-Con layout with rotated inputs (stick, d-pad, face buttons)
- Live display of buttons, sticks, IMU (accelerometer + gyroscope), and battery

## Setup Guide

### Prerequisites

- Android device running API 24+ with BLE support
- [Shizuku](https://shizuku.rikka.app/) installed and running (required for virtual gamepad)
- Joy-Con 2 controller(s)

### Step 1: Install Shizuku

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
2. Grant Shizuku permission when prompted (first time only)
3. The virtual gamepad will appear as a system input device

Once enabled, any app that supports gamepads (games, emulators, etc.) will see the virtual controller. The app runs a foreground service to keep the connection alive in the background.

### Step 5 (optional): DSU Motion Server for emulators

The virtual gamepad cannot carry motion (HID gamepad reports have no motion channel), so for
gyro aiming in emulators the app runs a [DSU/cemuhook](https://v1993.github.io/cemuhook-protocol/)
server instead — plain UDP on port 26760, no Shizuku needed.

Toggle **DSU Motion Server** in the app (visible once a player is assigned), then point any
DSU/cemuhook-capable emulator (Dolphin, Cemu, Citra, suyu, …) at the address the app shows:
`127.0.0.1:26760` for an emulator on the same phone, or — with **Allow other devices (LAN)**
enabled — the phone's Wi-Fi IP for an emulator on a PC on the same network.

DSU serves players 1–4 only. Pads (buttons/sticks) are streamed alongside motion, so an
emulator can map everything from the one DSU device. When a player has two Joy-Cons, motion
comes from the **right** one.

DSU runs independently of the Virtual Gamepad — enable either or both. With both on, an
emulator sees the controller twice (system gamepad + DSU device); map inputs from one.

#### Dolphin specifics

- **Desktop**: Controllers → Alternate Input Sources → DSU Client → add the phone's address.
- **Android**: there is no settings UI for the DSU client. Create `Config/DSUClient.ini` inside
  Dolphin's user folder (`Android/data/org.dolphinemu.dolphinemu/files/`, with this as the file's 
  entire content:

  ```ini
  [Server]
  Enabled = True
  Entries = Joycon2:127.0.0.1:26760;
  ```

- Restart Dolphin → Wii Remote 1 → Emulated → a `DSUClient/...` device appears in the device
  dropdown.
- **Map every input manually** (long-press a control → Advanced Mapping → pick from the input
  list): Dolphin Android's Detect only listens to Android input devices, so it never picks up
  DSU inputs — this looks like "buttons don't work" but is just the detection flow. Buttons use
  DS4 names (A=`Circle`, B=`Cross`, X=`Triangle`, Y=`Square`, L/R=`L1`/`R1`,
  ZL/ZR=`L2`/`R2`, −/+=`Share`/`Options`, sticks=`L3`/`R3`, D-Pad=`Pad N/S/E/W`,
  Home=`PS`, Camera=`Touch`). SL, SR and C have no DSU equivalent and aren't streamed —
  except a solo sideways Joy-Con, where SL/SR act as its shoulder buttons.
- Under Motion Input, map all Accelerometer + Gyroscope entries name-to-name (motion inputs are
  never auto-detectable, by design), and map a **Recenter** button for the Point group — gyro
  pointing drifts, so pressing Recenter in-game is what summons the pointer.
- Solo horizontal Joy-Con: enable "Sideways Wii Remote".

### Troubleshooting

| Issue | Fix |
|---|---|
| "Shizuku is not running" | Open Shizuku app and start the service |
| "Shizuku permission denied" | Open Shizuku → Apps → grant permission to Joycon2Android |
| Controller not found during scan | Press SYNC again; move closer to device |
| Gamepad not appearing in games | Check `adb shell getevent -p` for "Joy-Con Virtual Gamepad" |
| Controller stops responding | Press SYNC to reset, then reconnect |

---

## Architecture

```
BLE layer ─→ Domain state ─→ ViewModel ─→ Compose UI
                                    └──→ UHID relay ─→ /dev/uhid ─→ Android input system
```

| Layer | Key files |
|---|---|
| BLE | `BleScanner`, `ConnectionPool`, `JoyconConnection`, `GattOpQueue`, `PacketParser` |
| Domain | `PlayerAssignmentManager`, `Joycon2Manager` |
| Model | `PlayerState`, `JoyconInput`, `JoyconButton`, `ConnectedJoycon`, `Side` |
| UHID | `UhidRelay`, `GamepadManager`, `ReportMapper`, `ShizukuPermissionHandler` |
| DSU | `DsuServer`, `DsuPacketEncoder`, `MotionConverter`, `DsuClientRegistry` |
| Native | `uhid_relay.c` (standalone binary run via Shizuku) |
| UI | `JoyconScreen`, `PlayerView`, `ControllerLayout` |
| Service | `GamepadForegroundService` |

Single-activity Compose app. State flows from BLE notifications through `Joycon2Manager` into `Joycon2ViewModel` and down to Compose. When gamepad output is enabled, `PlayerState` changes are also fed to `GamepadManager` which converts them to HID reports and pipes them to the UHID relay process.

### Virtual Gamepad (UHID)

The app creates system-wide virtual gamepads using Linux's UHID (User-space HID) interface:

1. **`uhid_relay.c`** — A small native binary that opens `/dev/uhid` and writes UHID events using `write()`. Runs as a Shizuku shell process (`u:r:shell:s0` SELinux context, which has `/dev/uhid` access).

2. **`UhidRelay.kt`** — Launches the relay binary via `IShizukuService.newProcess()`, sends a UHID_CREATE2 event (4380-byte struct with HID report descriptor), then streams UHID_INPUT2 events through the stdin pipe.

3. **`ReportMapper.kt`** — Converts `PlayerState` into a 13-byte HID input report: 14 buttons + hat switch + 2x 16-bit sticks + 2x 8-bit triggers.

4. **`GamepadManager.kt`** — Manages per-player relay instances and collects from `StateFlow<PlayerState>` to drive reports at input rate.

The virtual device uses BUS_USB with generic vendor/product IDs (0x1234:0x5678) to ensure the kernel's `hid-generic` driver binds it (Nintendo VID/PID causes the `hid-nintendo` driver to claim and reject the device).

## Requirements

- Android API 24+ (minSdk 24, targetSdk 36)
- BLE-capable device
- Joy-Con 2 controller(s) in pairing mode (press SYNC)
- Shizuku running (for virtual gamepad feature)

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
