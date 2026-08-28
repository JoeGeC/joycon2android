# Contributing to Joycon2Android

Thanks for your interest in improving Joycon2Android! This guide covers how to get set up, the
standards your change is expected to meet, and how to get it merged.

## Coding standards

**[`CLAUDE.md`](CLAUDE.md) is the project's coding-standards contract, and it applies to everyone —
human or AI.** Read it before writing code. In short, it asks for:

- **Kotlin, Jetpack Compose, Material 3**, targeting Android API 24+.
- **Clean, self-documenting code** — short, well-named methods; one class per file; reusable
  composables. Prefer renaming or extracting over adding a comment.
- **Minimal comments** — no KDoc that restates the signature. A comment earns its place only when
  it captures a *why* the code can't express. Comments describing hardware/BLE protocol details
  (byte layouts, timing, Switch conventions) are welcome and can be detailed.
- **SOLID and clean architecture** — small focused classes, dependencies injected via constructors,
  orchestrators that delegate rather than implement.
- **No hard-coded strings** — use string resources. **No inline dimensions/colors** — use the
  theme.
- Immutable data classes for state; `@SuppressLint("MissingPermission")` only on permission-guarded
  methods.

## Architecture

This is a **Gradle multi-module** app split by **feature × layer** (`domain` / `data` /
`presentation`), and the module graph *enforces* the dependency rules at compile time. Before
making structural changes:

- Read [`docs/architecture.md`](docs/architecture.md) — the module graph, layers, and dependency
  rules.
- Follow [`docs/adding-a-feature.md`](docs/adding-a-feature.md) when adding or changing a feature —
  it has step-by-step recipes and explains the convention plugins in `build-logic/`.

Presentation reaches data **only through use cases** — never bypass it. Every module applies one of
the convention plugins; don't hand-roll `android {}` blocks in a module.

## Development setup

- **JDK 21** and a recent **Android Studio** (the repo targets AGP 9.2, Gradle 9.4, Kotlin 2.2).
- The native UHID relay builds via the pinned **NDK 28.2.13676358** and **CMake 3.22.1** — install
  both through the SDK Manager.
- Clone, then let Android Studio sync, or build from the command line:

  ```bash
  ./gradlew assembleDebug
  ```

Testing the virtual gamepad on-device also needs [Shizuku](https://shizuku.rikka.app/) running; the
DSU motion server and BLE features work without it. See the [README](README.md) for the full setup
guide and the BLE protocol reference.

## Before you open a PR

Run the same checks CI runs, and make sure they pass:

```bash
./gradlew build :konsist:test
```

This compiles every module, runs the unit tests, runs Android lint, and runs the **Konsist**
architecture tests that enforce layer placement. If you moved classes between modules, `:konsist:test`
is what catches a misplacement.

- **Add tests** for new domain/data logic where practical — the layering exists so each class is
  independently testable.
- **Leave the code better than you found it** (the Boy Scout Rule).

## Pull requests

1. Branch off `main`.
2. Keep each PR focused on one change; write a clear description of *what* and *why*.
3. Use imperative, descriptive commit messages that match the existing history
   (e.g. `Add Eden Nightly support for Virtual Gamepad auto-setup`).
4. Make sure CI is green — PRs won't be merged with a failing build.

## Reporting bugs

BLE behaviour is very device-specific, so a good bug report includes:

- Device model and Android version
- Controller type (Joy-Con 2 left / right / pair / Pro) and how it was connected
- What you expected vs. what happened
- A relevant `adb logcat` snippet (the app logs under the `Joycon2` and `DsuServer` tags)

Open an issue, or start a thread in Discussions for setup questions and ideas.

## License

By contributing, you agree that your contributions will be licensed under the project's
[GPL-3.0](LICENSE) license.
