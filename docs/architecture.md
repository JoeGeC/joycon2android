# Architecture

The living reference for how this app is structured. For *how to add or change* a feature,
see [adding-a-feature.md](adding-a-feature.md).

## Shape in one paragraph

A single-activity Compose app built as a **Gradle multi-module** project, split by **feature ×
layer**. Each feature (`connection`, `assignment`, `gamepad`, `dsu`) has up to three modules —
`domain`, `data`, `presentation` — plus shared `:core` modules and a thin `:app` that wires
everything together. The split exists to *enforce* the dependency rules at compile time: a
ViewModel physically cannot reach a repository implementation, because presentation and data
live in separate modules that share only domain.

## Module graph

| Module | Plugin / type | Depends on |
|---|---|---|
| `:core:model` | `joycon.kotlin.jvm` | — |
| `:core:designsystem` | `joycon.android.library.compose` | — |
| `:core:session` | `joycon.kotlin.jvm` | `:core:model`, `:feature:connection:domain`, `:feature:assignment:domain` |
| `:core:emulatorconfig` | `joycon.kotlin.jvm` | — |
| `:feature:<f>:domain` | `joycon.kotlin.jvm` | `:core:model` ² |
| `:feature:<f>:data` | `joycon.android.library`¹ | `:feature:<f>:domain`, `:core:model` |
| `:feature:<f>:presentation` | `joycon.android.library.compose` | `:feature:<f>:domain`, `:core:designsystem`, `:core:model` |
| `:app` | `com.android.application` | every feature module + all `:core` |
| `:konsist` | `joycon.kotlin.jvm` (test-only) | — (scans the whole project) |

…for each feature `<f>` ∈ { `connection`, `assignment`, `gamepad`, `dsu` }.

¹ `assignment:data` is pure Kotlin (`joycon.kotlin.jvm`) — it has no Android dependencies.

² `gamepad:domain` and `dsu:domain` also depend on `:core:emulatorconfig` for the one-tap
emulator setup (shared ini editing + Dolphin paths). Each feature owns its own emulator-config
*generators* — gamepad mapping in `gamepad:domain`, DSU/motion mapping in `dsu:domain` — over that
shared leaf; no feature depends on another feature.

The three convention plugins live in `build-logic/convention/` and dedupe all the per-module
Gradle config (compileSdk, Java 11, Compose, test options) so each `build.gradle.kts` is a few
lines. See [adding-a-feature.md](adding-a-feature.md) for what each plugin sets up.

## The layers

**`domain`** — pure Kotlin/JVM. No Android, no Compose, no other layer. Holds:
- repository **interfaces** (`ControllerRepository`, `DsuRepository`, `GamepadRepository`, …)
- **use cases** — one-line wrappers, invoked as `useCase(args)` via an `operator fun invoke`
- domain-only transforms (e.g. `SideInference`, `ComboAssignmentDetector`)

**`data`** — the repository **implementations** and everything they need: BLE/GATT, the UHID
relay, the DSU UDP server, byte/wire parsers, framework access. Depends only on its own domain.

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

**`:core:emulatorconfig`** — emulator-agnostic primitives for the one-tap setup: `IniEditor`
(read → splice → write of ini config text, leaving the user's other keys intact) and `DolphinPaths`
(Dolphin's package + config-file locations, shared because both the gamepad and DSU features write
to Dolphin). Holds *mechanism*, not feature logic — the per-emulator config generators live in
their owning feature's `domain`. Depends on nothing of ours.

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
`dsu.motion` / `dsu.dolphin`. Each module's `namespace` matches its package root so generated `R`
lands there. `:core:designsystem` solely owns `com.joegec.joycon2android.ui.components` / `ui.theme`
(the shared design system); no feature adds to those.

## Composition root — `AppContainer`

`app/.../AppContainer.kt` is the only place the abstractions and implementations meet. It:

- constructs each **repository implementation** (`Joycon2Manager`, `DsuServer`,
  `PlayerAssignmentManager`, `GamepadOutput`, `PrivilegedAccess`) as **app-scoped singletons**,
- binds each to its **use cases** (`StartScanUseCase(controllerRepository)`, …),
- owns the **`SessionCoordinator`**, wiring connection + assignment into the gamepad/DSU outputs,
- exposes a flat surface of use cases that `MainActivity` hands to each feature's ViewModel.

It's held by `JoyconApplication`, so connections and servers **outlive any Activity or the
foreground service**. The `Service` only manages the foreground-notification lifetime; it reads
no state of its own.

## ViewModels and the UI

One **ViewModel per feature**, each in its own presentation module, constructed in
`MainActivity` via `viewModelFactory { initializer { … } }` that pulls the relevant use cases
off `AppContainer`. This keeps the ViewModel class dependent only on its domain — never on
`:app`.

- `DsuViewModel` — DSU status + enable toggle.
- `GamepadViewModel` — gamepad status + Shizuku availability.
- `Joycon2ViewModel` (in `:app`) — the app-level host: the coordinator's session `uiState`
  (genuinely cross-feature), BLE permissions, scan/assign/disconnect, and the service binding.

`AppUiState` (in `:core:model`) is the `SessionCoordinator`'s output — the single immutable
snapshot the screen renders.

## Data & control flow

```
BLE notify ─→ Joycon2Manager (connection/data, ControllerRepository)
                   │
                   ▼
            SessionCoordinator (core/session) ── combines connections + assignments
                   │  onState(state)
                   ├─→ PushGamepadStateUseCase ─→ GamepadOutput ─→ UHID relay ─→ /dev/uhid
                   ├─→ PushDsuPadDataUseCase   ─→ DsuServer    ─→ UDP :26760 ─→ emulators
                   └─→ ObserveSessionUseCase   ─→ Joycon2ViewModel ─→ AppUiState ─→ Compose
```

The gamepad and DSU outputs ride a **synchronous per-packet path** off the coordinator's
`onState` callback, not a conflated `StateFlow` — conflation would drop motion samples. See the
DSU and UHID sections of the [README](../README.md#architecture) for the hardware-level detail.

## Build & test

```bash
./gradlew :app:assembleDebug          # build the app
./gradlew test                        # all unit tests, every module
./gradlew :konsist:test               # architecture-rule tests only
./gradlew :feature:dsu:data:test      # one module's tests
```

`:konsist` enforces the layer-placement rules described above; run it after moving classes
between modules.
