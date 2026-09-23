# Design

The theme as built in `core/designsystem/.../ui/theme/` and the screen layout in
`app/.../ui/JoyconScreen.kt`. Update this doc when those change; who the app is for is in
[PRODUCT.md](PRODUCT.md).

## Theme

Dark-only, deliberately: the app is used mid-setup, often in a dim room. `Joycon2AndroidTheme` maps
only `primary`, `surface` and `background` into a `darkColorScheme`; the rest of the palette is
top-level `Color` vals used directly. One teal accent, plus each controller's own shell colour.

## Color

**Surfaces & ink**
- `Background` / `surface` — `#0E1116` (deep near-black blue-gray canvas)
- `CardBg` — `#161B22` (raised card surface)
- `ButtonOff` — `#1A1F26` (inactive control fill)
- Ink (default on-surface) — Material default near-white
- `TextBright` — `#C2CDD8` (light blue-gray; live telemetry values — the data that pops, ~10.7:1 on CardBg)
- `TextDim` — `#8B98A5` (muted gray secondary text / telemetry labels; ~5.9:1 on CardBg — AA-safe as a solid, never at reduced alpha for text)
- `TextOnAccent` — `#0E1116` (ink on the teal accent)

**Accent**
- `Accent` — `#38E0C8` (teal/cyan; `primary`, active toggles, high battery, default Joy-Con color)
- `AccentDim` — `#1C3A38` (muted teal for dim/secondary accent states)

**Status**
- `ErrorText` — `#FF6B6B` / `ErrorBg` — `#2D1B1B`
- Battery ramp (`batteryColor()`): high `Accent` · medium `#FBBF24` · low `BatteryLow #FF8A8A`
  (a lighter red than `ErrorText` so the low % clears AA on the `AccentDim` pill). Shown with a
  battery icon whose fill tracks the level, so it isn't colour-only.

**Signature: the controller's shell colour.** The UI wears the colour of the actual hardware — the
app's identity move, so lean into it. The shell accent is read from SPI flash (`0xRRGGBB`) and
saturation-boosted ×1.4 (capped) in HSV. It drives:
- `joyconBorderColor()` — the card's hairline border, colour verbatim.
- `controllerActiveColor()` — the fill of every live input in a `JoyconCard` (pressed buttons, stick
  ring and dot), with a brightness floor of 0.72: a near-black shell would otherwise vanish on the
  dark UI. `ControllerAccent` provides it per card, so a pair lights each side in its own colour, and
  `readableInkOn()` picks dark ink or white for the label on it.

`JoyconBlue` / `JoyconRed` and the teal `Accent` are the fallbacks.

## Typography

`Type.kt` is a full Material 3 `Typography`: a fixed sp scale at ~1.2 ratio, with weight and
tracking carrying hierarchy alongside size, and a small line-height and tracking bump for
light-on-dark. UI text uses `MaterialTheme.typography.*`, never a `fontSize` literal.

| Role | Size / LH | Weight | Use |
|---|---|---|---|
| titleLarge | 22 / 28 | Bold | top-level heading (reserved) |
| titleMedium | 16 / 22 | SemiBold | player labels, primary CTA |
| titleSmall | 14 / 20 | SemiBold | card & section headings |
| bodyLarge | 15 / 22 | Normal | primary reading text |
| bodyMedium | 13 / 19 | Normal | secondary body, error/banner |
| bodySmall | 12 / 16 | Normal | captions, subtitles, guide steps |
| labelLarge | 14 / 20 | SemiBold | buttons |
| labelMedium | 12 / 16 | Medium | compact action labels |
| labelSmall | 11 / 16 | Medium | small labels |

Two app roles live in `AppTextStyles.kt` (`AppType`) because they aren't reading hierarchy:
- **`telemetry`** — monospace + tabular figures (`tnum`) + `includeFontPadding=false`, sized by the
  caller. All live numeric readouts (IMU, stick coords, battery %, DSU port, config snippets, the
  DS4 name table) share it, so digit columns stay aligned as values change.
