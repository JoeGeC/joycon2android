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
- Comments describing the physical world ARE welcome and can be detailed: BLE protocol details, byte layouts, timing constraints, and hardware behavior being mirrored (e.g. Switch combo/LED conventions) — these cannot be derived from code
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
- Read `README.md` for project context, BLE protocol reference, packet layout, and Android-specific gotchas

## Conventions
- Use `enableEdgeToEdge()` with `WindowInsets.systemBars` for edge-to-edge inset handling
- Avoid hard-coded strings — use string resources where possible
- Use theme for dimensions and colors rather than inline literals
- Prefer immutable data classes for state
- Use `@SuppressLint("MissingPermission")` only on methods guarded by the permission launcher
- Deprecated BLE APIs (`.value =` pattern) used intentionally for broad compatibility (API 24+)
