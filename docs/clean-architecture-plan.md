# Clean Architecture Refactor — Plan

The app has grown to ~90 files split by *technical concern* (`ble/`, `dsu/`, `uhid/`,
`model/`, `ui/`). The boundaries have blurred: `model/` mixes domain entities with
presentation state, `domain/` holds an Android-flavoured state aggregator, the service
hand-wires every collaborator, and one `Joycon2ViewModel` + one `JoyconScreen` serve
every feature. This plan reorganises into **feature modules, each with presentation /
domain / data layers**.

## Goals

- A reader can open `feature/gamepad/` and see the whole feature: its UI, its state, its
  logic, its system integration — without hopping across five top-level packages.
- Strict dependency direction: presentation → domain ← data. Domain knows nothing about
  Android, BLE, sockets, or Compose.
- Features don't depend on each other; they share only `core`. Cross-feature data flow is
  orchestrated at the app layer.
- **Every presentation → data call routes through a domain use case**, including thin
  passthroughs (`StartScanUseCase`, `DisableDsuUseCase`). The rule is absolute so the
  dependency direction is never ambiguous: a ViewModel never touches a repository
  directly. The cost is boilerplate; the benefit is one obvious seam per action.

## Module strategy

**Gradle multi-module**, each feature split into `domain` / `data` / `presentation`
modules. This is the only way to *enforce* the use-case rule: presentation and data are
separate modules that each depend only on domain, so neither can import the other — the
compiler rejects any path from a ViewModel to a repository that skips a use case. Packages
in one module couldn't prevent it.

### Module graph

| Module | Type | Depends on |
|---|---|---|
| `:core:model` | Kotlin/JVM | — |
| `:core:designsystem` | Android library (Compose) | — |
| `:feature:<f>:domain` | Kotlin/JVM | `:core:model` (+ coroutines) |
| `:feature:<f>:data` | Android library | `:feature:<f>:domain`, `:core:model` |
| `:feature:<f>:presentation` | Android library (Compose) | `:feature:<f>:domain`, `:core:designsystem`, `:core:model` |
| `:app` | Android application | every feature module + all `:core` |

…for each feature `<f>` ∈ { `connection`, `assignment`, `gamepad`, `dsu` }. That's 15
library modules + `:app`. The cost is real (more build files, slower cold build, ceremony
when adding a class), bought deliberately for hard isolation and enforced boundaries. A
small `build-logic` convention plugin will dedupe the per-module Gradle config
(compileSdk, Kotlin/Compose setup) so each module's `build.gradle.kts` is a few lines.

### Feature boundaries

- **connection** — scan + BLE connect; the per-controller display UI.
- **assignment** — controllers → players (P1–P4), combos, sideways/pro resolution.
- **gamepad** — virtual HID output **and** the privileged access it needs (Shizuku +
  wireless-debugging/ADB). Privileged access stays inside gamepad rather than a separate
  feature, because nothing else uses it — so its "clear gamepad error on connect / disable
  on link loss" interactions are *intra*-feature, not cross-module wiring. (If a second
  consumer ever appears, the `PrivilegedShell` abstraction graduates to `:core`.)
- **dsu** — cemuhook motion server.

## Dependency rules

- A feature's `presentation` and `data` modules **cannot see each other** — they share
  only that feature's `domain`. Data flow up (data → presentation) goes data → repository
  interface (domain) → use case (domain) → ViewModel; control flow down goes ViewModel →
  use case → repository. Both directions cross domain, as required.
- `domain` modules are pure Kotlin/JVM — no Android, no Compose, no other layer. They hold
  domain models, repository *interfaces*, and use cases.
- **Dependency inversion**: use cases take repository interfaces in their constructors;
  `data` provides the implementations; `:app` is the composition root that sees both and
  binds them (`AppContainer`). This is why `:app` depends on each feature's `data`.
- Features never depend on each other; `:app` is the only place they meet (the
  connection → assignment → gamepad/dsu pipeline, via a session coordinator).
- `:core:model` and `:core:designsystem` depend on nothing of ours.
- **Repositories/data sources are app-scoped singletons** in `AppContainer` (held by
  `Application`), so connections/servers survive the Activity without living in the
  `Service`. ViewModels are feature-scoped and depend only on their feature's use cases.