- **`statusOverline`** — the wide-tracked `DISCONNECTED / SHIZUKU` chrome label in the app bar.

Controller-visualisation glyph sizes (d-pad arrows, face/shoulder labels, on-controller buttons)
stay in `Dimens` as geometry tuned to the drawn controls — deliberately outside the type scale.

## Layout & Shape

From `Dimens.kt` (all dp unless noted):
- Screen padding `16` H / `16` V · card padding `16`
- **Card:** corner `20`, **full border `2dp`** (`cardBorderAlpha` 1f) — no side-stripe accents
- Button: corner `12`, height `44` (large `52`); on-controller button corner `6`
  (`controllerButtonCorner`) · Pill: corner `20`
- Spacing rhythm: section `14`, element `8` (varied, not a single uniform gap)
- **Touch targets:** `minTouchTarget` `48` — every interactive control clears it via
  `minimumInteractiveComponentSize()` or a min-height, keeping visual size independent of tap size
- Rich controller-visualisation dims: stick canvas `110`, d-pad/face `46`, sideways-layout
  variants, IMU/legend/battery-icon sub-scales, plus stick sub-tokens (`stickValueGap`,
  `stickAxisGap`, `crosshairStroke`, `stickIdleRingAlpha`) — fully tokenised, no hard-coded values

Cards are the right affordance here — each is one controller or feature — and the shell-colour
border gives them identity beyond a plain grid.

### Connection-screen chrome

In `JoyconScreen.kt`:

- **Edge-to-edge.** `contentWindowInsets` reserves only the horizontal insets, so content passes
  under the transparent status and nav bars; each screen adds its own clearance.
- **Overlaid, scroll-away app bar.** Not in the Scaffold's `topBar` slot, which would reserve space.
  It overlays the content and is translated up in lockstep with the scroll, so it slides away with
  no gap and content (the Ko-fi banner included) passes behind the status bar. Screens add its
  height plus the status-bar inset as top clearance.

### Landscape

Two-up, to use the wide, short viewport; portrait keeps single columns.

- **Players** — a two-column grid. Detailed players are shrunk to `LandscapePlayerScale` (0.7) by
  `scaleLayout`, which scales the whole controller uniformly and reflows, so a full player fits the
  short height. Compact rows aren't scaled.
- **Feature cards** — Virtual Gamepad with its Shizuku card beneath on the left, DSU on the right.
- **Scanning graphics** — the "Looking for Joy-Con 2" card and sync-button illustration side by side.
- **Action buttons** — Disconnect All left, Scan right; Disconnect keeps its half while Scan is hidden
  during a scan.

An odd trailing item takes a half cell, a weighted `Spacer` filling the other half.

## Components

Shared in `core/designsystem/.../ui/components/`:
- `FeatureToggleCard` — the primary on/off feature surface (gamepad, DSU)
- `OptionDropdown` / `DropdownOption` / `PanelDropdownMenu` — the app's picker: an accented current
  value over a panel of alternatives, each row optionally sub-labelled, dimmed or deletable
- `EmulatorDropdown` / `EmulatorOption` / `EmulatorAutoSetup` — emulator picker + one-tap setup
- `DolphinSetupButton` / `DolphinSetupPhase` / `CloseEmulatorDialog` — staged setup flow
- `SettingsRow` · `SettingSwitch` — settings surfaces (e.g. Motion settings)
- `ConfirmDialog` · `TextInputDialog` — ask before a change lands, or ask it for a name
- `ErrorBox` · `WarningBox` · `LabeledBorderBox` · `ExpandableInfoSection` · `CopyableCode`

## Motion

Minimal and functional: the scroll-away app bar, the portrait view-mode crossfade, and expand/fade
on error boxes and feature-card content. Ease-out curves, no bounce or elastic, and always honour the
system's reduced-motion setting.

## Open work

- **Motion with character** — purposeful, reduced-motion-aware transitions for connect and assign
  moments, which the "playful gaming gear" personality still lacks.
- **Density check** — the dual layout at ≤320dp and 200% font scale (needs a device).
