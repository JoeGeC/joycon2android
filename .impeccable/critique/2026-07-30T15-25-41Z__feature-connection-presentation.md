---
target: feature/connection/presentation
total_score: 24
p0_count: 0
p1_count: 1
timestamp: 2026-07-30T15-25-41Z
slug: feature-connection-presentation
---
# Critique — feature/connection/presentation

Native Jetpack Compose. Deterministic detector N/A (Kotlin, not markup). Fully-connected
surfaces not visually verified (require paired Joy-Con 2 hardware); review is source-level.

## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Rich live feedback (presses, stick, battery); connection-loss-while-assigned unclear |
| 2 | Match System / Real World | 3 | Controller viz mirrors hardware faithfully; raw IMU ints have no units |
| 3 | User Control and Freedom | 3 | Unassign easy + recoverable via re-assign, but no explicit undo |
| 4 | Consistency and Standards | 3 | Tap-to-unassign consistent; label removes both vs card removes one is a hidden nuance |
| 5 | Error Prevention | 1 | Entire live display is an unsignified, unconfirmed tap-to-unassign target |
| 6 | Recognition Rather Than Recall | 2 | No signifier the cards are tappable-to-remove; view toggle icon-only |
| 7 | Flexibility and Efficiency | 3 | Detailed/compact density toggle is genuinely useful |
| 8 | Aesthetic and Minimalist | 3 | Distinctive viz; IMU raw-number block + stick digits add noise |
| 9 | Error Recovery | 2 | Accidental unassign recoverable but no undo/toast affordance |
| 10 | Help and Documentation | 1 | Nothing hints that tapping the display removes the player |
| **Total** | | **24/40** | **Acceptable — distinctive craft dragged down by a hidden destructive interaction** |

## Anti-Patterns Verdict

**Does this look AI-generated? No — the opposite.** The faithful controller visualisation (correctly
positioned d-pad/face buttons, sideways-rotated single-Joy-Con layout, dual layout, shell-colour
border + shell-colour live inputs) is a bespoke hardware mirror, not a generic card template. None
of the cross-register bans apply (no side-stripes, gradient text, glass, eyebrows). Deterministic
scan: N/A (Compose is Kotlin; the markup detector returned `[]`). No browser overlay — native app.

## Overall Impression

The controller visualisation is the app's best asset: specific, on-brand, and now wearing each
controller's real colour. The single biggest problem is the **interaction model** — the entire
live display doubles as an unlabelled tap-to-unassign button with no confirmation. It reads as a
readout, behaves as a destructive control. That mismatch is the one thing holding this back.

## What's Working

1. **The hardware-faithful, shell-coloured viz.** Genuine identity; the peak moment is watching your
   own controller's buttons light up in its real colour. This is the anti-slop.
2. **Detailed/compact view modes.** Real density flexibility — 4 detailed player cards is a long
   scroll, and compact rows solve it. Thoughtful, persisted across restarts.
3. **Live feedback richness.** Presses, stick position, battery, IMU update in real time — the core
   value of the app, well executed.

## Priority Issues

- **[P1] The whole live display is a hidden, unconfirmed tap-to-unassign target.** Every card,
  player label, row, and chip has `.clickable { onUnassign(...) }` with no signifier and no
  confirmation. A user watching their inputs (the whole point of the screen) will tap and lose the
  player; a first-timer has no idea tapping does anything.
  - *Why it matters:* destructive-ish action (returns controller to the unassigned pool) on the
    primary surface, triggered accidentally, with no undo. Recoverable but jarring.
  - *Fix:* give unassign an explicit affordance — a small × in each player-card header — and make
    the big display non-destructive on tap (or require long-press / a confirm). Add an undo path
    (snackbar) if tap-to-remove stays.
  - *Command:* `/impeccable harden`

- **[P2] IMU raw-number block is high-noise, low-glanceability.** ACCEL/GYRO shown as six-digit
  signed integers ticking every frame, no units, no visualisation. Lowest value-per-pixel element,
  competing with the interactive viz.
  - *Fix:* de-emphasise (progressive disclosure / collapsible), or replace the raw ints with a small
    motion indicator; keep raw values for the enthusiast behind a toggle.
  - *Command:* `/impeccable distill`

- **[P2] Telemetry contrast likely fails WCAG AA.** IMU axis labels/values use `TextDim` at alpha
  0.6–0.7 on `CardBg`; base `TextDim` is ~6.4:1 on the background, so the alpha variants drop below
  the 4.5:1 body threshold.
  - *Fix:* raise the dimmed telemetry toward the ink end, or drop the alpha.
  - *Command:* `/impeccable audit`

- **[P2] Dual-mode unassign is unpredictable.** Tapping the player label removes *both* controllers
  (`controllers.forEach`), tapping a single Joy-Con card removes *one*. Same surface, two different
  destructive outcomes, no signifier for either.
  - *Fix:* fold into the P1 affordance — explicit per-controller remove + explicit remove-player.
  - *Command:* `/impeccable harden`

- **[P3] View-mode toggle is icon-only.** `ViewAgenda` / `ViewList` glyphs carry contentDescription
  (good for screen readers) but no visible label; the detailed-vs-compact distinction isn't obvious
  to a first-timer. Low impact — a common pattern.
  - *Command:* `/impeccable clarify`

## Persona Red Flags

**Casey (distracted, one-handed mobile — the core context: phone in one hand, controller in the
other):** the biggest victim of the P1. Steadying or scrolling the screen one-thumbed over a
full-card tap target unassigns a player. Touch targets on the viz are large (good), but the
*destructive* one is the whole card.

**Sam (accessibility / low vision):** dimmed IMU telemetry (alpha 0.6–0.7 `TextDim`) fails contrast.
Pressed states change fill + ink, not colour alone (good — survives colour-blindness). Tap-to-remove
with no accessible label announces nothing meaningful about its destructive effect.

**Riley (stress tester):** what happens with 4 detailed players (very long scroll)? Dual pair with
two different shell colours (now handled — each side lights in its own)? A controller dropping BLE
while assigned — does the viz show "reconnecting", or silently freeze on the last frame? The
connected view has no visible connection-health state; worth confirming.

## Minor Observations

- Stick coordinates render as raw 0–4096 ints — fine for the audience, but paired with the IMU
  block it's a lot of live digits.
- `ViewModeToggle` sits in the app-bar actions only when players exist — good conditional, but its
  appearance/disappearance is unheralded.

## Questions to Consider

- What if the live display were purely a display, and removal lived in one obvious control?
- Does the IMU need to show raw numbers by default, or only when an enthusiast asks for them?
- What would a "confident" version of the connection-lost state look like, mid-session?
