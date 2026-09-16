# Joy-Con 2 BLE protocol

What the app speaks to the controllers, and the Android BLE behaviour it has to work around.

Values come from the confirmed-working macOS implementation,
[Joycon2forMac](https://github.com/seitanmen/Joycon2forMac)'s `Joycon2BLEReceiver.mm`. Where this
disagrees with community READMEs, trust this file.

## Identifiers

| Thing | Value |
|---|---|
| Manufacturer ID (advertising) | `0x0553` (Nintendo) |
| Input service | `ab7de9be-89fe-49ad-828f-118f09df7fd0` |
| Notify characteristic (input packets) | `ab7de9be-89fe-49ad-828f-118f09df7fd2` |
| Write characteristic (commands) | `649d4ac9-8eb7-4e6c-af44-1ea54fe5f005` |
| CCCD descriptor | `0x2902` |

The write characteristic is **write without response**. Writing to the similar-looking `...fdf`
enables a subscription that never delivers data.

## Advertising

Manufacturer data for ID `0x0553` carries:

- **Bytes `[5..6]`** — little-endian product ID: `0x2067` left Joy-Con 2, `0x2066` right Joy-Con 2,
  `0x2069` Switch 2 Pro Controller. The advertisement has no local name, so this is the only type
  signal before input starts.
- **Bytes `[10..15]`** — the bonded host's MAC. Holding SYNC zeroes it; a button press on a synced
  controller wakes it into a short reconnect advertisement carrying the address. The scanner only
  accepts a zeroed field, so stray presses on nearby synced Joy-Cons don't flash into the list.

```
pairing:  01 00 03 7E 05 66 20 00 01 00 [00 00 00 00 00 00] 0F ...
wake:     01 00 03 7E 05 66 20 00 01 00 [09 A7 9A 55 E2 98] 0F ...   <- host MAC
```

### Why SYNC is needed every time

Reconnect-on-button-press is console-exclusive (investigated 2026-06):

- During a wake advertisement the controller refuses GATT connections from anyone but its bonded
  host (immediate status 133), and rejects standard SMP bonding (drops the link, status 22).
- The console pairs at the application layer instead. Report `0x15` cmd `0x01`
  "PairingSetAddress" exists, but a lone SetAddress write doesn't change the stored host (the reply
  echoes the controller's own MAC). The rest of the handshake (cmds `0x02`–`0x04`, presumably the
  LTK exchange behind the `ConsoleMacA/B` / `LtkA/B` SPI slots) is undocumented.
- Even documented, reconnecting likely needs link-layer encryption with that LTK, which Android's
  BLE API cannot inject.

## Connection sequence

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

Init commands, 12 bytes each, written without response 500 ms apart:

```
Command 1 (buttons/standard):  0C 91 01 02 00 04 00 00 FF 00 00 00
Command 2 (IMU/extended):      0C 91 01 04 00 04 00 00 FF 00 00 00
```

## Packet layout

63 bytes, little-endian. Each controller is its own BLE peripheral with its own notification
stream.

| Field | Offset | Type | Notes |
|---|---|---|---|
| PacketID | 0x00 | uint24 | sequence counter; in practice a millisecond clock |
| Buttons | 0x03 | uint32 | bitmap, below |
| Back paddles | 0x07 | uint8 | Pro Controller only, below |
| Left Stick | 0x0A | 3 bytes | 12-bit X = `val & 0xFFF`, Y = `(val >> 12) & 0xFFF` |
| Right Stick | 0x0D | 3 bytes | same packing |
| Mouse X/Y | 0x10–0x13 | int16 ×2 | |
| Mouse Unk | 0x14 | int16 | |
| Mouse Distance | 0x16 | int16 | |
| Mag X/Y/Z | 0x18–0x1D | int16 ×3 | |
| Battery Voltage | 0x1F | uint16 | volts = raw / 1000 |
| Battery Current | 0x28 | int16 | mA = raw / 100 |
| Temperature | 0x2E | int16 | °C = 25 + raw / 127 |
| Accel X/Y/Z | 0x30–0x35 | int16 ×3 | 4096 = 1 g |
| Gyro X/Y/Z | 0x36–0x3B | int16 ×3 | 48000 = 360 °/s |
| Trigger L | 0x3C | uint8 | analog |
| Trigger R | 0x3D | uint8 | analog |

A left Joy-Con's right-stick bytes are garbage, and a right Joy-Con's left-stick bytes are too.

### Buttons (uint32 at 0x03)

```
0x80000000 ZL          0x40000000 L           0x00010000 - (Select)
0x00080000 LS          0x01000000 Dpad Down   0x02000000 Dpad Up
0x04000000 Dpad Right  0x08000000 Dpad Left   0x00200000 Camera
0x10000000 SR (L)      0x20000000 SL (L)      0x00100000 Home
0x00400000 Chat (C)    0x00020000 + (Start)   0x00001000 SR (R)
0x00002000 SL (R)      0x00004000 R           0x00008000 ZR
0x00040000 RS          0x00000100 Y           0x00000200 X
0x00000400 B           0x00000800 A
```

### Back paddles (uint8 at 0x07)

```
0x01 GR                0x02 GL
```

## Stick range and centre

The raw 12-bit sticks neither span `0x000..0xFFF` nor rest at the midpoint, and both vary per
controller and per axis. Measured:

```
travel:  full left ~900   full right ~3400   full down ~890   full up ~3360   (half-span ~1250)
rest:    left Joy-Con  x 2080  y 2157        right Joy-Con  x 2014  y 2022
```

Rest isn't the midpoint of travel (those extremes midpoint to 2150/2125), so it has to be sampled.
Treating 2048 as centre and half-span leaves full deflection at ~60% with a 4–5% drift at rest.

`StickCalibrator` learns each axis' centre from the first still window after connecting, then
freezes it — a stick held at full deflection is perfectly still too. It scales each direction by
its own span, the same centre/below/above triple the factory calibration stores. It runs where
packets are parsed, so the live display, gamepad and DSU all see corrected values.

## Android BLE gotchas

1. **MTU first.** The default ATT MTU of 23 truncates 63-byte notifications: `requestMtu(247)`
   after connecting, wait for `onMtuChanged`, then discover services.
2. **One GATT operation at a time.** Queue them and advance only on the matching callback
   (`GattOpQueue`).
3. **Write the CCCD.** `setCharacteristicNotification(true)` alone delivers nothing; descriptor
   `0x2902` must be written too.
4. **Pass `TRANSPORT_LE`** to `connectGatt`, or it may try classic Bluetooth.
5. **Connect cooldown.** Rapid repeated connects make the controller stop responding. Press SYNC to
   re-advertise, and wait if it stays unresponsive.
6. **Connection interval is the report rate.** Balanced priority can settle on 30 ms (~33 Hz,
   measured on an AYN Thor); high priority brought it to 15 ms (~67 Hz). See
   [dsu-motion.md](dsu-motion.md#report-rate).
7. **Deprecated write APIs are deliberate.** The `.value =` pattern keeps API 24 support; the API
   33+ overloads behave the same.
