# Joy-Con 2 over BLE — Protocol Reference & Android Port Plan

All values below are extracted from the **working** macOS implementation
(`Joycon2BLEReceiver.mm`). Where this disagrees with community READMEs, trust
this file — it reflects code that is confirmed to stream data from both Joy-Cons.

---

## 1. Identifiers

| Thing | Value |
|---|---|
| Manufacturer ID (advertising) | `0x0553` (Nintendo) |
| Input service | `ab7de9be-89fe-49ad-828f-118f09df7fd0` |
| **Notify** characteristic (input packets) | `AB7DE9BE-89FE-49AD-828F-118F09DF7FD2` |
| **Write** characteristic (commands) | `649D4AC9-8EB7-4E6C-AF44-1EA54FE5F005` |
| CCCD descriptor (to enable notifications) | `0x2902` |

> NOTE: The write characteristic is `649D4AC9...`, **not** the `...fdf` one.
> Writing to the wrong characteristic is why a manual nRF Connect attempt
> produces an enabled subscription but no data.

The write characteristic is **WRITE WITHOUT RESPONSE**.

---

## 2. Connection / init sequence (exact order from working code)

1. Scan. Match a peripheral whose advertising manufacturer ID == `0x0553`.
2. Connect.
3. Discover services, then discover characteristics for each service.
4. Locate write char (`649D4AC9...`) and notify char (`...FD2`).
5. Wait ~**0.5 s**, then send init command 1.
6. Wait **0.5 s**, then send init command 2.
7. At ~**2 s** after discovery, enable notifications on the notify char
   (set CCCD = notify).
8. Receive 63-byte notification packets; parse per section 4.

Both init commands are 12 bytes, written **without response**:

```
Command 1 (enable button/standard reports):
  0C 91 01 02 00 04 00 00 FF 00 00 00

Command 2 (enable IMU / mouse / extended reports):
  0C 91 01 04 00 04 00 00 FF 00 00 00
```

If you only want buttons + sticks, command 1 is the essential one; command 2
turns on the IMU/mouse fields.

---

## 3. Per-controller, not per-pair

Each Joy-Con (and the Pro Controller 2) is an **independent BLE peripheral**:
its own connection, its own two init writes, its own notification stream.

- **Two separate controllers** → 2 GATT connections → 2 virtual gamepads.
- **One combined controller** → 2 GATT connections → merge both parsed packets
  into a single virtual gamepad each frame.

Determine side from the advertised/peripheral **name**:
`(L)` / `Left` → Left, `(R)` / `Right` → Right, `Pro Controller2` → Pro.

On the **Left** Joy-Con the right-stick bytes are garbage (ignore them); on the
**Right** the left-stick region is the one to ignore. Button bits differ by
side — the mask table in section 5 is the full union; filter per side.

---

## 4. Packet layout (little-endian; packet is 63 bytes, >= 0x3C required)

| Field | Offset | Type | Notes |
|---|---|---|---|
| PacketID | 0x00 | uint24 | sequence counter |
| Buttons | 0x03 | uint32 | bitmap (section 5) |
| Left Stick | 0x0A | 3 bytes | 12-bit X = val & 0xFFF, Y = (val>>12) & 0xFFF |
| Right Stick | 0x0D | 3 bytes | same packing |
| Mouse X | 0x10 | int16 | |
| Mouse Y | 0x12 | int16 | |
| Mouse Unk | 0x14 | int16 | |
| Mouse Distance | 0x16 | int16 | |
| Mag X | 0x18 | int16 | |
| Mag Y | 0x1A | int16 | |
| Mag Z | 0x1C | int16 | |
| Battery Voltage | 0x1F | uint16 | volts = raw / 1000 |
| Battery Current | 0x28 | int16 | mA = raw / 100 |
| Temperature | 0x2E | int16 | °C = 25 + raw/127 |
| Accel X | 0x30 | int16 | 4096 = 1 G |
| Accel Y | 0x32 | int16 | |
| Accel Z | 0x34 | int16 | |
| Gyro X | 0x36 | int16 | 48000 = 360°/s |
| Gyro Y | 0x38 | int16 | |
| Gyro Z | 0x3A | int16 | |
| Trigger L | 0x3C | uint8 | analog |
| Trigger R | 0x3D | uint8 | analog |

Stick decode (from working code):

```
read 3 bytes at offset into val (little-endian)
x = val & 0x0FFF
y = (val >> 12) & 0x0FFF
```

---

## 5. Button bitmask (uint32 at offset 0x03)

