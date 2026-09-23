# Joycon2Android

[![Latest release](https://img.shields.io/github/v/release/JoeGeC/joycon2android)](../../releases/latest)
[![Downloads](https://img.shields.io/github/downloads/JoeGeC/joycon2android/total)](../../releases)
[![Android 7.0+](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](#what-you-need)
[![License: GPL-3.0](https://img.shields.io/github/license/JoeGeC/joycon2android)](LICENSE)
[![Support on Ko-fi](https://img.shields.io/badge/Ko--fi-support-FF5E5B?logo=kofi&logoColor=white)](https://ko-fi.com/joestechprojects)

Use Nintendo Switch 2 **Joy-Con 2** and **Pro Controllers** on Android — as gamepads in any app, and
with motion in emulators.

Android can't pair these controllers from its Bluetooth settings, because they use their own BLE
protocol rather than standard HID. Joycon2Android connects to them itself and presents each player
as an ordinary gamepad.

## Features

- **Joy-Con 2 and Switch 2 Pro Controller** — connect several at once, GL/GR paddles included.
- **Up to 8 players** — each with a sideways Joy-Con, a pair, or a Pro Controller.
- **A gamepad per player** — every app sees one standard controller per player, so multiplayer just
  works.
- **Motion for emulators** — a DSU (cemuhook) server streams gyro, accelerometer, buttons and sticks
  for players 1–4.
- **One-tap emulator setup** — writes controller and motion bindings for Eden and Dolphin.
- **Custom button mapping** — choose which button drives which emulator button, per console.
- **Live readout** of buttons, sticks, motion and battery, with each card in the controller's real
  shell colour.

## Setup guide

### What you need

- Android 7.0 (API 24) or newer, with Bluetooth LE
- Joy-Con 2 or a Switch 2 Pro Controller
- [Shizuku](https://shizuku.rikka.app/), for the virtual gamepad and emulator auto setup — DSU motion
  works without it

> [!IMPORTANT]
> Press **SYNC** every time you connect. Only a Switch 2 can wake a controller back up from a button
> press ([why](docs/protocol.md#why-sync-is-needed-every-time)).

### 1. Install

1. Download the APK from the [latest release](../../releases/latest) and install it. Allow
   **Nearby devices** when asked.
2. Install Shizuku from [Google Play](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api)
   or [GitHub](https://github.com/RikkaApps/Shizuku/releases), and start it:
   - **Wireless debugging** (no root, recommended): Developer options → Wireless debugging, then
     pair from Shizuku's notification.
   - **ADB:** `adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh`
   - **Root:** tap Start.

### 2. Connect

1. Hold **SYNC** on the controller and tap **Scan**.
2. Assign each controller to a player — from the app, or with the Switch combos: **L** on one Joy-Con
   and **R** on another for a pair, **SL + SR** for a sideways Joy-Con, **L + R** on a Pro Controller.

### 3. Turn on the virtual gamepad

Turn on **Virtual Gamepad** and grant Shizuku permission the first time. Each player appears to every
app as its own controller, `Joy-Con Virtual Gamepad <N>`. The app stays connected in the background
while its notification is showing.

### 4. Motion for emulators (optional)

A gamepad has no motion channel, so for gyro aiming turn on **DSU Motion Server**. Emulators on the
same device connect to `127.0.0.1:26760`.

- Players 1–4 get motion. A pair streams from its right Joy-Con, and its left one too when a slot is
  free.
- Rest each controller for ~2 s at the start; the gyro recalibrates whenever it's still.
- **Motion settings** on the DSU card has two options: **Ignore this device's motion in Eden** (on by
  default, needs Shizuku), and **Faster motion updates** for smoother aiming at a battery cost.

> [!TIP]
> DSU and the virtual gamepad are independent. With both on, an emulator sees each controller twice —
> map from one, and turn the gamepad off while mapping DSU inputs so detection doesn't grab it.

### 5. Set up your emulator

Both cards have **Emulator auto setup**: pick an emulator and tap **Set up**. The app offers to start
the emulator afterwards — it only reads its config when it starts.

| Card | Emulators | What it writes |
|---|---|---|
| Virtual Gamepad | Eden, Eden Nightly, Dolphin (GameCube) | buttons and sticks |
| DSU Motion Server | Eden, Eden Nightly, Dolphin (Wii) | buttons, sticks and motion |

The gamepad button beside **Set up** opens the mapping editor, with a card per connected player —
tap one to open its bindings. A single Joy-Con is set up as a Pro Controller held sideways, so every
button works in every game ([why](docs/virtual-gamepad.md#why-theyre-set-up-as-pro-controllers)). A
target can take **several sources** — tick as many as you like, and any of them fires it.

- **Layouts.** Each player picks a layout to start from, which resets their changes. The card reads
  **Custom** once you change a binding, and the layout's name again if you change it back.
- **Saving.** The save icon beside the name keeps your mapping as a layout for any player holding the
  same body. It dims when the mapping already matches a layout. Deleting a layout (the bin in the
  dropdown) keeps everyone's buttons; they just read **Custom** until you save again.
- **All players** at the top sets and saves everyone at once. A grip only some bodies fit —
  **Mario Kart Wheel**, say — gives the rest the same game's other grip. A saved set remembers who
  held which body ("P1 L, P2 R, P3 L/R") and stays greyed out until those players are back.
- **Sideways Wii Remote.** On the Wii console, a lone Joy-Con also gets this switch, for games that
  steer by tilting a sideways remote. On, up on the stick is towards the rail; off, towards L or R.
  The layout sets it (on for Mario Kart) and you can override it until you next pick a layout.

The layouts the app ships:

| Layout | For |
|---|---|
| Wii | The Wii Remote's own arrangement: the trigger under your finger is B, 1 and 2 under the thumb |
| Joy-Con | The same, with the Joy-Con's own B as B, and 1 and 2 on the shoulders so your thumb stays on the stick |
| Mario Kart Wheel | A lone Joy-Con held sideways as a wheel, laid out the way Mario Kart 8 uses one — 2 accelerates, 1 brakes, SR hops and tricks, and SL throws an item alongside the stick. Steers by **tilt**, so it plays as a sideways Wii Remote: the d-pad turns with it and a right Joy-Con aims from its tail |
| Mario Kart Nunchuck | The remote-and-nunchuk scheme, which steers by **stick** instead. A pair splits the halves across the hands, each index finger on the shoulder its controller keeps a trigger on; a lone Joy-Con plays both halves itself, its own stick standing in for the Nunchuk's and its rails carrying C and Z |

> [!NOTE]
> Auto setup needs Shizuku, and some devices block writing into another app's `Android/data` — use
> [manual setup](#manual-setup) if it fails. Emulators rewrite their config when they exit, so let the
> app close an open one first. Run **Set up** again after changing players or updating the app.

### Manual setup

The DSU server binds each player to slot N−1. Press-to-detect in Android emulators never sees DSU
inputs, so pick them from the input list instead.

<details>
<summary><b>DSU input names</b></summary>

DSU uses DS4 names. SL, SR and C aren't streamed, except that a sideways Joy-Con's SL/SR arrive as its
shoulder buttons.

| Button | DSU name | Button | DSU name |
|---|---|---|---|
| A | `Circle` | B | `Cross` |
| X | `Triangle` | Y | `Square` |
| L / R | `L1` / `R1` | ZL / ZR | `L2` / `R2` |
| − / + | `Share` / `Options` | LS / RS | `L3` / `R3` |
| Home | `PS` | Capture | `Touch Button` |
| D-pad | `Pad N/S/E/W` | Sticks | `Left X±/Y±`, `Right X±/Y±` |

</details>

<details>
<summary><b>Eden</b></summary>

1. In Eden's controller settings, turn on motion and the UDP controller, and add `127.0.0.1:26760` as a
   server.
2. Point each player's buttons, sticks and motion at the UDP controller. A pair has two motion inputs:
   the second reads the left Joy-Con's own slot.

</details>

<details>
<summary><b>Dolphin (Wii)</b></summary>

1. Create `Android/data/org.dolphinemu.dolphinemu/files/Config/DSUClient.ini` containing exactly:

   ```ini
   [Server]
   Enabled = True
   Entries = Joycon2:127.0.0.1:26760;
   ```

2. Restart Dolphin and open **Wii Remote N → Emulated**. A `DSUClient/<slot>/Joycon2` device appears
   once Dolphin's DSU client starts — open a mapping screen or a game first.
3. Map buttons from the input list (long-press → Advanced Mapping).
4. Under **Motion Input**, map the accelerometer and gyroscope, and map **Recenter** — press it while
   aiming at the screen centre to bring the pointer back. A pair maps name-to-name; a single Joy-Con
   streams in its sideways grip, so map it back to the remote's body:

   | Dolphin input | Pair | Left Joy-Con | Right Joy-Con |
   |---|---|---|---|
   | Accelerometer Up / Down | `Accel Up` / `Down` | same | same |
   | Accelerometer Left / Right | `Accel Left` / `Right` | `Accel Backward` / `Forward` | `Accel Forward` / `Backward` |
   | Accelerometer Forward / Backward | `Accel Forward` / `Backward` | `Accel Left` / `Right` | `Accel Right` / `Left` |
   | Gyroscope Pitch Up / Down | `Gyro Pitch Up` / `Down` | `Gyro Roll Right` / `Left` | `Gyro Roll Left` / `Right` |
   | Gyroscope Roll Left / Right | `Gyro Roll Left` / `Right` | `Gyro Pitch Up` / `Down` | `Gyro Pitch Down` / `Up` |
   | Gyroscope Yaw Left / Right | `Gyro Yaw Left` / `Right` | same | same |

   That restores each Joy-Con's own body, which is what **pointing** wants: the remote's nose is the
   shoulder edge, so aim R/ZR (or L/ZL) at the screen. Also set **Total Yaw** to around 60 —
   Dolphin's 25 clamps the cursor after ±12.5° of turn, which a hand-held aim overruns.

   **For a game written for a sideways Wii Remote** (Mario Kart Wii and its Wii Wheel) — both
   **Mario Kart** layouts write these for you:

   - Give a **right** Joy-Con the *left* column. Sideways, its top edge points where a sideways
     remote's tail does, so without that half turn the wheel steers backwards; a left Joy-Con
     already matches. Aiming then moves to the tail on that body.
   - **Turn the four D-pad bindings a quarter**: put the source you'd bind to Up on **D-Pad/Right**,
     Right on Down, Down on Left, Left on Up. A sideways remote's d-pad turns with it, so the
     player's up is the remote's right.
   - For **tricks and wheelies**, append
     ``+ pulse(<flick>, 0.6) * max(sin(timer(0.15) * 6.2832), 0) * 50`` to **IMUAccelerometer/Up**,
     where `<flick>` is ``(\`Gyro Pitch Up\` / 9) & not(pulse(\`Gyro Pitch Down\` / 9, 0.4))`` —
     then the same on **/Down** with Up and Down swapped
     ([why](docs/dsu-motion.md#tricks-and-wheelies)). Dolphin's own **Shake** group doesn't land
     tricks, but you can bind **Shake** to a button.

5. Under **Swing**, set **Forward** to
   ``(`Accel Forward` - `Accel Backward`) - smooth((`Accel Forward` - `Accel Backward`), 0.03)``,
   **Range** to 7% and **Dead Zone** to 20%. For a single Joy-Con use whichever pair its nose reads
   above — `Accel Right`/`Left` on a right Joy-Con, and the other way round when it plays sideways.
   This lets thrusts reach games like Wii Play Billiards ([why](docs/dsu-motion.md#dolphin-wii-remote-mapping)).
6. **Pair only:** under **Nunchuk → Motion Input**, map each accelerometer entry to the left Joy-Con's
   slot, e.g. `` `DSUClient/3/Joycon2:Accel Up` `` — the highest slot no player uses (3 with one
   player).

> [!WARNING]
> Leave Dolphin's **Sideways Wii Remote** off with this mapping — it rotates motion a second time.

</details>

## Troubleshooting

| Problem | Fix |
|---|---|
| "Shizuku is not running" | Open Shizuku and start it |
| "Shizuku permission denied" | Shizuku → Apps → allow Joycon2Android |
| Controller not found | Hold SYNC again and move closer |
| Controller stops responding | Press SYNC and reconnect; wait a moment if it stays silent |
| Gamepad doesn't show up in games | Check `adb shell getevent -p` lists "Joy-Con Virtual Gamepad" |
| No DSUClient device in the emulator | Check the server address, restart the emulator and open a mapping screen; `adb logcat -s DsuServer` shows whether it's connecting |
| Emulator won't detect DSU presses | Pick inputs from the list — detection never sees DSU — with the virtual gamepad off |
| Motion aiming stutters | DSU card → **Motion settings**: keep **Ignore this device's motion in Eden** on, and try **Faster motion updates** |
| Tilting the device moves the aim in Eden | Turn on **Ignore this device's motion in Eden** |
| Pointer drifts or starts off-screen | Rest the controller for ~2 s, then press Recenter |
| MotionPlus tutorial replays every boot (Dolphin) | Set `MPLS.MOVIE` in `Wii/shared2/sys/SYSCONF` with Dolphin closed ([why](docs/dsu-motion.md#motionplus-tutorial-replays)) |

## Roadmap and feedback

Planned and in-progress work is on the [project board](https://github.com/users/JoeGeC/projects/2).
Every card is an [issue](../../issues) you can subscribe to, and `area:` labels filter it to the parts
you use. Found a bug or want something? [Open an issue](../../issues/new/choose).

## Contributing

Start with [CONTRIBUTING.md](CONTRIBUTING.md), then the [docs](docs/README.md):

- [Architecture](docs/architecture.md) and [adding a feature](docs/adding-a-feature.md)
- [BLE protocol](docs/protocol.md) — identifiers, packets, Android BLE gotchas
- [Virtual gamepad](docs/virtual-gamepad.md) — UHID, keycodes, sideways Joy-Cons
- [DSU motion](docs/dsu-motion.md) — slots, motion frames, emulator mapping details
- [Debug tools](tools/README.md) — a DSU client for inspecting the stream

## Credits

Built on the community's reverse-engineering work, in particular:

- **[Joycon2forMac](https://github.com/seitanmen/Joycon2forMac)** by seitanmen — the working macOS BLE
  implementation the connection sequence, init commands and packet layout came from.
- **[JoyconDriver](https://github.com/german77/JoyconDriver)** by german77 — Switch controller protocol
  docs, including the Switch 2 Wireshark dissector and SPI flash map (shell colours).
- **[ProCon 2 Enabler Tool](https://handheldlegend.github.io/procon2tool/)** by HandHeldLegend — a
  Web Bluetooth implementation whose SPI-read bytes unlocked reading controller colours.
- **[Nintendo_Switch_Reverse_Engineering](https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering)**
  by dekuNukem — the original Joy-Con docs, and the battery voltage thresholds.
- **[cemuhook protocol docs](https://v1993.github.io/cemuhook-protocol/)** by v1993 — the DSU wire
  format.
- **[Shizuku](https://shizuku.rikka.app/)** by RikkaApps — privileged access without root.

## License

[GNU General Public License v3.0](LICENSE). If you find this useful, you can support development on
[Ko-fi](https://ko-fi.com/joestechprojects).

## Disclaimer

Not affiliated with, endorsed by, or sponsored by Nintendo. Joy-Con, Nintendo Switch and GameCube are
trademarks of Nintendo. The BLE protocol was reverse-engineered by the community from their own
hardware; this project contains no Nintendo code.

Claude (Anthropic's AI assistant) was used in building this project, directed and reviewed throughout
by a professional software and Android engineer.
