# Joy-Con 2 → Android — Project Handoff

Context for continuing this in Claude Code. Pair this with
`joycon2_android_reference.md` (the protocol spec) and the two Kotlin files
already produced (`Joycon2Manager.kt`, `MainActivity.kt`).

---

## Goal

Use Nintendo Switch 2 **Joy-Con 2** controllers as gamepads **on an Android
device** — both as two separate controllers and (ideally) merged into one.
This is the *receive* direction: Joy-Con → phone. (Note: the well-known
"JoyCon Droid / JoyDroid" apps are the opposite direction — phone pretending to
be a controller for a Switch — and are NOT relevant here.)

## Why it's non-trivial

Original Switch-1 Joy-Cons use Bluetooth **Classic HID**, so Android pairs them
as a gamepad through normal Bluetooth settings. Joy-Con **2** are **BLE only**
(advertising flag "BR/EDR Not Supported") with a **custom GATT service** — not
standard HID-over-GATT — so Android's settings can't pair them as a controller.
You must connect over GATT, send vendor init commands, and parse the raw
notification packets yourself.

## Status — what's done and verified

- Protocol fully reverse-engineered by the community and confirmed working on
  macOS (`Joycon2BLEReceiver.mm`, by seitanmen, MIT). We extracted exact UUIDs,
  init bytes, packet offsets, and the button bitmask from that source — they're
  in `joycon2_android_reference.md`. **Trust that file over any web README**;
  several READMEs had wrong offsets (e.g. they said sticks at 0x08/0x0B; the
  real offsets are 0x0A/0x0D, buttons at 0x03 not 0x04).
- Confirmed on the actual hardware via nRF Connect that the device advertises
  Nintendo mfr ID 0x0553 and exposes the custom service.
- The macOS BLE_ONLY client connects to BOTH Joy-Cons successfully — so the
  protocol and our extracted values are good.
- First Android cut written: `Joycon2Manager.kt` (BLE engine, single
  controller) + `MainActivity.kt` (Compose UI showing live input). NOT yet
  compiled/run — first build in Android Studio / Gradle is the real test.

## Key facts (quick copy — full detail in the reference doc)

- Manufacturer ID: `0x0553`
- Input service: `ab7de9be-89fe-49ad-828f-118f09df7fd0`
- Notify char (input): `ab7de9be-89fe-49ad-828f-118f09df7fd2`
- Write char (commands): `649d4ac9-8eb7-4e6c-af44-1ea54fe5f005`  ← NOT the
  `...fdf` characteristic; using the wrong one was our early dead end.
- Init cmd 1 (buttons/standard): `0C 91 01 02 00 04 00 00 FF 00 00 00`
- Init cmd 2 (IMU/mouse/extended): `0C 91 01 04 00 04 00 00 FF 00 00 00`
- Write type: WRITE WITHOUT RESPONSE. 500 ms gap between the two init writes.
- Packet: 63 bytes, little-endian. Buttons uint32 @0x03, L stick 3B @0x0A,
  R stick 3B @0x0D (12-bit packed: x = v&0xFFF, y = (v>>12)&0xFFF),
  accel i16 @0x30/32/34, gyro i16 @0x36/38/3A, triggers u8 @0x3C/3D,
  battery u16 @0x1F (/1000 = volts).

## Hard-won gotchas (Android-specific)

1. **MTU first.** Default ATT MTU 23 truncates the 63-byte notification.
   `requestMtu(247)` after CONNECTED, wait for `onMtuChanged`, THEN
   `discoverServices()`. (Code already does this.)
2. **One GATT op at a time.** Android serializes writes/descriptor-writes.
   Must queue and advance only on the matching callback. (Code has an op queue.)
3. **CCCD required.** `setCharacteristicNotification(true)` alone won't deliver
   notifications — must also write descriptor `0x2902` =
   `ENABLE_NOTIFICATION_VALUE`.
4. **TRANSPORT_LE.** Pass it to `connectGatt` so it doesn't try classic.
5. **Connect cooldown.** Repeated quick connect attempts make the Joy-Con stop
   responding for a few minutes. Press SYNC to re-advertise; wait if dead.
6. **Side detection** is by peripheral name: "(L)"/"(R)"/"Pro". Left Joy-Con's
   right-stick bytes are garbage and vice versa.

## File map

- `Joycon2Manager.kt` — scan/connect/MTU/discover/notify/init/parse. Pushes a
  `Joycon2State` data class on every packet. Single controller.
- `MainActivity.kt` — Compose UI: status, battery, both sticks as moving dots,
  trigger bars, button grid that lights on press, raw accel/gyro readout.
- `joycon2_android_reference.md` — the authoritative protocol spec.

## Manifest permissions needed

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />
```

Deps: Compose BOM, `androidx.activity:activity-compose`, `material3`.
Package in the files is `com.example.joycon2` — change to taste.

## First-run debugging checklist

- UI connects but no input updating → check `onCharacteristicChanged` fires.
  Most likely the CCCD write didn't land before init writes (the op queue
  should prevent this; add a log in `onDescriptorWrite`).
- Packets arrive but truncated / parse errors → MTU didn't get raised; confirm
  `onMtuChanged` reports a high value before discovery.
- Nothing connects → wrong characteristic for writes, or cooldown. Re-sync.
- Cross-check the first live values against the macOS console output for the
  same physical button presses / stick positions.

## Roadmap after single-controller streaming works

1. **Dual connection** — generalize `Joycon2Manager` to manage N peripherals
   (or instantiate two). Each is fully independent: own connection, own init,
   own notification stream.
2. **Combined mode** — merge two `Joycon2State`s into one logical pad each
   frame (L: left stick, L/ZL, dpad, minus; R: right stick, R/ZR, ABXY, plus).
3. **Expose as a system gamepad** — the genuinely hard part, analogous to the
   DriverKit wall the macOS project hit. Stock Android has no public uinput
   API. Options:
   - Your own app/emulator consuming `Joycon2State` directly (easy, no special
     perms) — good first target.
   - System-wide virtual gamepad: needs root (`/dev/uinput`) or a limited
     InputManager/accessibility injection route (won't give true analog axes to
     arbitrary games).
   Decide the target early; it shapes everything downstream.

## Build/test note

The Kotlin was logic-checked against the working macOS source but not compiled
in this environment. The deprecated `writeCharacteristic(char)` /
`writeDescriptor(desc)` signatures (with `.value =`) are used for broad
compatibility; on API 33+ you may switch to the newer overloads that pass the
byte array and write type explicitly — functionally identical.
