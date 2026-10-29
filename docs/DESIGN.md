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
- `TextTitle` — `#FFFFFF` (screen, section, card, dialog and setting titles)
- `TextBright` — `#C2CDD8` (light blue-gray; live telemetry values — the data that pops, ~10.7:1 on CardBg)
- `TextDim` — `#8B98A5` (muted gray secondary text / telemetry labels; ~5.9:1 on CardBg — AA-safe as a solid, never at reduced alpha for text)
- `TextOnAccent` — `#0E1116` (ink on the teal accent)

**Accent**
- `Accent` — `#38E0C8` (teal/cyan; `primary`, active toggles, high battery, default Joy-Con color)
- `AccentDim` — `#1C3A38` (muted teal for dim/secondary accent states)

**Status**
- `ErrorText` — `#FF6B6B` / `ErrorBg` — `#2D1B1B`
- `WarningText` — `#FFB454` / `WarningBg` — `#2D2113`: a limit or a recoverable state, not a fault —
  the no-gyroscope note, and a dropped controller's **Connection lost** (a `WarningBox` at the top
  of its player card, a link-off glyph in the compact row, a label in the unassigned card), and the
  tappable battery-restriction banner at the top of the connection screen, shown until the app runs unrestricted
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
  no gap and content passes behind the status bar. Screens add its height plus the status-bar
  inset as top clearance.
- **Settings behind a gear.** The app bar's only action opens the settings panel, an `EndDrawer`
  from the right so it sits under the icon that opened it. The Ko-fi banner sits under the panel title.
- **The type label is the type menu.** An unassigned controller's type at the top of its card opens
  a `PanelDropdownMenu`: Detect automatically first, sub-labelled with what was detected, then the
  four types. A manual choice shows `SET MANUALLY` beside the label. Only an unrecognised controller
  gets a `WarningBox`, which opens the same menu; a recognised one costs no extra row.
- **A GameCube controller is named and drawn as itself.** Its card, compact row and mapping card
  say GameCube, and its detailed view is the Pro layout without the buttons it lacks, with ZR marked
  Z and its triggers, L and R, above ZL and Z. `GameCubeButtons` is the one list of what it has, which the mapping editor offers too.

### Output card

- **On by itself.** Assigning a player switches it on, so the usual path is assign and play; the
  switch is for stopping. Tabs, emulator and mapping stay open either way.
- **A tab per output, so setup has one job.** Virtual gamepad, Motion (DSU), Mouse & keys; the
  emulator box under a tab is auto setup only, for that output. **Other** ends its list: Set up hides
  and a labelled Mapping button eases out to fill the row (`SplitRow`; weights in a `Row` snap), for
  the keys-only mapping.
- **A tab's label lights while its output runs**, in the accent: what is actually up, so an output
  that failed (no Shizuku) stays dim. Mouse & keys lights for the keyboard or mouse, on other tabs too
  once the mapping uses them. A lit tab tells a screen reader it's running, as colour alone can't.
- **The subtitle names what is running:** "Active — Virtual gamepad", "Active — Mouse & keys", or
  DSU's port and listening emulators on the Motion tab. A warning, such as DSU's slot limit, sits at
  the bottom.
- **A tab whose output needs Shizuku says so** in the card's red error line while it runs without
  Shizuku: the virtual gamepad from its failed start, Mouse & keys from Shizuku's absence, since the
  keyboard and mouse wait for Shizuku rather than fail. Motion names what its mapping presses that
  needs it ("Shizuku is needed for mouse and keyboard"). The line follows Shizuku and the tab.
- **Long tab names wrap** to two centred lines at phone width rather than cut off.
- **Mouse & keys warns when nothing can move the pointer**: a `SettingWarning` under Mapping while
  every connected controller lacks the Joy-Con 2's mouse sensor (Pro, GameCube, third-party), so
  clicks that work without a moving pointer don't read as broken. The mapping editor's Mouse section
  warns the same per player.

### Mapping editor

- **A row per target**, its sources in a `MultiSelectDropdown`, sections in `MappingSection` order:
  the console's own, then Android, Mouse, Keyboard.
- **Tapping a row listens first** (`PressToMapDialog`): the next button or half-way stick push on
  that player's controller replaces the row's sources, which is what pressing to bind means
  everywhere else. **Choose from list** opens the dropdown, the only way to bind several. A press
  the row doesn't offer (a stick on Android) is ignored. Live input reaches only the dialog
  (`LiveController`), so the screen doesn't recompose per frame. Android, keyboard and mouse keys
  pause while it's open and until the controller lets go, so a button bound to Back binds rather
  than acting as it lifts.