## Where each current file goes

### core
| Current | → |
|---|---|
| `model/JoyconInput`, `JoyconButton`, `Side`, `PlayerNumber`, `ConnectedJoycon`, `JoyconConnectionState`, `PlayerState`, `GamepadState`, `SidewaysMapper`, `BatteryGauge` | `core/model/` |
| `ui/theme/*` (Color, Dimens, Theme, Type) | `core/designsystem/theme/` |
| `ui/components/` generics: `CopyableCode`, `ExpandableInfoSection`, `ErrorBox`, `FeatureToggleCard`, `IconButton`, `SmallButton`, `ScanningIndicator`, `BatteryPill` | `core/designsystem/component/` |

`GamepadState`/`SidewaysMapper`/`BatteryGauge` are pure transforms over domain entities, so
they stay domain — `core/model` keeps them shared without a feature owning them.

### feature/connection
| Current | → | Layer |
|---|---|---|
| `ble/BleScanner`, `ConnectionPool`, `JoyconConnection`, `GattOpQueue`, `Joycon2Manager` | `connection/data/` | data |
| `ble/PacketParser`, `SpiColorParser`, `JoyconAdvertisement` | `connection/data/` | data (byte/wire) |
| `ble/BlePermissionHandler` | `connection/data/` | data (framework) |
| `domain/SideInference` | `connection/domain/` | domain |
| (new) `ControllerRepository` interface | `connection/domain/` | domain |

### feature/assignment
| Current | → | Layer |
|---|---|---|
| `domain/PlayerAssignmentManager`, `ComboAssignment`, `ComboAssignmentDetector`, `ControllerAssigner`, `PlayerStateResolver` | `assignment/domain/` | domain |
| `domain/UiStateAggregator` | `app/` (cross-feature orchestrator) or `assignment/domain` | see note |
| `model/AppUiState` | `app/presentation/` (composite screen state) | presentation |

`UiStateAggregator` joins connection + assignment to build the player list; it's
cross-feature, so it belongs to the app-level session orchestrator, not inside one
feature.

### feature/gamepad
| Current | → | Layer |
|---|---|---|
| `uhid/UhidRelay`, `GamepadManager`, `ReportMapper` | `gamepad/data/` | data |
| `uhid/PrivilegedShell`, `ShizukuShell`, `ShizukuPermissionHandler`, `PrivilegedAccess` | `gamepad/data/privileged/` | data |
| `adb/*` (AdbShell, AdbConnectionManager, AdbPairingNotification, AdbPairingReceiver, WirelessDebuggingSettings) | `gamepad/data/privileged/adb/` | data |
| `uhid/GamepadOutput` | `gamepad/domain/` | domain (orchestrates enable/report) |
| `ui/components/AdbSetupCard`, `AdbSetupState`, `DsuCard`? no | `gamepad/presentation/` | presentation |

Privileged access lives under gamepad because nothing else needs it (DSU is
Shizuku-free). If a second consumer ever appears, promote it to `core`.

### feature/dsu
| Current | → | Layer |
|---|---|---|
| `dsu/DsuServer`, `DsuPacketEncoder`, `DsuRequest`, `DsuRequestParser`, `DsuClientRegistry`, `LanAddress` | `dsu/data/` | data |
| `dsu/MotionConverter`, `GyroCalibrator`, `DsuMotion` | `dsu/domain/` | domain |
| `ui/components/DsuCard`, `DsuCardState` | `dsu/presentation/` | presentation |

### app
| Current | → |
|---|---|
| `MainActivity` | `app/` |
| `service/Joycon2Service` (slimmed), `Joycon2Notification`, `PartialWakeLock` | `app/service/` |
| (new) `AppContainer` | `app/` (composition root; owns app-scoped repositories + use cases) |
| `ui/Joycon2ViewModel` | deleted — replaced by per-feature ViewModels (Phase 4) |
| `ui/JoyconScreen` | `app/presentation/` (composes feature sections) |
| controller UI: `ui/components/PlayerView`, `ControllerLayout`, `DualJoyconLayout`, `ProControllerLayout`, `SidewaysLeftLayout`, `SidewaysRightLayout`, `JoyconCard`, `StickCard`, `ImuDisplay`, `SidewaysImuDisplay`, `ControllerButtons`, `FaceButtons`, `DPad`, `ShoulderButton`, `RailButton` | `connection/presentation/` |
| `ui/components/AssignmentPanel` | `assignment/presentation/` |

