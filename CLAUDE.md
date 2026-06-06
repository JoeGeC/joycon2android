# Project Guidelines

## Language & Frameworks
- Kotlin, Jetpack Compose, Material 3
- Target: Android API 24+ (minSdk 24, targetSdk 36)
- BLE (BluetoothGatt) for Joy-Con 2 communication

## Code Style
- Follow current Android, Kotlin, and Compose conventions
- Clean Architecture and SOLID principles
- Boy Scout Rule: leave code better than you found it
- Self-describing code — comments only when context or "why" is non-obvious
- This project benefits from comments explaining BLE protocol details, timing constraints, and byte-level formats since these are not self-evident from code alone

## Architecture
- Single-activity Compose app
- State flows from `Joycon2Manager` (BLE engine) → `Joycon2State` data class → Compose UI
- GATT operations are queued (Android allows only one at a time)
- Protocol reference: `joycon2_android_reference.md` is authoritative over any external docs

## Conventions
- Use `enableEdgeToEdge()` + `systemBarsPadding()` for proper inset handling
- Prefer immutable data classes for state
- Use `@SuppressLint("MissingPermission")` only on methods guarded by the permission launcher
- Deprecated BLE APIs (`.value =` pattern) used intentionally for broad compatibility (API 24+)
