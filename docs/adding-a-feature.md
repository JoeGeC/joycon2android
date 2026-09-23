# Adding or changing a feature

Assumes the layer and module rules in [architecture.md](architecture.md).

- **Extending an existing feature** (new action, state or screen section) →
  [Recipe A](#recipe-a--extend-an-existing-feature). The common case.
- **A new capability nothing else owns** → [Recipe B](#recipe-b--add-a-new-feature).

Unsure? Ask *"what changes for what reason?"* If it changes for the same reason as an existing
feature, it belongs there.

## The convention plugins

Every module's `build.gradle.kts` applies exactly one of these (`build-logic/convention/`). Never
hand-roll `android {}` or `compileSdk` in a module.

| Plugin id | For | Gives you |
|---|---|---|
| `joycon.kotlin.jvm` | `domain`, `:core:model`, `:core:session`, pure-Kotlin `data` | Kotlin/JVM, Java 11 |
| `joycon.android.library` | Android `data` modules | `com.android.library`, compileSdk 36, minSdk 24, Java 11, JVM unit-test defaults. Never also apply `org.jetbrains.kotlin.android`: AGP 9 has Kotlin built in. |
| `joycon.android.library.compose` | `presentation`, `:core:designsystem` | the above + Compose |

## Recipe A — extend an existing feature

Five edits, following the feature's existing pattern:

1. **Domain — add the capability to the repository interface** (if the data layer needs to do
   something new). e.g. add `fun setFoo(enabled: Boolean)` to `DsuRepository`.

2. **Data — implement it** in the repository impl (`DsuServer`, `Joycon2Manager`,
   `GamepadOutput`, …).

3. **Domain — add a one-line use case.** Presentation reaches data *only* through use cases, always
   an `operator fun invoke`:

   ```kotlin
   class SetFooUseCase(private val repository: DsuRepository) {
       operator fun invoke(enabled: Boolean) = repository.setFoo(enabled)
   }
   ```

4. **Composition root — expose it.** In `AppContainer`, add `val setFoo = SetFooUseCase(dsuRepository)`.

5. **Presentation — surface it** in the feature's ViewModel (inject the use case via its
   constructor) and call it from the UI. Then add the new argument to the ViewModel's factory in
   `MainActivity`:

   ```kotlin
   DsuViewModel(c.observeDsuStatus, c.enableDsu, c.disableDsu, c.setFoo)
   ```

For new *state*, the repository exposes a `Flow`, an `Observe…StatusUseCase` wraps it (often
`combine`-ing several into a status data class), and the ViewModel `stateIn`s it. Mirror
`ObserveDsuStatusUseCase` / `DsuViewModel`.

## Recipe B — add a new feature

For a feature `foo`, create three modules (drop `presentation` without UI, `data` for pure logic).

### 1. Register the modules

In `settings.gradle.kts`:

```kotlin
include(":feature:foo:domain")
include(":feature:foo:data")
include(":feature:foo:presentation")
```

### 2. Create the build files

`feature/foo/domain/build.gradle.kts`:
```kotlin
plugins { id("joycon.kotlin.jvm") }
dependencies { api(project(":core:model")) }
```

`feature/foo/data/build.gradle.kts` (Android — use `joycon.kotlin.jvm` instead if it needs no
Android APIs, like `assignment:data`):
```kotlin
plugins { id("joycon.android.library") }
android { namespace = "com.joegec.joycon2android.foo.data" }
dependencies {
    implementation(project(":feature:foo:domain"))
    implementation(project(":core:model"))
}
```

`feature/foo/presentation/build.gradle.kts`:
```kotlin
plugins { id("joycon.android.library.compose") }
android { namespace = "com.joegec.joycon2android.foo.presentation" }
dependencies {
    implementation(project(":feature:foo:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)   // if it has a ViewModel
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}
```

### 3. Domain: repository interface + use cases

```
feature/foo/domain/src/main/kotlin/.../foo/
  FooRepository.kt        // interface: StateFlows to observe + functions to drive
  FooStatus.kt            // immutable data class the UI renders
  EnableFooUseCase.kt     // operator fun invoke(...) = repository.enable()
  ObserveFooStatusUseCase.kt
```

### 4. Data: the implementation

`FooManager` implements `FooRepository`. Only this layer touches BLE, the relay, sockets or
framework APIs.

### 5. Presentation: ViewModel + UI

```kotlin
class FooViewModel(
    observeFooStatus: ObserveFooStatusUseCase,
    private val enableFoo: EnableFooUseCase,
) : ViewModel() {
    val status: StateFlow<FooStatus> = observeFooStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), FooStatus())
    fun enable() = enableFoo()
}
```

Compose UI goes alongside it, built from `:core:designsystem` components.

### 6. Wire it into `:app`

- Add the three modules to `:app`'s `dependencies {}`.
- In `AppContainer`: construct the repository impl as a singleton and bind its use cases.
- In `MainActivity`: add `private val fooViewModel: FooViewModel by viewModels { viewModelFactory
  { initializer { val c = (application as JoyconApplication).container; FooViewModel(c.observeFoo,
  c.enableFoo) } } }`, then render its section in `JoyconScreen`.
- If the feature reacts to player assignment (like gamepad/dsu), hook it into the
  `SessionCoordinator`'s `onState` in `AppContainer` rather than calling it from the UI.

## Gotchas

- **Strings and `R` are per-namespace.** A composable moved into a feature module needs its strings
  copied into that module's `res/values/strings.xml` and its `R` import switched to the module's
  namespace. Rename a package, and update the module's `namespace` to match.
- Package layout, singleton scope, cross-feature coordination and the per-packet path are in
  [architecture.md](architecture.md); `:konsist:test` fails the build if a class lands in the wrong
  layer.

## Before you're done — checklist

- [ ] `./gradlew :app:assembleDebug` is green.
- [ ] `./gradlew test` is green (including the new module's tests).
- [ ] `./gradlew :konsist:test` is green.
- [ ] New use case has an `operator fun invoke`; it's the only way presentation reaches the impl.
- [ ] Repository interface is in `domain`; its impl is in `data`; the ViewModel is in `presentation`.
- [ ] Nothing app-specific leaked into `:core:designsystem`.
- [ ] App installs and the feature works on device.
