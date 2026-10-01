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
  signal before input starts. Left and right are confirmed on hardware; the Pro value is community
  reverse-engineering.
- **Bytes `[10..15]`** — the bonded host's MAC. Holding SYNC zeroes it; a button press on a synced
  controller wakes it into a short reconnect advertisement carrying the address. The scanner only
  accepts a zeroed field, so stray presses on nearby synced Joy-Cons don't flash into the list.

```
pairing:  01 00 03 7E 05 66 20 00 01 00 [00 00 00 00 00 00] 0F ...
wake:     01 00 03 7E 05 66 20 00 01 00 [09 A7 9A 55 E2 98] 0F ...   <- host MAC
```

### Why SYNC is needed every time

A button press only reconnects to the host the controller stored, and Android can't connect as that
host. Measured on genuine Joy-Con 2s (left and right) with an AYN Thor, Android 13, 2026-09-29:

- **The controller accepts the phone as its host.** The [console pairing
  handshake](#console-pairing-handshake) stores it, and the wake advertisement then carries the
  phone's address.
- **It still ignores the phone's connection.** Android starts LE connections from a resolvable
  private address, not the stored one. The controller never answers, and the link drops with `0x3E`
  (GATT status 62), the same as a Joy-Con stored to another host.
- **Android won't connect from its public address.** Setting `bluetooth.core.gap.le.privacy.enabled`
  to `false` from the shell is accepted but ignored on the Thor. A Galaxy S25 (Android 16) refuses it.
- Standard SMP bonding is rejected (drops the link, status 22). Whether a public-address connection
  would also need link-layer encryption with the LTK is untested.

#### Console pairing handshake

This is how a Switch 2 stores itself on the controller. Commands go to the side-specific command
characteristic (`ce49a830-dced-48ae-931e-c8cf88aadbea` left, `65a724b3-f1e7-4a61-8078-a342376b27ff`
right, write without response) as `id 91 01 sub 00 len 00 00` + data, behind 17 zero bytes, after
writing `01 00` to `00c5af5d-1964-4e30-8f51-1956f96bd282`. Replies notify on
`c765a961-d9d8-4d36-a20a-5315b111836a`, echoing `id` and `sub`.

1. `15/01`: `00 02`, the host address byte-reversed, then the same address with its lowest byte
   minus one.
2. `15/04`: `00` + A1 (any 16 bytes). The reply data is a status byte + B1. The LTK is `A1 xor B1`.
3. `15/02`: `00` + A2 (any 16 bytes). The reply is `AES-128-ECB(key = reversed LTK, block = reversed
   A2)`, which proves the key.
4. `15/03` `00`, then `03/07` with the second address + the reversed LTK, then `03/09` to store it.

A lone `15/01` doesn't change the stored host. Pairing replaces the console's entry: SYNC on the
Switch 2 restores it.

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
0x04000000 Dpad Right  0x08000000 Dpad Left   0x00200000 Capture
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

## Player LEDs

```
09 91 01 07 00 08 00 00 <mask> 00 00 00 00 00 00 00
```

The mask's low nibble lights P1–P4 solid (`0x01`, `0x02`, `0x04`, `0x08`), its high nibble flashes
them (`0x10` … `0x80`). `0xF0`, all flashing, is the controller's default cycling animation.

## SPI reads

The controller keeps its factory data in SPI flash. The app wants one field: the **shell accent
colour**, 3 bytes RGB at `0x01301F` — the per-side colour (coral right, blue left) the UI paints each
controller with. Not the body colour at `0x013019`: that is the near-black shell, the same on both
Joy-Cons.

The request reads the surrounding DeviceInfo block, `0x40` bytes from `0x013000`:

```
02 91 00 04 00 08 00 00  40 7E 00 00  00 30 01 00
report cmd               len magic    address, LE
```

Byte 2 must be `0x00`, as HandHeldLegend's procon2tool sends it. The init commands carry `0x01`
there, but an SPI read with `0x01` gets no reply.

The reply arrives on the command-response characteristic. Layout, little-endian, confirmed on a live
controller:

| Offset | Meaning |
|---|---|
| `0` | report type — `0x02` for SPI |
| `3` | command — `0x04` for SPI read |
| `8` | data length |
| `12..15` | source address, echoing the address requested |
| `16..` | data bytes, starting at that source address |

`SpiColorParser` finds the field at `16 + (wanted address − echoed address)`, so the block can be
requested at any alignment.

## Battery

The packet's voltage reads ~0.6 V below the cell's: ~3.30 V shows 75% on a Switch 2, ~3.60 V shows
100%. `BatteryGauge` interpolates Nintendo's Joy-Con thresholds (3.3 / 3.6 / 3.76 / 3.9 / 4.2 V,
from dekuNukem's docs) shifted down 0.6 V. Below ~3.0 V is extrapolated; no low readings have been
captured yet. `JoyconInput` carries the result as a `BatteryCharge` percentage, since the console
report gives a level rather than a voltage.

## Stick range and centre

The raw 12-bit sticks neither span `0x000..0xFFF` nor rest at the midpoint, and both vary per
controller and per axis. Measured:

```
travel:  full left ~900   full right ~3400   full down ~890   full up ~3360   (half-span ~1250)
rest:    left Joy-Con  x 2080  y 2157        right Joy-Con  x 2014  y 2022
```

Rest isn't the midpoint of travel (those extremes midpoint to 2150/2125), so it has to be sampled.
Treating 2048 as centre and half-span leaves full deflection at ~60% with a 4–5% drift at rest.

`StickCalibrator` runs where packets are parsed, so the live display, gamepad and DSU all see
corrected values:

- **Centre** is learned from the first still window (30 samples), then frozen: a stick held at full
  deflection is perfectly still too.
- **Each direction scales by its own span**, the centre/below/above triple the factory calibration
  stores. Spans are seeded just under the smallest travel measured (~1180 LSB), so full tilt works
  from the first packet, and only ever widen.

## Console-protocol controllers

Some third-party Joy-Con 2 clones (measured on a NYXI Hyperion 3, left and right, 2026-09) copy the
GATT table above but ignore the write characteristic and never notify on `...fd2`. They implement
only the side-specific channel a Switch 2 console uses, driven by `connection/console/`.

| Thing | Left | Right |
|---|---|---|
| Command write (no response) | `ce49a830-dced-48ae-931e-c8cf88aadbea` | `65a724b3-f1e7-4a61-8078-a342376b27ff` |
| Input notify | `cc1bbbb5-7354-4d32-a716-a81cb241a32a` | `d5a9e01e-2ffc-4cca-b20c-8b67142bf442` |
| Extended responses | `63a3810f-aec7-474b-9010-3d52403cb996` | `640ca58e-0e88-410c-a7f3-426faf2b690b` |
| Responses | `c765a961-d9d8-4d36-a20a-5315b111836a` | same |
| Session start | `00c5af5d-1964-4e30-8f51-1956f96bd282`, write `01 00` | same |
| Report rate descriptor | `679d5510-5a24-4dee-9557-95df80486ecb`, write `85 00` | same |

Commands take the same 8-byte header as above, behind 17 zero bytes. `ConsoleSession` replays the
console's order: hello (`07/01`), the DeviceInfo SPI read, firmware info (`10/01`), `16/01`, a
rumble sample, the player LED, feature mask `0x37`, four more SPI reads, `11/03`, `11/01`, then the
report-rate descriptor and the input CCCD. It does not pair (report `0x15`): pairing would store
this host on the controller and unpair it from its owner's console, and it buys nothing, because
Android connects from a resolvable private address the controller ignores.

### Input report

63 bytes on the input characteristic, report `0x07` left / `0x08` right:

| Offset | Size | Field |
|---|---|---|
| `0` | 1 | counter, +1 per report |
| `1` | 1 | power — bit 0 external, bit 1 charging, bits 2..5 battery level 0–9 |
| `2..3` | 2 | buttons, little-endian |
| `4` | 1 | always `0x07` |
| `5..7` | 3 | stick, packed 12-bit as above |
| `0x0E` (left) / `0x0F` (right) | 1 | motion block length — 4 during init, then 30 |
| `0x0F` (left) / `0x10` (right) | 30 | motion block, below |

#### Motion block

Seven 4-byte words and two spare bytes; the preceding byte gives the length. Offsets are from the
start of the block. ndeadly's reference calls this format unknown and allots it 0x28 bytes, listing
lengths {0, 30, 40}; a NYXI Hyperion 3 only ever sends 30.

| Word | Contents |
|---|---|
| `0x00` | counter; climbs steadily even while the controller lies still |
| `0x04`, `0x08`, `0x0C` | a dead-reckoned estimate in world coordinates, not raw sensor data |
| `0x10`, `0x14`, `0x18` | accel x, y, z: int16 in each word's **high half**, low half always zero, 4096 = 1 g |
| `0x1C` | two spare bytes, always zero — the block ends here |

Accelerometer, measured on a NYXI Hyperion 3 (both sides, 2026-10) against gravity in six
orientations, magnitude 1.00 g throughout: x is the controller's right and z leaves the button face,
as on a genuine Joy-Con 2, but **y runs the opposite way**, so `ConsolePacketParser` negates it.
Confirmed by Mario Kart Wii's tilt steering in Dolphin.

No rotation data reaches the app at all, so these controllers report
`MotionSupport.AccelerometerOnly`: DSU advertises the slot as a pad without a gyroscope, the readout
names it, and nothing downstream reads zeros as a real measurement. The 12 bytes a 40-byte block
would add are exactly where a gyro triple would sit, but nothing moved them: no reply to feature
select (`0x0C`) changes the length, including configure (`0x06`) with the IMU flag and the
reference's own data bytes, dropping the mouse feature (mask `0x07`), or either one of enable and
set-mask. Get-feature-info (`0x0C/0x01`) answers with a bare header on this hardware, where the
reference documents 8 bytes of capability data, so the firmware stubs it. Nor does anything else
reach it: the two characteristics the reference lists as "Input Report (Unknown)"
(`ab7de9be…7fde` and `d3bd69d2…`) accept a subscription and then never notify, and the report-rate
descriptor takes ten different values, content byte included, without the length budging.

The sensor itself is present. NYXI specifies 9-axis motion, and the three words at `0x04`–`0x0F`
hold a world-frame estimate the firmware could only resolve from a gyroscope: they ramp at rest as
an accelerometer bias integrates, and after a 90° yaw the two largest swap roles and rotate with it
while the third stays small. It is the raw rate that never reaches this report. No one has seen
this path carry gyro data on a genuine Joy-Con 2 either, since those take the common channel.

Mario Kart Wii tricks and wheelies ride `Gyro Pitch`, so they cannot fire; bind **Shake** to a
button instead ([why](dsu-motion.md#tricks-and-wheelies)). Tilt steering is accelerometer-only and
works.

Buttons, by bit: right `[2]` B A Y X R ZR + RS, `[3]` Home `0x01`, C `0x10`, SR `0x40`, SL `0x80`;
left `[2]` Down Right Left Up L ZL − LS, `[3]` Capture `0x01`, SR `0x40`, SL `0x80`.
`ConsolePacketParser` translates them into the bitmask above, so everything downstream is unchanged.

### Android workarounds

These controllers send an SMP Security Request on every connection, which a genuine Joy-Con 2 never
does — that request is what identifies them. Android pairs one device at a time, so a second clone
connecting while the first one's pairing is pending sends none; silence on the common channel 1.5 s
after init switches it over instead. Genuine Joy-Con 2s answer the console channel too, so that
silence is the only thing separating them: a controller moved over by the timeout goes back to the
common path as soon as a report arrives on `...fd2`, which only a controller speaking that protocol
sends.

The pairing itself can never succeed (Confirm Value Failed, or a 30 s timeout), so:

- `SecurityRequestReceiver` aborts the ordered `ACTION_PAIRING_REQUEST` broadcast, and no system
  dialog appears.
- `l2cu_start_post_bond_timer` then drops the link 3 s later unless it carries a dynamic L2CAP
  channel. The controller never answers LE credit-based connection requests, so `LinkHolder` keeps a
  pending `createInsecureL2capChannel(0x80).connect()` on the link — each attempt pends ~20 s.

Both depend on AOSP Bluetooth internals and may break on a future release.

High priority settles at 15 ms for these controllers (~67 reports/s). `ConnectionInterval` instead
asks the hidden `BluetoothGatt.requestLeConnectionUpdate` for 7.5 ms, the LE minimum, reached
through HiddenApiBypass because the method is on the blocked list; at 7.5 ms both controllers
deliver ~200 reports/s with no lost reports (RedMagic Astra, Android 16, 2026-09). It falls back to
`CONNECTION_PRIORITY_HIGH`, and every console session asks again when another controller joins,
since Android can slow an existing connection down when one does.

## Android BLE gotchas

1. **MTU first.** The default ATT MTU of 23 truncates 63-byte notifications: `requestMtu(247)`
   after connecting, wait for `onMtuChanged`, then discover services.
2. **One GATT operation at a time.** A second issued before the callback is silently dropped.
   `GattOpQueue` advances on the matching callback, or after a timeout if none comes.
3. **Write the CCCD.** `setCharacteristicNotification(true)` alone delivers nothing; descriptor
   `0x2902` must be written too.
4. **Pass `TRANSPORT_LE`** to `connectGatt`, or it may try classic Bluetooth.
5. **Connect cooldown.** Rapid repeated connects make the controller stop responding. Press SYNC to
   re-advertise, and wait if it stays unresponsive.
6. **Connection interval is the report rate.** Each report carries buttons, sticks and motion.
   Balanced priority settled on 30 ms (~33 Hz), which reads as motion stutter at 60 fps; high
   priority brought it to 15 ms (~67 Hz) (AYN Thor, 2026-09). **Faster controller updates**
   requests high priority while the virtual gamepad or DSU is on (`FasterUpdatesPolicy`), at a
   battery cost on both ends.
7. **Deprecated write APIs are deliberate.** The `.value =` pattern keeps API 24 support; the API
   33+ overloads behave the same.
