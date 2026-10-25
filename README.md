# Joycon2Android

[![Latest release](https://img.shields.io/github/v/release/JoeGeC/joycon2android)](../../releases/latest)
[![Downloads](https://img.shields.io/github/downloads/JoeGeC/joycon2android/total?cacheSeconds=3600)](../../releases)
[![Android 7.0+](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](#what-you-need)
[![License: GPL-3.0](https://img.shields.io/github/license/JoeGeC/joycon2android)](LICENSE)
[![Support on Ko-fi](https://img.shields.io/badge/Ko--fi-support-FF5E5B?logo=kofi&logoColor=white)](https://ko-fi.com/joestechprojects)

Use Nintendo Switch 2 **Joy-Con 2** and **Pro Controllers** on Android — as gamepads in any app, and
with motion in emulators.

Android can't pair these controllers from its Bluetooth settings, because they use their own BLE
protocol rather than standard HID. Joycon2Android connects to them itself and presents each player
as an ordinary gamepad.

## Features

- **Joy-Con 2, Switch 2 Pro Controller and NSO GameCube controller** — connect several at once, GL/GR
  paddles included.
- **Up to 8 players** — each with a sideways Joy-Con, a pair, or a Pro Controller.
- **A gamepad per player** — every app sees one standard controller per player, so multiplayer just
  works.
- **Motion for emulators** — a DSU (cemuhook) server streams gyro, accelerometer, buttons and sticks
  for players 1–4.
- **One-tap emulator setup** — writes controller and motion bindings for Eden and Dolphin.
- **Custom button mapping** — choose which button drives which emulator button, per console, or a
  keyboard key, a mouse click or scroll, or Android's Home, Back and Screenshot.
- **One Output card** — pick an emulator, any other game or emulator, or mouse and keyboard, and it
  runs what that needs.
- **Joy-Con mouse** — stand a Joy-Con 2 on its rail on a desk and it moves Android's pointer.
- **Live readout** of buttons, sticks, motion and battery, with each card in the controller's real
  shell colour.
- **Third-party clones** — controllers that speak the console's protocol instead, tested with the
  NYXI Hyperion 3 (see [protocol.md](docs/protocol.md#console-protocol-controllers)). Motion on
  these is accelerometer-only: they send no gyroscope data, so gyro aiming can't work. They need
  Android 10 or later to stay connected; on older versions they drop about 30 seconds in.

## Setup guide

### What you need

- Android 7.0 (API 24) or newer, with Bluetooth LE
- Joy-Con 2, a Switch 2 Pro Controller or an NSO GameCube controller
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
   and **R** on another for a pair, **SL + SR** for a sideways Joy-Con, **L + R** on a Pro Controller
   or GameCube controller.
3. If a controller shows as the wrong type, tap its type at the top of its card and pick the right one
   before assigning it. The choice is remembered for that controller; **Auto** goes back to what it
   reports.

### 3. Turn on the output

The **Output** card asks what you're playing and runs what that needs. It turns on by itself once a
player is assigned; its switch stops it. Grant Shizuku permission the first time.

| Tab | For | Runs |
|---|---|---|
| **Virtual gamepad** | Android games and any emulator | each player appears to every app as `Joy-Con Virtual Gamepad <N>` |
| **Motion (DSU)** | emulators that read DSU (cemuhook) | gyro, buttons and sticks over DSU |
| **Mouse & keys** | PC games under Winlator, Droiddeck and the like | a virtual keyboard and mouse, mapped **WASD + mouse** to start |

The first two tabs have **Emulator auto setup** (step 5): Dolphin or Eden once installed, then
**Other**. Pick **Other** for Android games or any other emulator, set up in its own settings (pick the
gamepad in its controller settings, or point it at DSU: [manual setup](#manual-setup)); its
**Mapping** holds just the app's own keys (Android, mouse, keyboard). Each tab remembers its pick,
and the mapping that applies is the one for whatever it's set to.

A tab's name lights up while its output is running.
The app stays connected in the background while its notification is showing.

- **Motion.** DSU streams gyro, buttons and sticks to emulators on the same device at
  `127.0.0.1:26760`. Players 1–4 get motion; a pair streams from its right Joy-Con, and its left one
  too when a slot is free. Rest each controller for ~2 s at the start; the gyro recalibrates whenever
  it's still.
- **Mouse.** Stand a Joy-Con 2 on its rail on a flat surface and it moves the pointer while the mouse
  is up. It clicks and scrolls with the player's **Mouse** mapping; with none, L or R clicks, ZL or ZR
  right-clicks and the stick scrolls, and those never reach the game while it rests there. Not on Pro,
  GameCube or third-party controllers.
- **Keyboard and mouse on the other tabs** come up by themselves once the mapping uses them: a
  keyboard key or Android key, or a mouse input. Otherwise they stay off.

> [!TIP]
> With the gamepad and DSU both on, an emulator sees each controller twice — map from one, and turn
> the gamepad off while mapping DSU inputs so detection doesn't grab it.

### Settings

The gear at the top right opens the settings panel:

- **Layout** — detailed controller graphics or compact rows.
- **Show tips** (on by default) — the hints under auto setup and on the mapping screen. Tap any tip
  to turn them all off.
- **Open emulators on the main screen** (on by default, two-screen devices such as the AYN Thor) —
  when the app offers to start the emulator after setup, it opens on the main screen instead of the
  one this app is on.
- **Faster controller updates** (on by default) — Joy-Cons send input about twice as often, for
  smoother motion and quicker buttons, at a battery cost. Applies while the output is on.
- **Ignore this device's motion in Eden** (on by default, needs Shizuku) — stops Eden mixing this
  device's gyro into Player 1. Lifted when the virtual gamepad and DSU are both off.

### 5. Set up your emulator

On the **Virtual gamepad** or **Motion (DSU)** tab, pick Dolphin or Eden and tap **Set up**: the app writes the emulator's controller config for that tab. The app offers to start
the emulator afterwards — it only reads its config when it starts.

| Tab | Emulators | What it writes |
|---|---|---|
| Virtual gamepad | Dolphin (GameCube), Eden, Eden Nightly | buttons and sticks; analog L/R for a GameCube controller |
| Motion (DSU) | Dolphin (Wii), Eden, Eden Nightly | buttons, sticks and motion |

The gamepad button beside **Set up** (**Mapping** on Mouse & keys) opens the mapping editor, with a card per connected player —
tap one to open its bindings. On Eden a single Joy-Con is set up as a real left or right Joy-Con;
with **Pro Controller** (below) turned on, or on Dolphin, it's a Pro Controller held sideways, so every
button works in every game ([why](docs/virtual-gamepad.md#why-theyre-set-up-as-pro-controllers)). A
GameCube controller has layouts of its own, with Z on its Z button and analog L/R in Dolphin. A
target can take **several sources** — tick as many as you like, and any of them fires it.

- **Layouts.** Each player picks a layout to start from, which resets their changes. The card reads
  **Custom** once you change a binding, and the layout's name again if you change it back.
- **Saving.** The save icon beside the name keeps your mapping as a layout for any player holding the
  same body. It dims when the mapping already matches a layout. Deleting a layout (the bin in the
  dropdown) keeps everyone's buttons; they just read **Custom** until you save again. While a layout reads **Custom**, a hint points at its save icon.
- **Tips.** A note at the top reminds you that games differ: if one plays oddly, try another layout.
- **Group layout** at the top sets and saves everyone at once. A grip only some bodies fit —
  **Mario Kart Wheel**, say — gives the rest the same game's other grip. A layout you saved for one
  player is offered here too: it sets everyone holding the same body and leaves the rest alone.
  Layouts saved under the same name for different bodies are one option ("Players on L/R, R"), and
  each player gets the one saved for what they hold. A saved group remembers who held which body ("P1 L, P2 R, P3 L/R") and stays greyed
  out until those players are back.
- **Android.** Below the emulator's buttons, sticks and motion, each card has Android's **Home**,
  **Back** and **Screenshot**. A button picked here works as the device's own and never reaches the
  game; Home always leaves it. Like the rest of the mapping it's per player and console, and saved in
  layouts. Over DSU alone it needs Shizuku running.
- **Keyboard.** Below that, **Add a key** binds a button or a stick direction to a keyboard key —
  arrows, Enter, Esc, Space, letters, digits, F1–F12, Shift, Ctrl, Alt — for apps and games that take
  a keyboard; the stick on W, A, S and D walks. A stick presses its key at half tilt. Like Android
  keys it never reaches the game, and it needs Shizuku running. While a letter is bound,
  Android hides the on-screen keyboard; tap **Other** on the letter to bring it back.
- **Mouse.** Left, right and middle click, and scroll up and down, from a button or stick direction
  — for PC games that want a click or the wheel. A held scroll repeats. Works held, and on a Joy-Con
  standing on the desk as a mouse, replacing its default clicks.
  Needs Shizuku running.
- **Pro Controller.** On Eden, a lone Joy-Con also gets this switch, off by default. Off, Eden sees
  a real left or right Joy-Con and the game handles the grip and motion itself, which games such as
  Super Mario Party Jamboree's motion games need; you map its own buttons, plus SL and SR. If a game
  won't accept the Joy-Con, or reads it as held upright (Moving Out), turn it on. Switching resets
  that player's buttons to the grip's standard ones (Android and keyboard keys stay), and layouts save the switch
  with the rest. Its layouts put SL/SR on L/R, or on ZL/ZR with **Pro Controller (ZL/ZR)**. It needs **Set up** run again (and Eden restarted) after changing it. Dolphin always
  uses the sideways grip.
- **Wii controller.** On the Wii console, pick what each player holds: **Wiimote**, a
  **Sideways** remote (a lone Joy-Con only), or a **Classic** Controller. Sideways is for games that steer by
  tilting a sideways remote; up on the stick is then towards the rail rather than L or R. Classic
  plugs a Classic Controller into the remote, which still points for menus; the first time you pick
  it, its buttons start where a Pro Controller has them. The layout sets it (Sideways for Mario Kart,
  Classic for Classic Controller) and you can override it until you next pick a layout.
- **Thrusts.** On the Wii console, turn this on for games that measure a push, like Wii Play
  Billiards' cue. Not offered with Classic. Leave it off for swings: with it on, Wii Sports Golf putts too hard and Bowling
  misses your swing. Every layout starts with it off; saved layouts remember it.
- **Motion sensitivity.** On the Wii console, a slider from 50% to 200% scales swings and shakes
  without changing tilt. Every layout starts at 110%, which suits Wii Sports; 100% passes motion
  through unscaled. Saved layouts remember it.

The layouts the app ships:

| Layout | For |
|---|---|
| Wii | The Wii Remote's own arrangement: the trigger under your finger is B, 1 and 2 under the thumb |
| Joy-Con | The same, with the Joy-Con's own B as B, 1 and 2 on the shoulders so your thumb stays on the stick, and Recenter on the stick click. A GameCube controller has no stick click or −, so Recenter is on Y and − on X |
| Mario Kart Wheel | A lone Joy-Con held sideways as a wheel, laid out the way Mario Kart 8 uses one — 2 accelerates, 1 brakes, SR hops and tricks, and SL throws an item alongside the stick. Steers by **tilt**, so it plays as a sideways Wii Remote: the d-pad turns with it and a right Joy-Con aims from its tail |
| Mario Kart Nunchuck | The remote-and-nunchuk scheme, which steers by **stick** instead. A pair splits the halves across the hands, each index finger on the shoulder its controller keeps a trigger on; a lone Joy-Con plays both halves itself, its own stick standing in for the Nunchuk's and its rails carrying C and Z |
| Classic Controller | Plays as a Classic Controller in place of the Nunchuk, for any controller, buttons where a Pro Controller has them. A lone Joy-Con plays it sideways, its stick as the left stick. Only a player on Classic sets up a Pro or GameCube controller on Dolphin (Wii) |

> [!NOTE]
> Auto setup needs Shizuku, and some devices block writing into another app's `Android/data` — use
> [manual setup](#manual-setup) if it fails. Emulators rewrite their config when they exit, so let the
> app close an open one first. Run **Set up** again after changing players or updating the app.

### Manual setup

The DSU server binds each player to slot N−1. Press-to-detect in Android emulators never sees DSU
inputs, so pick them from the input list instead. The info button on a Motion tab mapping screen has these steps
and the server address to copy.

<details>
<summary><b>DSU input names</b></summary>

DSU uses DS4 names. SL, SR and C aren't streamed, except that a lone Joy-Con's SL/SR arrive as the
shoulder buttons its body lacks.

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

5. **Only for games that measure a push**, like Wii Play Billiards: under **Swing**, set **Forward** to
   ``($n = timer(1000), $d = clamp(($n - $l) * 1000, 0, 0.1) / 0.03, $l = $n, $s = $s + clamp((`Accel Forward` - `Accel Backward`) - $s, 0 - $d, $d), (`Accel Forward` - `Accel Backward`) - $s)``,
   **Range** to 7% and **Dead Zone** to 20%. Don't use Dolphin's `smooth()` here: it misbehaves on
   newer phones ([why](docs/dsu-motion.md#high-pass-without-smooth)). For a single Joy-Con use whichever pair its nose reads
   above — `Accel Right`/`Left` on a right Joy-Con, and the other way round when it plays sideways.
   Leave it empty otherwise — Dolphin adds it to the real motion, so Wii Sports swings read too
   strong ([why](docs/dsu-motion.md#thrusts)).
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
| Controller shows as the wrong type (stick turned sideways, no right stick) | Tap its type at the top of its card, pick the right one, then assign it |
| Controller stops responding | Press SYNC and reconnect; wait a moment if it stays silent |
| Controller shows "Connection lost" | Press SYNC on it and it reconnects to the same player by itself, or tap Scan. After two minutes it's removed |
| Controllers drop when a game starts | Some phones clear background apps when a game launches. Tap the yellow banner at the top to let the app run in the background (or set its battery use to unrestricted), and exclude it from the game launcher's cleanup |
| Gamepad doesn't show up in games | Check `adb shell getevent -p` lists "Joy-Con Virtual Gamepad" |
| No DSUClient device in the emulator | Check the server address, restart the emulator and open a mapping screen; `adb logcat -s DsuServer` shows whether it's connecting |
| Emulator won't detect DSU presses | Pick inputs from the list — detection never sees DSU — with the virtual gamepad off |
| Motion aiming stutters | Settings (gear, top right): keep **Ignore this device's motion in Eden** and **Faster controller updates** on |
| Tilting the device moves the aim in Eden | Turn on **Ignore this device's motion in Eden** |
| Joy-Con mouse doesn't move the pointer | The Output card's **Mouse & keys** tab name must be lit: the **Mouse & keys** tab, or a mapped mouse action on the others. Stand the Joy-Con on its rail, not its back, on a flat, non-glossy surface |
| Pointer drifts or starts off-screen | Rest the controller for ~2 s, then press Recenter |
| Sticks feel twitchy in Dolphin (hard to walk or aim) | Tap **Set up** for Dolphin again. A Pro Controller: map it in Dolphin first, then tap **Set up** once to add the calibration |
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

### Contributors

- **[mygamecache](https://github.com/mygamecache)** — the console protocol path for third-party
  Joy-Cons, tested on NYXI Hyperion 3 ([#26](https://github.com/JoeGeC/joycon2android/pull/26)).
- **[thisishuey](https://github.com/thisishuey)** — NSO GameCube controller support
  ([#30](https://github.com/JoeGeC/joycon2android/pull/30)), controller buttons for Android's Home,
  Back and Screenshot ([#31](https://github.com/JoeGeC/joycon2android/pull/31)), and Dolphin sticks
  calibrated to the GameCube gate ([#32](https://github.com/JoeGeC/joycon2android/pull/32)).

### Community projects

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
