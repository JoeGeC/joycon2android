# Architecture

How the app is structured. To add or change a feature, follow [adding-a-feature.md](adding-a-feature.md).

A single-activity Compose app, split into Gradle modules by **feature × layer**. Each feature
(`connection`, `assignment`, `gamepad`, `dsu`, `update`, `settings`) has up to three modules — `domain`, `data`,
`presentation` — over shared `:core` modules and a thin `:app` that wires them together. The split
*enforces* the dependency rules at compile time: presentation and data share only domain, so a
ViewModel cannot reach a repository implementation.

## Module graph

| Module | Plugin / type | Depends on |
|---|---|---|
| `:core:model` | `joycon.kotlin.jvm` | — |
| `:core:designsystem` | `joycon.android.library.compose` | — |
| `:core:session` | `joycon.kotlin.jvm` | `:core:model`, `:feature:connection:domain`, `:feature:assignment:domain` |
| `:core:emulatorconfig` | `joycon.kotlin.jvm` | `:core:buttonmapping:domain` |
| `:core:buttonmapping:domain` | `joycon.kotlin.jvm` | `:core:model` |
| `:core:buttonmapping:data` | `joycon.android.library` | `:core:buttonmapping:domain`, `:core:model` |
| `:core:buttonmapping:presentation` | `joycon.android.library.compose` | `:core:buttonmapping:domain`, `:core:designsystem`, `:core:model` |
| `:feature:<f>:domain` | `joycon.kotlin.jvm` | `:core:model` ²³ |
| `:feature:<f>:data` | `joycon.android.library`¹ | `:feature:<f>:domain`, `:core:model` ³ |
| `:feature:<f>:presentation` | `joycon.android.library.compose` | `:feature:<f>:domain`, `:core:designsystem`, `:core:model` ³ |
| `:app` | `com.android.application` | every feature module + all `:core` |
| `:konsist` | `joycon.kotlin.jvm` (test-only) | — (scans the whole project) |

…for each feature `<f>` ∈ { `connection`, `assignment`, `gamepad`, `dsu`, `update`, `settings`, `output` }.

¹ `assignment:data` is pure Kotlin (`joycon.kotlin.jvm`) — it has no Android dependencies.

² `gamepad:domain` and `dsu:domain` also depend on `:core:emulatorconfig` and
`:core:buttonmapping:domain` for one-tap emulator setup. Each owns its own config *generators* over
that shared leaf; no feature depends on another.

`output:domain` also depends on `:core:buttonmapping:domain`, for the `Console` each mode maps.

³ Not `update`, which only reads GitHub's releases API and hands an APK to the system installer.

