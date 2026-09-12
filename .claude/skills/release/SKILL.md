---
name: release
description: Cut a prerelease or a release of Joycon2Android — choose the version bump from what actually changed, update build.gradle.kts, publish, and verify. Use when asked to release, ship, cut a build, publish a test build for sharing, or bump the version.
user-invocable: true
argument-hint: "[prerelease|release] [patch|minor|major]"
---

# Releasing Joycon2Android

## Versions change only when publishing

Never bump ahead of time and never bump straight after a release — the number is
chosen at the moment something is published, from what is in it.

| What is in it | Bump | From 1.2.3 |
|---|---|---|
| Fixes | patch | 1.2.4 |
| Small features | minor | 1.3.0 |
| Big features, architecture changes | major | 2.0.0 |

Mixed content takes the highest level present. `versionCode` increments by one on
every bump, whatever the level — Play rejects a repeat.

## 1. Work out what is in it

```sh
git fetch --tags
last=$(git describe --tags --abbrev=0 --match 'v[0-9]*' --exclude '*debug*')
git log "$last"..HEAD --no-merges
```

Fetch first. Releases cut through `gh` create the tag server-side, so without a
fetch this reads a stale tag and proposes the wrong version.

Exclude `*debug*` or prerelease tags will be picked as the last release.

Read the commits rather than counting them, then state the level and one line of
why. If the user named a level, use theirs. Ask only when the call is genuinely
close — a fix and a small feature together is a minor, not a question.

## 2. Bump

`app/build.gradle.kts` is the only place a version lives (`versionCode`,
`versionName`) — no changelog or fastlane metadata to keep in step.

Commit as `Bump version to X.Y.Z`, matching the existing history, and push to
`main`. `main` is protected by the `main-guardrails` ruleset, but the repository
admin role bypasses it, so a direct push lands.

## 3a. Prerelease — a shareable debug build

The `Prerelease` workflow reads the version out of `build.gradle.kts`, so after
a bump it needs no `version` input:

```sh
gh workflow run Prerelease -f notes="What to try in this build"
gh run watch <id> --exit-status
gh release view vX.Y.Z-debug.<run> --json tagName,name,isPrerelease,assets
```

It runs the same `./gradlew build :konsist:test` gate as CI, so it cannot
publish from a failing tree. The APK is signed with the debug key restored from
the `DEBUG_KEYSTORE_BASE64` secret: it installs over a local debug build, and
never over a release build.

## 3b. Release — the real thing

CI cannot do this. Release signing needs `release.jks` and
`keystore.properties`, both gitignored and local only.

```sh
./gradlew :app:assembleRelease
```

Verify before publishing, not after:

- `apksigner verify --print-certs` on the new APK **and** on the previous
  release's asset (`gh release download <last tag> --pattern '*.apk'`). The
  signer SHA-1 must match, or "installs in place" is a false promise and every
  user has to uninstall first.
- `aapt2 dump badging` — `versionName` and `versionCode` must be the ones
  intended. Both tools live in `~/Library/Android/sdk/build-tools/*/`.

Then publish, with the asset named `joycon2android-X.Y.Z.apk`:

```sh
gh release create vX.Y.Z <apk> \
  --title "Joycon2Android X.Y.Z" --notes-file notes.md --target main --latest
```

`--target` takes a branch name; a commit SHA is rejected with
`Release.target_commitish is invalid`.

Confirm afterwards that the tag points at the intended commit, `isPrerelease` is
false, and the asset is attached.

## Release notes

Brief and human. Say what a player notices, not the mechanism — the commits hold
the detail for anyone who wants it.

- A single summary line at the top.
- `## What's new` — concise bullets, each a bolded lead plus one sentence of
  consequence. Leave out tooling, CI and doc-only changes unless a user would
  feel them.
- `## Upgrading` — only when the user must act. Rewriting an emulator's config
  is the usual case: new bindings only reach Dolphin when **Set up Dolphin and
  Wiimote mapping** runs again, with Dolphin closed.
- `## Install` — download `joycon2android-X.Y.Z.apk`, installs over an existing
  copy, link the README setup guide.
- `## Notes` — SYNC is needed for every connection, and the Nintendo
  disclaimer with a link to the credits. Carry these forward each release.

## Gotchas

- `gh release view` with no tag shows the latest **stable** release, so a
  prerelease has to be named explicitly.
- An asset's `size_in_bytes` reads `null` for a moment after upload. Not a
  failure.
- Prerelease tags carry the workflow's run number, which is monotonic across the
  workflow's whole life and cannot be reset.