```
0x80000000 ZL        0x40000000 L         0x00010000 SELECT (-)
0x00080000 LS        0x01000000 Dpad Down  0x02000000 Dpad Up
0x04000000 Dpad Right 0x08000000 Dpad Left 0x00200000 CAMERA
0x10000000 SR (L)    0x20000000 SL (L)     0x00100000 HOME
0x00400000 CHAT      0x00020000 START (+)  0x00001000 SR (R)
0x00002000 SL (R)    0x00004000 R          0x00008000 ZR
0x00040000 RS        0x00000100 Y          0x00000200 X
0x00000400 B         0x00000800 A
```

---

## 6. Android port — `BluetoothGatt` mapping

CoreBluetooth call → Android equivalent:

| macOS (CoreBluetooth) | Android (BluetoothGatt) |
|---|---|
| scanForPeripheralsWithServices | BluetoothLeScanner.startScan + ScanFilter on mfr 0x0553 |
| connectPeripheral | device.connectGatt(ctx, false, callback, TRANSPORT_LE) |
| discoverServices | gatt.discoverServices() |
| writeValue:type:WithoutResponse | char.writeType = WRITE_TYPE_NO_RESPONSE; gatt.writeCharacteristic(char) |
| setNotifyValue:YES | gatt.setCharacteristicNotification(char,true) + write CCCD 0x2902 = ENABLE_NOTIFICATION_VALUE |
| didUpdateValueForCharacteristic | onCharacteristicChanged → char.value (the 63-byte packet) |

### Android-specific gotchas (these break naive ports)

1. **One GATT op at a time.** Android serializes writes/descriptor-writes. You
   MUST queue: fire the next op only after the previous op's callback
   (`onCharacteristicWrite` / `onDescriptorWrite`). Because the command char is
   WRITE_NO_RESPONSE, `onCharacteristicWrite` still fires on modern Android, but
   to be safe add a short delay (the Mac code uses 500 ms between the two
   init writes) or chain off the descriptor-write callback.

2. **MTU.** Default ATT MTU is 23 → notifications truncate to 20 data bytes.
   Call `gatt.requestMtu(247)` right after `onConnectionStateChange` CONNECTED,
   wait for `onMtuChanged`, THEN discover services. You need MTU large enough
   for the 63-byte packet.

3. **CCCD is mandatory.** `setCharacteristicNotification(true)` alone is not
   enough on Android — you must also write the `0x2902` descriptor with
   `BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE`.

4. **Endianness.** All multi-byte fields are little-endian. Use
   `ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)` or mask manually.

5. **Connect cooldown.** Repeated quick connect attempts make the controller
   stop responding for a few minutes. Sync button to re-advertise; wait if dead.

### Suggested sequence on Android (per controller)

```
connectGatt(TRANSPORT_LE)
 └ onConnectionStateChange(CONNECTED)
     └ requestMtu(247)
         └ onMtuChanged
             └ discoverServices()
                 └ onServicesDiscovered
                     ├ find write char 649D4AC9..., notify char ...FD2
                     ├ enqueue: write CCCD(...FD2)=ENABLE_NOTIFICATION
                     ├ enqueue: write cmd1 to 649D4AC9... (NO_RESPONSE)
                     └ enqueue: write cmd2 to 649D4AC9... (NO_RESPONSE)
 └ onCharacteristicChanged(...FD2) → parse 63 bytes → emit input
```

### Presenting to Android as a gamepad

Reading the data in your own app is straightforward. Making the OS / other
games see it as a system gamepad is the hard part (the analog of the DriverKit
wall the macOS project hit):

- **Your own app only:** just consume the parsed struct. No special perms.
- **System-wide virtual gamepad:** stock Android has no public uinput API.
  Options: a rooted device writing to `/dev/uinput`; or an app that injects
  via the InputManager/accessibility route (limited, not true gamepad axes).
- **Combined vs separate:** maintain one parsed-state struct per connection;
  for "separate" emit two virtual pads; for "combined" merge L+R into one pad
  each frame (L: left stick, L/ZL, dpad, minus; R: right stick, R/ZR, ABXY,
  plus). Use `determineDeviceType` (by name) to route.

---

## 7. First validation step on Android

Before any virtual-gamepad work, port only steps in section 2 and log the
parsed struct (mirror the macOS console output). Confirm button bits and stick
ranges match what the Mac printed for the same physical inputs. Once the
numbers agree, the BLE half is done and the rest is input-plumbing.
