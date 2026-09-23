# Contributing to Joycon2Android

Thanks for helping improve Joycon2Android! This covers setup, the standards a change must meet, and
how to get it merged.

## Coding standards

**[`CLAUDE.md`](CLAUDE.md) is the coding-standards contract for everyone, human or AI.** Read it
before writing code. In short:

- **Self-documenting code** — short, well-named methods, one class per file, reusable composables.
- **Minimal, terse comments** — only what the code can't say. Protocol details, measurements and
  emulator internals go in [`docs/`](docs/README.md), not in comments.
- **SOLID and clean architecture** — small focused classes, constructor injection, orchestrators that
  delegate.
- **No hard-coded strings, dimensions or colours** — string resources and the theme.

## Architecture

A Gradle multi-module app split by feature × layer, whose module graph enforces the dependency rules.
Read [`docs/architecture.md`](docs/architecture.md) before structural changes, and follow
[`docs/adding-a-feature.md`](docs/adding-a-feature.md) when adding or changing a feature.

## Development setup

- **JDK 21** and a recent **Android Studio** (the repo targets AGP 9.2, Gradle 9.4, Kotlin 2.2).
- The native UHID relay builds via the pinned **NDK 28.2.13676358** and **CMake 3.22.1** — install
  both through the SDK Manager.
- Clone, then let Android Studio sync, or build from the command line:

  ```bash
  ./gradlew assembleDebug
  ```

Testing the virtual gamepad on-device also needs [Shizuku](https://shizuku.rikka.app/) running; the
DSU motion server and BLE features work without it. See the [README](README.md) for the setup guide,
and [docs/protocol.md](docs/protocol.md) for the BLE protocol.

## Before you open a PR

Run the same checks CI runs, and make sure they pass:

```bash
./gradlew build :konsist:test
```

That compiles every module and runs the unit tests, Android lint and the Konsist architecture tests.
Add tests for new domain and data logic, and update any doc your change makes stale.

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