### Where the service belongs

`Joycon2Service` currently does four jobs: framework entry point (lifecycle, foreground
notification, wake lock), composition root, UI facade (its binder exposes StateFlows to
the ViewModel), and process-lifetime host for the data objects. Clean architecture treats
`Service`/`Activity`/`BroadcastReceiver` as **framework entry points** at the outermost
layer — so the `Service` *class* is app-layer, like `MainActivity`. But the data work it
hosts moves out:

- BLE manager, DSU server, gamepad output, privileged access → app-scoped **data**
  singletons owned by `AppContainer`.
- Composition → `AppContainer`.
- UI facade → removed; ViewModels observe repositories via use cases, not the binder.
- What remains in the Service: keep the process alive + foreground notification while
  controllers are connected (it observes the connection repository to decide). That, and
  the wake lock, are the only things that genuinely need to be a `Service`.

`AdbPairingReceiver` is likewise a framework entry point → `app/service/` (or
`gamepad/.../adb/`), forwarding the code to a use case.

## New abstractions to introduce

- **Repository interfaces** (each feature's `domain`), implemented in `data`:
  - `ControllerRepository` — `connections: StateFlow`, scan/stop/disconnect.
  - `GamepadRepository` — enable/disable, status, the privileged-shell acquisition.
  - `WirelessDebugRepository` — pairing/connection state, start pairing, observe link.
  - `DsuRepository` — start/stop, client count, LAN toggle.
  ViewModels never see these; only use cases do.
- **Use cases — full coverage** (one per action a ViewModel can trigger, plus the
  logic-bearing ones). Thin passthroughs (`StartScanUseCase`, `AssignControllerUseCase`,
  `EnableDsuUseCase`, `SetDsuLanUseCase`, `StartPairingUseCase`) are deliberately kept so
  presentation never bypasses domain. Logic-bearing ones keep their substance:
  `ResolvePlayerStateUseCase`, `DetectAssignmentCombosUseCase`, `EnableVirtualGamepadUseCase`
  (acquire-shell + create-relays, currently in `GamepadOutput`), the motion pipeline
  (`MotionConverter` ∘ `GyroCalibrator`). Group a feature's use cases in one file/package
  to keep the boilerplate navigable.
- **`AppContainer`** (app) — manual DI held by `Application`. Constructs the app-scoped
  repositories + use cases once; the `Service` and the ViewModels both pull from it. No DI
  framework needed at this size. Replaces `Joycon2Service.buildCollaborators()`.
- **One ViewModel per feature** — `ControllerViewModel`, `AssignmentViewModel`,
  `GamepadViewModel`, `DsuViewModel`, `WirelessDebugViewModel`, each exposing its own
  `*UiState` and taking its feature's use cases from the container (via a
  `ViewModelProvider.Factory`). This is the deliberate setup for the upcoming UI/UX work:
  each feature's screen state can evolve independently. The catch-all `AppUiState` +
  `Joycon2ViewModel` are retired; `JoyconScreen` becomes a composer that hosts each
  feature's ViewModel and section composable.
- **App session coordinator** (app/domain) — runs the cross-feature pipeline
  (connection → assignment → gamepad/dsu push) at app scope, replacing the `onState`
  callback in `UiStateAggregator`. Started by `AppContainer`, independent of the Service.

## Phased migration

Each phase ends green: `./gradlew assembleDebug testDebugUnitTest` passes and the app runs
on device. Phases 2–3 proceed **one feature at a time**, so a regression is always
isolated to the feature in flight and checked against the traceability table before moving
on.

1. **Scaffold** — add a `build-logic` convention plugin for the shared module config;
   create `:core:model` (Kotlin/JVM) and `:core:designsystem` (Compose); move the shared
   models, theme, and generic components into them; point `:app` at both. No behaviour
   change.