- **The Wii controller is one untitled `SegmentedChoice`, not switches**, on the Wii console:
  Wiimote, Sideways (lone Joy-Con only) and Classic exclude each other, so two switches
  would offer a combination that can't exist. The options name themselves, so it needs no title. The
  chosen one's description cross-fades beneath it, and Thrusts hides on Classic.
- **Each section sits on its own panel**, `SectionBg` rounded inside the card, so scrolling shows
  where one ends and the next begins.
- **Every section collapses.** The app's own keys (Android, Mouse, Keyboard) start collapsed, Other's
  included, and open only on Mouse & keys, where they're the point.
- **Keyboard lists only the keys in use.** Bound keys, then an "Add a key" `OptionDropdown` of the
  rest; a key added but left on None is gone next visit. Sixty rows of None would bury the few that
  matter. A bound letter adds a `SettingWarning` that the on-screen keyboard hides.
- **Mouse is five plain rows**, like Android: few enough to show whole.
- **Other's keys-only mapping is named for its output**, "Virtual Gamepad" or "Motion Controller",
  though both share it. Its tip says how to map by hand in place of trying another layout; like every
  tip it hides with **Show tips** off.

### Landscape

Two-up, to use the wide, short viewport; portrait keeps single columns.

- **Players** — a two-column grid. Detailed players are shrunk to `LandscapePlayerScale` (0.7) by
  `scaleLayout`, which scales the whole controller uniformly and reflows, so a full player fits the
  short height. Compact rows aren't scaled.
- **Output card** — full width, not a cell: its tabs need the room, and
  its mapping button is labelled. The Shizuku card, when shown, sits beneath at full width too.
- **Scanning graphics** — the "Looking for Joy-Con 2" card and sync-button illustration side by side.
- **Action buttons** — Disconnect All left, Scan right; Disconnect keeps its half while Scan is hidden
  during a scan.

An odd trailing item takes a half cell, a weighted `Spacer` filling the other half.

## Components

Shared in `core/designsystem/.../ui/components/`:
- `FeatureToggleCard` — the primary on/off feature surface (the Output card)
- `SegmentedChoice` — one of a few options, all in view, any of them lit in the accent (the Output
  card's tabs)
- `OptionDropdown` / `DropdownOption` / `PanelDropdownMenu` — the app's picker: an accented current
  value over a panel of alternatives, each row optionally sub-labelled, dimmed or deletable
- `EmulatorDropdown` / `EmulatorOption` / `EmulatorAutoSetup` — emulator picker + one-tap setup
- `DolphinSetupButton` / `DolphinSetupPhase` / `CloseEmulatorDialog` — staged setup flow
- `SettingSwitch` — a titled, described switch (the settings panel)
- `EndDrawer` — a modal drawer from the end edge (the settings panel)
- `ConfirmDialog` · `TextInputDialog` — ask before a change lands, or ask it for a name
- `InfoSheet` — help behind an info icon, as a bottom sheet (auto setup, DSU mapping)
- `ErrorBox` · `WarningBox` · `LabeledBorderBox` · `ExpandableInfoSection` · `CopyableCode`
- `InfoBox` — an accent note with an exclamation-circle icon, at the top of a screen (mapping tips)
- `HintBubble` — an accent bubble pointing up at a control (the mapping button, under auto setup; the save button, under a Custom group or player layout)
- Tips (`InfoBox`, `HintBubble`) show only while `LocalTips` is visible and pass its `onTap`;
  `ProvideTips` at the root supplies it from **Show tips** and asks before turning them off

## Motion

Minimal and functional: the scroll-away app bar, a fade-through between detailed and compact player cards, and expand/fade
on error boxes and feature-card content. Ease-out curves, no bounce or elastic, and always honour the
system's reduced-motion setting.

- **`Motion.easeOut()`** is the one spec for new transitions: 220 ms, decelerating. Compose scales it
  by the system's animation setting, so turning animations off makes it instant.
- **Every state change on the Output card moves.** `SegmentedChoice`'s highlight slides to the
  choice and its labels fade to lit or dim; the tab's content cross-fades as the card eases to its
  new height; the subtitle cross-fades; DSU's slot warning expands in and out.

## Open work

- **Motion with character** — purposeful, reduced-motion-aware transitions for connect and assign
  moments, which the "playful gaming gear" personality still lacks.
- **Density check** — the dual layout at ≤320dp and 200% font scale (needs a device).
