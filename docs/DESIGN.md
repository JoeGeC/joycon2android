# Design

> Compose-adapted design-system snapshot. This is a native Android (Jetpack Compose,
> Material 3) app, so this doc captures the **theme in code** rather than web CSS tokens.
> Source of truth: `core/designsystem/src/main/kotlin/.../ui/theme/{Color,Type,Dimens,Theme}.kt`.
> Update this doc when those change.

## Theme

Dark-only. `Joycon2AndroidTheme` wraps Material 3 with a `darkColorScheme` that maps only
`primary`, `surface`, and `background`; the rest of the palette lives as top-level `Color`
vals consumed directly. Deep near-black blue-gray canvas, single teal accent, controller-color
accents on cards. No light scheme currently exists.

Physical scene: an enthusiast at a desk or on a couch, often in a dimly lit room, mid-setup,
frequently holding a controller in the other hand. Dark is a deliberate fit, not a default.

## Color

Defined in `Color.kt`. Values are the real hex in code.

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
- Battery ramp (`batteryColor()`): high `Accent` · medium `#FBBF24` · low `ErrorText`
  — **color-only today; pair with icon/text for color-blind safety**

**Signature: controller shell color.** The controller's real shell accent is read from SPI flash
(packed `0xRRGGBB`), converted to HSV, saturation-boosted ×1.4 (capped). It drives two things:
- `joyconBorderColor()` — the card's hairline border (colour verbatim).
- `controllerActiveColor()` — the same hue with a brightness floor (0.72) so it reads as "lit"
  filling a control; every live input inside a `JoyconCard` glows in it (pressed d-pad / face /
  shoulder / rail / special buttons, and the stick ring + dot). Delivered via the
  `LocalControllerAccent` CompositionLocal, so a dual pair lights each side in its own colour.
  `readableInkOn()` picks dark-ink-or-white by WCAG contrast for the label on that fill.

This is the app's identity move — the UI, not just its border, wears the colour of the actual
hardware. `JoyconBlue`/`JoyconRed` and the teal `Accent` are the fallbacks. Lean into this.

**Color strategy:** Committed-dark — one teal accent doing most of the lifting, with the
controller shell color as a per-item second accent. Not restrained (the hardware color is
load-bearing), not full-palette.

## Typography

`Type.kt` defines a full Material 3 `Typography` — a fixed sp scale (product UI, not fluid),
~1.2 ratio, with weight/tracking carrying hierarchy alongside size and a small line-height +
tracking bump for light-on-dark. UI text is styled via `MaterialTheme.typography.*`; there are
no scattered `fontSize` literals in the UI.

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
- **Card:** corner `20`, **full border `2dp`** (`cardBorderAlpha` 1f) — good, no side-stripe accents
- Button: corner `12`, height `44` (large `52`) · Pill: corner `20`
- Spacing rhythm: section `14`, element `8` (varied, not a single uniform gap)
- Rich controller-visualisation dims: stick canvas `110`, d-pad/face `46`, sideways-layout
  variants, IMU/legend/battery-icon sub-scales

Card-based, but cards are the correct affordance here (each = one controller/feature). The
controller-color border gives them identity beyond a plain card grid.

## Components

Shared in `core/designsystem/.../ui/components/`:
- `FeatureToggleCard` — the primary on/off feature surface (gamepad, DSU)
- `EmulatorDropdown` / `EmulatorOption` / `EmulatorAutoSetup` — emulator picker + one-tap setup
- `DolphinSetupButton` / `DolphinSetupPhase` — staged Dolphin config flow
- `ErrorBox` · `LabeledBorderBox` · `ExpandableInfoSection` · `CopyableCode`

## Motion

Largely unspecified today. Given the "playful gaming gear" personality, motion is an
opportunity area — controller-connect, assignment, and toggle transitions could carry
character. Any motion must honor system reduced-motion (see PRODUCT.md accessibility).
Ease-out curves, no bounce/elastic.

## Opportunity Areas (for future impeccable passes)

1. ~~**Type hierarchy** — replace scattered `fontSize*` literals with a real Material type scale.~~
   ✅ Done: full Material 3 `Typography` + `AppType` telemetry/overline roles (see Typography above).
2. **Personality gap** — partly closed: each controller's live inputs now glow in its real shell
   colour (see the controller-color signature above). Remaining: motion, and celebratory
   connect/assign moments.
3. ~~**Contrast** — reduced-alpha telemetry labels + battery-low failed WCAG AA.~~ ✅ Done:
   telemetry now uses solid `TextBright` (values) / `TextDim` (labels) with no sub-threshold alpha,
   and `BatteryLow` was lightened to `#FF8A8A`; all clear 4.5:1 (verified numerically).
4. **Color-only status** — pair battery/connection color with icon or text.
5. **Motion system** — define purposeful, reduced-motion-aware transitions.