2. **Feature domain + data** (per feature) — create `:feature:<f>:domain` (repository
   interfaces + full use-case set + domain models) and `:feature:<f>:data` (move the BLE /
   DSU / UHID / ADB sources here, implementing the interfaces). Stand up `AppContainer` in
   `:app` to bind impls → use cases. The still-monolithic `Joycon2ViewModel` calls use
   cases instead of the service binder. Behaviour unchanged; this inserts the enforced
   domain seam.
3. **Feature presentation** (per feature) — create `:feature:<f>:presentation`; move the
   feature's composables; add its `*ViewModel` + `*UiState`. `JoyconScreen` (in `:app`)
   becomes a composer hosting each feature section. `Joycon2ViewModel` and `AppUiState`
   are deleted once the last feature lands.
4. **Slim the shell** — replace the service binder facade with the `AppContainer` +
   session coordinator; the `Service` keeps only lifetime + foreground + wake lock. The
   module graph already enforces the dependency rules; optionally add a Konsist test as
   belt-and-braces and CI documentation.

Phase 1 is mechanical. Phases 2–4 are the real design work; the module boundaries make
each step's mistakes loud (a forbidden import won't compile) rather than silent.

## Functionality traceability

Splitting one ViewModel/Screen into five is where behaviour gets silently dropped. The
safeguard: this capability inventory is the migration's acceptance checklist. Every row
must have a working home (feature · use case · ViewModel state) before its phase is
"done", verified by unit tests (which move with the code) plus the on-device smoke pass.

| Capability (today) | Target feature |
|---|---|
| Scan / stop scan; BLE permission grant/deny/recheck; `permissionDenied` | connection |
| Connected list, scanning + error state (`AppUiState` minus players) | connection |
| Disconnect one / disconnect all | connection (+ app for "all") |
| Per-controller display: sticks, IMU, battery, buttons, sideways/pro/dual layouts | connection presentation |
| Assign / unassign to P1–P4; `players` list | assignment |
| Combo auto-assign (L+R pair, SL+SR solo, Pro L+R) | assignment |
| Player-state resolution (evict conflicting) | assignment |
| Player LED set on assignment | assignment → connection (LED write) |
| Enable / disable virtual gamepad; `gamepadEnabled`, `gamepadError` | gamepad |
| Privileged path auto-select (Shizuku → ADB) | gamepad |
| Gamepad error clears on ADB connect; disables on ADB loss | gamepad ↔ wireless-debug |
| Wireless-debug: pairing-arm, mDNS pairing+connect discovery, code notification, auto-reconnect, revoke detection; `adbState`, `adbError`, `adbSetupNeeded` | wireless-debug |
| POST_NOTIFICATIONS request gating | wireless-debug presentation |
| DSU enable/disable, LAN toggle, client count, P1–4 slot routing, gyro bias, motion frame; `dsuEnabled/Error/ClientCount/LanEnabled` | dsu |
| Cross-feature push pipeline (players → gamepad + dsu) | app session coordinator |
| Foreground notification only while connected; wake lock; `stopService` | app/service |

A row that spans two features (e.g. gamepad-error-clears-on-adb-connect) becomes an
explicit collaboration at the app/session layer, not a hidden dependency between features.

## Decisions (resolved)

- **Gradle multi-module**, per feature × layer (`domain`/`data`/`presentation`), so the
  use-case rule is compiler-enforced, not conventional.
- **Full, bidirectional use-case coverage** — presentation reaches data only through use
  cases, and data surfaces to presentation only through use cases (via repository
  interfaces in domain). No layer skips the domain.
- **One ViewModel per feature**, to give the upcoming UI/UX work room to move each
  feature's screen state independently.

- **`build-logic` convention plugins from the start** (`joycon.kotlin.jvm`,
  `joycon.android.library`, `joycon.android.library.compose`).
- **`:core:session` module** for the cross-feature coordinator, so it's reusable from a
  future headless entry point, not bolted to `:app`.

### Migration note: keep package names stable

Modules are a *build* boundary; code keeps its existing package (e.g. model classes stay
`com.joegec.joycon2android.model` in `:core:model`). So relocating a file into a module is
mostly a file move plus a Gradle dependency — imports across `:app` don't change. The
exception is Android library modules with resources: their `R` is per-namespace, so any
moved composable using `R.string.…` takes its string resources with it and switches to the
module's `R`.

