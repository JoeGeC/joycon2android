# Project Guidelines

## Language & Frameworks
- Kotlin, Jetpack Compose, Material 3
- Target: Android API 24+ (minSdk 24, targetSdk 36)
- BLE (BluetoothGatt) for Joy-Con 2 communication

## Code Style
- Follow current Android, Kotlin, and Compose conventions
- Boy Scout Rule: leave code better than you found it
- One class per file
- Names should make code read like well-written prose (comments do not — see below)
- Methods should be short enough that they explain themselves
- Methods and composables should be reusable like components

## Comments
- Default to **no comment**. A name is the documentation; if renaming or extracting would make a
  comment redundant, do that instead
- A comment says only what the code cannot: a constraint, a unit, a byte's meaning, what a
  workaround works around. Litmus test: "could a reader reconstruct this from the code?" If yes, delete it
- **Plain and terse.** State the fact like a spec sheet, not a story: no narrative, aphorism or
  flourish. `// Dolphin ORs its inputs, so any bound source fires the target.`
- **One line is the norm, three the ceiling.** Longer means it is explaining the subject rather
  than the line — a derivation, a measurement, a byte layout, an emulator's internals, approaches
  that failed. That goes in the doc that owns it, with a one-line pointer:
  `/** Axes and signs: docs/dsu-motion.md#motion-frame */`
- **No KDoc on classes, interfaces, use cases or properties by default** — remove one that restates
  the name when you touch the file. Keep it only for a non-obvious "why"
- **One home per fact.** Never repeat a doc in a comment, or the same comment in sibling files (two
  emulator generators, two ViewModels): point at the doc, or put it on the shared type. Copies drift
- Physical-world facts (BLE protocol, byte layouts, timing, measurements, emulator internals) live in
  `protocol.md`, `virtual-gamepad.md`, `dsu-motion.md`; UI decisions in `DESIGN.md`; structure in
  `architecture.md`
- Tests: say it in the test name. An inline comment only labels a magic value (`// slot`, `// BUTTON_A 96`)

## Docs
- Written for someone about to change the code: what is true now, and why. No changelogs, "was X
  until Y" stories or ticked-off to-do lists — git holds history
- Lead with the fact. Tables and short bullets over paragraphs; cut preamble and restatement
- Keep a failed approach only when it stops someone retrying it, in a sentence or two
- Date a measurement and name the hardware it came from; nothing else needs a date
- **Docs are part of the change.** When behaviour a doc or the README describes changes, update it
  in the same commit
- `README.md` is for players (setup, troubleshooting); implementation detail goes in `docs/`

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
- **User-facing strings never live in a domain module** (pure JVM, no `R.string`). A domain type
  carries its identity — the enum entry, the stored id — and presentation names it through an
  exhaustive `when`, so a new case without a word fails to build (`MappingLabels`, `LayoutLabels`)
- Not copy, so they stay in code: **wire tokens** an emulator or protocol must match exactly
  (`DolphinControls`, `EdenControls`), and **hardware markings** the controller graphics draw
  (`JoyconButton.label` — "ZL", "+", "A")
- Avoid hard-coded strings elsewhere too — use string resources where possible
- Use theme for dimensions and colors rather than inline literals
- Prefer immutable data classes for state
- Use `@SuppressLint("MissingPermission")` only on methods guarded by the permission launcher
- Deprecated BLE APIs (`.value =` pattern) used intentionally for broad compatibility (API 24+)
