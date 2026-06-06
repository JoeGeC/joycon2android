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
- Self-describing code — comments only when context or "why" is non-obvious
- This project benefits from comments explaining BLE protocol details, timing constraints, and byte-level formats since these are not self-evident from code alone

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
- Single-activity Compose app
- State flows from BLE layer → domain state → ViewModel → Compose UI
- GATT operations are queued (Android allows only one at a time)
- Protocol reference: `joycon2_android_reference.md` is authoritative over any external docs

## Conventions
- Use `enableEdgeToEdge()` with `WindowInsets.systemBars` for edge-to-edge inset handling
- Avoid hard-coded strings — use string resources where possible
- Use theme for dimensions and colors rather than inline literals
- Prefer immutable data classes for state
- Use `@SuppressLint("MissingPermission")` only on methods guarded by the permission launcher
- Deprecated BLE APIs (`.value =` pattern) used intentionally for broad compatibility (API 24+)