## Open decisions

- `AdbPairingReceiver` home — settled: stays in `:app`. It (and `AdbPairingNotification`)
  bridge to `Joycon2Service`, so they're framework entry points. `PrivilegedAccess` (data)
  no longer touches the notification; it exposes `pairingServiceAvailable` and the service
  shows/cancels the notification.

---

## Migration progress (log)

**Status: Phases 1–2 complete (all four features extracted) AND the session coordinator
(2b). 12 modules, everything builds green; app installs. The dependency rule is now
compiler-enforced end to end.** Remaining: Phase 3 (presentation modules + per-feature
ViewModels), Phase 4 (optional Konsist test). NOT yet device-tested since 2a.

### Done
- **build-logic** included build with convention plugins: `joycon.kotlin.jvm`,
  `joycon.android.library`, `joycon.android.library.compose`.
- **`:core:model`** (JVM) — all model classes, package kept `…model`. `BatteryGaugeTest`.
- **`:core:designsystem`** (Android/Compose) — theme + generic components (`CopyableCode`,
  `ErrorBox`, `ExpandableInfoSection`, `FeatureToggleCard`); `copy_label` string; namespace
  `com.joegec.joycon2android.core.designsystem`.
- **`:feature:dsu:{domain,data}`** — `DsuRepository` + full use cases; `DsuServer` impl.
  Introduced **`AppContainer`** + **`JoyconApplication`** here.
- **`:feature:assignment:{domain,data}`** — `AssignmentRepository` + `PlayerAssignmentManager`
  impl; `ComboAssignmentDetector`/`PlayerStateResolver`/`SideInference` in domain. No use
  cases yet — its actions (assign/unassign) carry cross-feature side effects (LED, gamepad
  lifecycle), so they land with the coordinator.
- **`:feature:gamepad:{domain,data}`** — `GamepadRepository` + `WirelessDebugRepository` +
  11 use cases; all UHID/Shizuku/ADB in data (owns those deps). Notification decoupled via
  `pairingServiceAvailable`. `AppContainer` gained a `Context`. `GamepadOutput.enable(players)`
  no longer reaches into UI state.
- **`:feature:connection:{domain,data}` (2a)** — `ControllerRepository` (exposes assembled
  `controllers: StateFlow<List<ConnectedJoycon>>`) + scan/disconnect use cases. `Joycon2Manager`
  implements it and assembles the `ConnectedJoycon` list from per-connection flows (moved out
  of the aggregator), keeping `JoyconConnection` internal to data. Error strings moved into the
  module. `BlePermissionHandler` stays in `:app`. The app's `UiStateAggregator` now consumes
  `controllers` + assignments and evicts disappeared controllers; `ControllerAssigner` uses
  `ControllerRepository.setPlayerLed`. Per-packet path preserved (controllers re-emit per input).

### Conventions / AGP-9 gotchas learned
- **Keep package names stable** across module moves → near-zero import churn; split packages
  across modules are tolerated on Android.
- AGP 9.2 has **built-in Kotlin** — do NOT apply `org.jetbrains.kotlin.android` (collides on
  the `kotlin` extension); use **`com.android.build.api.dsl.LibraryExtension`**.
- Android-library convention sets `testOptions.unitTests.isReturnDefaultValues = true`.
- Domain modules are pure JVM: `api(project(":core:model"))` + `api(libs.kotlinx.coroutines.core)`.

- **`:core:session` (2b, done)** — `SessionCoordinator` absorbed `UiStateAggregator` +
  `ControllerAssigner`; produces `AppUiState` (reused from `:core:model`); gamepad/DSU push +
  lifecycle injected as callbacks so the module depends only on feature domains. Use cases:
  `ObserveSession`, `AssignController`, `UnassignController`. `AppContainer` owns/starts it and
  has `disconnectAll()`. The service is now thin (wake lock + foreground + pairing notification
  + intent actions); the ViewModel reads all state from the container and binds the service
  only for lifetime. Old `UiStateAggregator`/`ControllerAssigner` deleted.

