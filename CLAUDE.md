# Project Guidelines

## Language & Frameworks
- Kotlin, Jetpack Compose, Material 3
- Target: Android API 24+ (minSdk 24, targetSdk 36)
- BLE (BluetoothGatt) for Joy-Con 2 communication

## Code Style
- Follow current Android, Kotlin, and Compose conventions
- Boy Scout Rule: leave code better than you found it
- One class per file
- Code should read like well-written prose
- Methods should be short enough that they explain themselves
- Methods and composables should be reusable like components

## Comments
- Default to **no comment**: a well-named class or method is its own documentation
- Never write a comment that restates the code, the signature, or what the next line does — if a comment can be made redundant by renaming or extracting, do that instead
- A genuine "why" (a constraint the code cannot express, e.g. "StateFlow conflation requires a synchronous callback") gets one or two lines, never a paragraph
- New classes get **no KDoc by default**; earn it only with a non-obvious "why"
- **Three lines is the ceiling.** If a comment is outgrowing that, it has stopped explaining the
  line in front of it and started explaining the subject — a derivation, a measurement, a byte
  layout, an emulator's behaviour, why two other approaches failed. That belongs in
  [`docs/`](docs/README.md), with a one-line pointer where the code needs it:
  `/** … : docs/dsu-motion.md#motion-frame */`
- Physical-world facts still can't be derived from code — BLE protocol, byte layouts, timing
  constraints, hardware conventions being mirrored — so write them down properly, in the doc that
  owns them (`protocol.md`, `virtual-gamepad.md`, `dsu-motion.md`, `DESIGN.md`, `architecture.md`)
  rather than at the top of whichever class happened to need them first
- **One home per fact.** A comment and a doc saying the same thing will drift, and the stale one is
  found only once it has misled someone
- Keep in code only what a reader needs *at that line* and cannot reconstruct from it: a byte's
  meaning in a descriptor, a constant's unit, what a workaround is working around
- Litmus test before writing any comment: "could a reader reconstruct this from the code alone?" If yes, delete it

## SOLID Principles
- **Single Responsibility:** each class has one reason to change — if you need "Manager" or "Handler" in the name, it's probably doing too much
- **Open/Closed:** add behaviour through new classes, not by editing existing ones
- **Liskov Substitution:** subtypes must be interchangeable with their parent
- **Interface Segregation:** prefer small, focused interfaces over broad ones
- **Dependency Inversion:** depend on abstractions, not concretions; inject dependencies via constructors

## Clean Architecture
- Split responsibilities into focused collaborators, not regions within a single class
- An orchestrator should delegate, not implement — keep it under ~100 lines
- Each class should be independently testable
- Identify boundaries by asking "what changes for what reason?"
- If a class exceeds ~200 lines or has multiple clusters of private fields serving different concerns, it needs splitting

## Architecture
- Single-activity Compose app, **Gradle multi-module** split by feature × layer
  (`domain` / `data` / `presentation`), over `:core` modules and a thin `:app` composition root
- State flows from BLE layer → `SessionCoordinator` → `AppUiState` → per-feature ViewModel → Compose
- Presentation reaches data **only through use cases** (`operator fun invoke`); the module graph
  enforces it — never bypass it
- GATT operations are queued (Android allows only one at a time)
- **Read [`docs/architecture.md`](docs/architecture.md) before changing structure**, and follow
  [`docs/adding-a-feature.md`](docs/adding-a-feature.md) when adding/changing a feature
- Run `./gradlew :konsist:test` after moving classes between modules — it enforces layer placement
- Read `README.md` for project context, and [`docs/`](docs/README.md) for the BLE protocol (`protocol.md`),
  the virtual gamepad (`virtual-gamepad.md`) and DSU motion (`dsu-motion.md`)

## Conventions
- Use `enableEdgeToEdge()` with `WindowInsets.systemBars` for edge-to-edge inset handling
- **User-facing strings never live in a domain module.** Those modules are pure Kotlin/JVM and
  cannot see `R.string` at all, so a name in one is a name that can never be translated or reworded
  without touching logic. A domain type carries its *identity* — the enum entry, the id it is stored
  under — and presentation gives it a word, through an exhaustive `when` over the type so that
  adding a case without a word fails to build (see `MappingLabels` / `LayoutLabels`)
- Two things that look like copy but are not, and stay: **wire tokens** an emulator or protocol must
  match exactly (`DolphinControls.DIRECTIONS`, `EdenControls`), and the **markings printed on the
  hardware** that the controller graphics draw (`JoyconButton.label` — "ZL", "+", "A" are the same
  in every language, and are part of the picture rather than prose)
- Avoid hard-coded strings elsewhere too — use string resources where possible
- Use theme for dimensions and colors rather than inline literals
- Prefer immutable data classes for state
- Use `@SuppressLint("MissingPermission")` only on methods guarded by the permission launcher
- Deprecated BLE APIs (`.value =` pattern) used intentionally for broad compatibility (API 24+)