Per-module Gradle config (compileSdk, Java 11, Compose, test options) lives in three convention
plugins in `build-logic/convention/` ([which to use](adding-a-feature.md#the-convention-plugins)).

## The layers

**`domain`** — pure Kotlin/JVM. No Android, no Compose, no other layer. Holds:
- repository **interfaces** (`ControllerRepository`, `DsuRepository`, `GamepadRepository`, …)
- **use cases** — one-line wrappers, invoked as `useCase(args)` via an `operator fun invoke`
- domain-only transforms (e.g. `SideInference`, `ComboAssignmentDetector`)

**`data`** — the repository **implementations** and everything they need: BLE/GATT, the UHID
and uinput relays, the DSU UDP server, byte/wire parsers, framework access. Depends only on its own domain.

**`presentation`** — the feature's `ViewModel` and Compose UI. Depends only on its own domain
(for the use cases + models) and `:core:designsystem`. Cannot see `data`.

**`:core:model`** — shared domain entities (`PlayerState`, `JoyconInput`, `JoyconButton`,
`Side`, `PlayerNumber`, `ConnectedJoycon`, …) and pure transforms over them (`BatteryGauge`,
`SidewaysMapper`, `GamepadState`). Depends on nothing of ours.

**`:core:designsystem`** — theme (Color, Dimens, Type) and generic, reusable composables
(`FeatureToggleCard`, `ExpandableInfoSection`, `CopyableCode`, `ErrorBox`, …). Anything
app-specific lives in a feature's presentation, not here.

**`:core:session`** — the cross-feature coordinator (`SessionCoordinator`) plus its session use
cases. It's the one place that depends on more than one feature's domain, because assembling the
app's `AppUiState` *is* the cross-feature concern (connection + assignment → player state).

**`:core:emulatorconfig`** — mechanism shared by the gamepad and DSU setup, which both write to
Dolphin and Eden: `IniEditor`, `DolphinPaths` / `EdenPaths`, and `EdenControls` (the `[Controls]`
keys both features write; each setup first clears every player's old keys). The generators
themselves live in their feature's `domain`. The config files sit in the emulator's `Android/data`,
writable by a shell-uid process (Shizuku), not by the app.

**`:core:buttonmapping`** — the user's Joy-Con → emulator mapping: model and layouts (`domain`,
shipped layouts in `preset/`), persistence (`data`) and the editor (`presentation`). See
[Button mapping](#button-mapping).

## Button mapping

- **Keyed by `PlayerBody`** — a player plus the body they hold (a pair or Pro, a lone Joy-Con, or a
  GameCube controller) — so each player maps independently.
- **A target holds every source bound to it.** Dolphin ORs them into one expression; Eden binds one
  input per key, so it keeps the first the body can emit.
- **A layout is a name for a set of bindings, never a stored reference.** Applying one copies out
  everything it says over a cleared mapping, so a target it omits is unbound (stored empty, since
  the console default fills any target the stored mapping lacks).
  `MappingLayouts.matching` reads the name back by comparing a body's bindings with every shipped
  layout and every one the user saved for that body; no match is "Custom". So deleting a layout
  removes only its name, and the same bindings answer to it again if an identical one is saved.
- **Android keys, keyboard keys and mouse actions are mapping targets too** (`ANDROID_HOME`,
  `KEYBOARD_W`, `MOUSE_LEFT_CLICK`, …), so layouts save them and a grip switch keeps them
  (`DEVICE_KEY_TARGETS`). They act in the app rather than the emulator, parsed into `KeyBindings`:
  `AndroidKeyRouter` and a `HeldKeyRouter` per kind sit between the coordinator and both outputs
  ([how](virtual-gamepad.md#android-keys)).
- **So is Eden's Pro Controller switch** for a lone Joy-Con (`PRO_CONTROLLER`), so layouts carry the
  grip with its bindings ([how](virtual-gamepad.md#single-joy-con)).
- **The Wii controller is not a binding** (`WiiController`: remote, sideways or Classic). Like
  Thrust and Sensitivity it is a per-player setting the layout seeds
  ([how](dsu-motion.md#wii-controller)).
- **`GlobalLayout` freezes the whole session** — every player's bindings in full, and the bodies they
  held — so it restores exactly what it saved, but only onto those players.

## Output

One **Output** card picks what runs. Each tab is one output, so its setup has one job.
`:feature:output` owns the choice; `:app` runs it.

| Tab (`OutputMode`) | Runs | Emulators offered | Set up writes |
|---|---|---|---|
| Virtual gamepad | pad | Dolphin (GameCube), Eden, Other | the gamepad config |
| Motion (DSU) | DSU | Dolphin (Wii), Eden, Other | the DSU config |
| Mouse & keys | keyboard, mouse | — | — |

| Emulator (`EmulatorKind`) | Console mapped |
|---|---|
| Dolphin (GameCube) | GameCube |
| Dolphin (Wii) | Wii Remote |
| Eden, Eden Nightly | Switch Pro |
| Other | `Console.ANDROID`: device keys only, shared by both tabs |
| (Mouse & keys) | `Console.PC`, WASD + mouse by default, or Mouse only |

- **`OutputPlan` is the one rule**, pure and tested: a tab's devices, plus the keyboard or mouse once
  the active mapping uses them (a keyboard or Android key for the keyboard, the Android keys' fast
  path; a mouse action for the mouse), and the console that mapping is.
- **No per-device overrides.** An emulator that reads gyro from DSU reads its buttons there too, so
  one device per tab covers it; Eden is on both tabs for the rest.
- **The emulator box is auto setup alone**: what the tab can set up, then **Other**
  (`EmulatorTarget.OTHER`, no package, so no Set up). Other is for Android games and emulators set up
  in their own settings.
- **The mapping that applies is the box's choice**, device keys included, so a layout carries its
  own keys (save-state F-keys for Dolphin, say) and Other has a keys-only mapping of its own.
- **Each tab keeps its own emulator** (`OutputSelection.emulators`), so switching tabs loses neither.
  Dolphin is two targets of one package (`dolphin-gamecube`, `dolphin-wii`).
- **One active console for every device.** The routers read one set of bindings per kind of key, which
  act while the output runs, whichever devices are up, unless paused (`DeviceKeysPause`, while the
  mapping editor listens for a press).
- **`:app` runs it.** `AppContainer` derives the planned devices; `OutputDevicesPolicy` starts the pad
  (with players to give it, again when one returns or Shizuku comes or goes) and DSU; `PrivilegedDevicePolicy` the keyboard
  and mouse, which also need Shizuku.
- **On as players arrive** (`OutputAutoStartPolicy`): the first player assigned switches it on.
  Switched off by hand, it stays off until every player leaves and one returns. The switch isn't kept
  across launches; the tab and each tab's emulator are (`OutputDataStore`).

## Dependency rules

These are the invariants. They're enforced by the module graph (compile-time) and, where the
graph can't reach, by `:konsist` tests.

- A feature's `presentation` and `data` **cannot see each other** — they share only that
  feature's `domain`. Every path between them crosses a use case:
  - data → presentation: `data` → repository interface (domain) → use case (domain) → ViewModel
  - presentation → data: ViewModel → use case (domain) → repository interface → `data`
- **Dependency inversion**: use cases take repository *interfaces* in their constructors; `data`
  supplies the implementations; `:app` is the only place that sees both and binds them.
- `domain` modules are pure Kotlin/JVM — no Android, no Compose, no sibling layer.
- **Features never depend on each other.** They meet only in `:app` (the connection →
  assignment → gamepad/dsu pipeline) and in `:core:session` (the coordinator).
- ViewModels live in a `presentation` (or `:app`) module and extend `(Android)ViewModel`. Use
  cases live in a `domain` (or `:core:session`) module. Repository abstractions are interfaces
  in a `domain` module. — these three are asserted by `:konsist`.

## Package layout

Packages are **feature-rooted**, mirroring the module split: `com.joegec.joycon2android.<feature>`
for a feature's domain + data, and `com.joegec.joycon2android.<feature>.presentation` for its
ViewModel + composables. A crowded package is split **by concern, not layer** into sub-packages
within the same module — e.g. `gamepad` (relay output) / `gamepad.privileged` (shell access) /
`gamepad.emulator` (emulator config), and `dsu` /
`dsu.motion` / `dsu.emulator`. Each module's `namespace` matches its package root so generated `R`
lands there. `:core:designsystem` solely owns `com.joegec.joycon2android.ui.components` / `ui.theme`
(the shared design system); no feature adds to those.

## Composition root — `AppContainer`

The only place abstractions and implementations meet. It:

- constructs each **repository implementation** (`Joycon2Manager`, `DsuServer`,
  `PlayerAssignmentManager`, `GamepadOutput`, `PrivilegedAccess`) as **app-scoped singletons**,
- binds each to its **use cases** (`StartScanUseCase(controllerRepository)`, …),
- owns the **`SessionCoordinator`**, wiring connection + assignment into the gamepad/DSU outputs,
- exposes a flat surface of use cases that `MainActivity` hands to each feature's ViewModel.

It's held by `JoyconApplication`, so connections and servers **outlive any Activity or the
foreground service**. The `Service` only manages the foreground-notification lifetime; it reads
no state of its own.

## ViewModels and the UI

One **ViewModel per feature**, in its presentation module, built in `MainActivity` by a
`viewModelFactory` that pulls use cases off `AppContainer` — so it depends only on its domain.

- `OutputViewModel` — the [output](#output): mode, emulator, overrides, the on switch and emulator
  auto setup.
- `DsuViewModel` — DSU status.
- `GamepadViewModel` — the virtual gamepad, keyboard and mouse status, and Shizuku availability.
- `UpdateViewModel` — the once-per-launch release check and the update prompt.
- `SettingsViewModel` — the settings that apply to whichever output runs (faster updates, the Eden
  motion block), **Show tips**, which `MainActivity` hands every screen through `LocalTips`, and
  which screen `EmulatorLauncher` starts an emulator on. The panel's layout choice stays with `Joycon2ViewModel`, which renders it.
- `ControllerMappingViewModel` (in `:core:buttonmapping:presentation`) — the button-mapping editor.
- `Joycon2ViewModel` (in `:app`) — the app-level host: the coordinator's session `uiState`
  (genuinely cross-feature), BLE permissions, scan/assign/disconnect, and the service binding.

`AppUiState` (in `:core:model`) is the `SessionCoordinator`'s output — the single immutable
snapshot the screen renders.

### Live input

`AppUiState` changes with every BLE packet, so `Joycon2ViewModel` hands the UI two streams instead:

| Stream | Holds | Changes when |
|---|---|---|
| `session` | `withoutLiveInput()`: everything but sticks, buttons, motion and packet id | who holds what, connection state, battery |
| `liveInputs` | each controller's `JoyconInput`, by address | every packet |

Only `PlayerControllerLayout` (the detailed view's controller graphics) collects `liveInputs`, so a
packet recomposes nothing else and compact view none at all. A new screen that draws input collects
it at the leaf the same way, never `session` with input put back.

`compose-stability.conf` marks `kotlin.collections` stable, so a composable taking `pressed: Set<String>`
skips when the set is equal rather than identical — each packet builds a new one. Pass a leaf only
what it draws (a battery row takes `BatteryCharge?`, not `JoyconInput`), or it recomposes with the IMU.

## Data & control flow

```
BLE notify ─→ Joycon2Manager (connection/data, ControllerRepository)
                   │
                   ▼
            SessionCoordinator (core/session) ── combines connections + assignments
                   │  onState(state)
                   ├─→ MouseRouter (pointer from Joy-Cons on a surface; default clicks if unmapped) ─→ MouseMix
                   │      └─→ AndroidKeyRouter (withholds Android-key buttons; keys the pad lacks ─→ keyboard, else shell)
                   │             └─→ HeldKeyRouter × 2 (keyboard keys, mouse actions; withholds their sources)
                   │                    ├─→ PushKeyboardFrameUseCase ─→ KeyboardOutput ─→ UHID relay
                   │                    ├─→ MouseMix ─→ PushMouseFrameUseCase ─→ MouseOutput ─→ UHID relay
                   │                    ├─→ PushGamepadStateUseCase  ─→ GamepadOutput  ─→ uinput relay ─→ /dev/uinput
                   │                    └─→ PushDsuPadDataUseCase    ─→ DsuServer     ─→ UDP :26760 ─→ emulators
                   └─→ ObserveSessionUseCase   ─→ Joycon2ViewModel ─→ AppUiState ─→ Compose
```

The gamepad and DSU outputs ride the coordinator's **synchronous** `onState` callback, not a
conflated `StateFlow`, which would drop motion samples. Hardware detail:
[virtual-gamepad.md](virtual-gamepad.md), [dsu-motion.md](dsu-motion.md).

## Build & test

```bash
./gradlew :app:assembleDebug          # build the app
./gradlew test                        # all unit tests, every module
./gradlew :konsist:test               # architecture-rule tests only
./gradlew :feature:dsu:data:test      # one module's tests
```