### Still in `:app`
`AppUiState` (presentation model, in `:core:model` for now — Phase 3 decomposes per feature),
`AdbPairingNotification` + `AdbPairingReceiver` (framework entry points), `BlePermissionHandler`.

### Phase 3a complete — all presentation modules extracted (16 modules total)
- **dsu:presentation** — `DsuCard`; data leak cleaned (`DsuConfig.PORT` domain constant +
  `DsuStatus.address` computed in `DsuServer`).
- **gamepad:presentation** — `AdbSetupCard` + `WirelessDebuggingSettings` (Settings-intent
  helper, moved out of data); `adb_pairing_*` notification strings stayed in `:app`.
- **connection:presentation** — the 18 controller-display composables. `PlayerView` is the
  public entry point; `SmallButton`/`ControllerIconButton`/layouts stay `internal`.
- **assignment:presentation** — `AssignmentPanel` (public).
- `ScanningIndicator` stays in `:app` (screen scaffolding, shares `scanning_hint`). Strings
  followed composables into per-module resources (`player_label` duplicated in connection +
  assignment). Composables still fed by the single `Joycon2ViewModel` via `JoyconScreen`.

### Phase 3b — per-feature ViewModels (in progress)
Pattern (proven on dsu): the feature `ViewModel` lives in its presentation module with use
cases injected via the constructor; `MainActivity` instantiates it with `by viewModels {
viewModelFactory { initializer { DsuViewModel(container.observeDsuStatus, …) } } }`. The
ViewModel `stateIn`s the use-case flow and exposes feature actions. `JoyconScreen` stays
param-based (fed from the feature VM, not the god VM) — low-risk, no screen surgery. The
feature's state/actions are removed from `Joycon2ViewModel`.
- **`DsuViewModel` (done)** in `:feature:dsu:presentation`. Needs `lifecycle-viewmodel-ktx`.
- **`GamepadViewModel` (done)** in `:feature:gamepad:presentation` — owns gamepad status +
  wireless-debug status + `adbSetupNeeded` (a static capability value injected at construction)
  + `toggle(enabled, players)` / `startAdbPairing()`. `toggle` takes the player list (the screen
  already has it from session state), avoiding a cross-feature dependency in the VM.

Remaining (optional):
- **`Joycon2ViewModel` is now the slim app-level host** for connection/assignment/session: the
  coordinator's `uiState` (cross-feature), BLE permissions, scan/assign/disconnect, and service
  binding. No longer a god VM. Split into thin connection/assignment VMs only if desired — the
  composite session state is genuinely app-level. `AppUiState` stays (it's the coordinator output).
### Phase 4 — Konsist dependency-rule test (done)
Dedicated pure-JVM `:konsist` module (`joycon.kotlin.jvm` + junit + `com.lemonappdev:konsist`)
with `ArchitectureTest`. Konsist scans files (not the classpath), so one module covers the whole
project via `Konsist.scopeFromProject()`. The rules enforce *where a kind of class may live* —
what the Gradle module graph can't catch on its own:
- `*ViewModel` classes live in a `/presentation/` or `/app/` module and extend `(Android)ViewModel`.
- `*UseCase` classes live in a `/domain/` (or `/core/session/`) module and expose an `invoke` operator.
- `*Repository` abstractions are interfaces in a `/domain/` module.

5 tests, all green. List extensions come from `com.lemonappdev.konsist.api.ext.list.*`. Runs under
`./gradlew :konsist:test` (and the aggregate `check`).

### Older notes
1. **Phase 3** — presentation modules + per-feature ViewModels; retire `Joycon2ViewModel`
   / `AppUiState`. Move `DsuCard`→`dsu:presentation` (clean up its `DsuServer.PORT` /
   `LanAddress` refs via domain), `AdbSetupCard`→`gamepad:presentation`, controller-display
   UI → `connection:presentation`, `AssignmentPanel`→`assignment:presentation`.
3. **Phase 4** — slim the service further; optional Konsist dependency-rule test.

### Verify on device (carried over)
Gamepad rewiring (Shizuku enable + ADB pairing notification) isn't covered by unit tests —
confirm both still work before building further on the gamepad feature.
