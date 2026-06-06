# Joycon2Android

Use Nintendo Switch 2 **Joy-Con 2** controllers as gamepads on an Android device over BLE.

Joy-Con 2 controllers use BLE with a custom GATT service (not standard HID-over-GATT), so Android can't pair them through normal Bluetooth settings. This app connects over GATT, sends vendor init commands, and parses raw notification packets to display live controller input.

## Features

- Connect multiple Joy-Con 2 controllers simultaneously
- Assign controllers to up to 4 players (left, right, or paired)
- Dual Joy-Con layout when both L+R assigned to one player
- Sideways single Joy-Con layout with rotated inputs (stick, d-pad, face buttons)
- Live display of buttons, sticks, IMU (accelerometer + gyroscope), and battery

## Architecture

```
BLE layer ─→ Domain state ─→ ViewModel ─→ Compose UI
```

| Layer | Key files |
|---|---|
| BLE | `BleScanner`, `ConnectionPool`, `JoyconConnection`, `GattOpQueue`, `PacketParser` |
| Domain | `PlayerAssignmentManager`, `Joycon2Manager` |
| Model | `PlayerState`, `JoyconInput`, `JoyconButton`, `ConnectedJoycon`, `Side` |
| UI | `JoyconScreen`, `PlayerView`, `ControllerLayout` (routes to `DualJoyconLayout`, `SidewaysLeftLayout`, `SidewaysRightLayout`) |

Single-activity Compose app. State flows from BLE notifications through `Joycon2Manager` into `Joycon2ViewModel` and down to Compose.

## Requirements

- Android API 24+ (minSdk 24, targetSdk 36)
- BLE-capable device
- Joy-Con 2 controller(s) in pairing mode (press SYNC)

### Permissions

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />
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
