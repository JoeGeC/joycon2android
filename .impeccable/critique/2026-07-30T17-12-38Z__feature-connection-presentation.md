---
target: feature/connection/presentation
total_score: 31
p0_count: 0
p1_count: 0
timestamp: 2026-07-30T17-12-38Z
slug: feature-connection-presentation
---
# Critique — feature/connection/presentation

Native Jetpack Compose. Deterministic detector N/A (Kotlin, not markup; returned `[]`).
Fully-connected surfaces not visually verified (require paired Joy-Con 2). Re-run after the
typeset / colorize / harden / adapt / optimize / polish arc.

## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Rich live feedback + undo snackbar; mid-session connection loss still not surfaced |
| 2 | Match System / Real World | 3 | Viz mirrors hardware in real shell colours; raw IMU ints still unit-less |
| 3 | User Control and Freedom | 4 | Every unassign is undoable (swipe-dismiss snackbar), re-assignable |
| 4 | Consistency and Standards | 4 | Uniform white glyphs, one type scale, tokenised dimens, consistent remove affordance |
| 5 | Error Prevention | 3 | Live display still tap-to-remove, but now signified (×) and fully recoverable (undo) |
| 6 | Recognition Rather Than Recall | 3 | × signifier + two-state toggle removed the main recall gaps |
| 7 | Flexibility and Efficiency | 3 | Detailed/compact switch, persisted |
| 8 | Aesthetic and Minimalist | 3 | Cohesive type + colour hierarchy; IMU raw-number block still noisy (kept by choice) |
| 9 | Error Recovery | 3 | Undo snackbar recovers the unassign action |
| 10 | Help and Documentation | 2 | Inline cues (×, snackbar) but no help system |
| **Total** | | **31/40** | **Good — up from 24 (Acceptable)** |

## Anti-Patterns Verdict

**Pass — not AI-generated.** The bespoke hardware visualisation now wears each controller's real
shell colour through its live inputs, over a real Material type scale and tokenised dimensions.
No cross-register bans. Detector N/A (Kotlin); no browser overlay (native app).

## Overall Impression

The interaction-model flaw that anchored the last critique — an unsignified, unconfirmed
tap-to-unassign on the primary display — is resolved: it's now signposted with a × and every
removal is a swipe-away undo. Combined with the identity (shell-coloured inputs), AA-clean
telemetry, 48dp targets, and a tidy token system, the feature reads as considered, not generated.
What's left is second-order: connection-health visibility, the raw IMU block's density, and no
help affordance.

## What's Working

1. **Identity + hierarchy.** Controllers light up in their own colour; telemetry values pop
   (bright) over dim labels; the type scale gives real hierarchy. Distinctive and legible.
2. **Safe, reversible removal.** Signifier + undo snackbar turned a destructive mystery tap into a
   forgiving, discoverable action.
3. **Systematic under the hood.** Fully tokenised, AA contrast verified, 48dp touch targets, and a
   render hot path that no longer churns allocations.

## Priority Issues

- **[P2] No mid-session connection-health state.** If an assigned controller drops BLE, the live
  view has no visible "reconnecting/lost" indicator (source-level inference; needs hardware).
  *Command:* `/impeccable harden`
- **[P2] IMU raw-number block density.** Deliberately kept (enthusiast readout) but still the
  noisiest element; a collapsible / calmer treatment would lift Aesthetic. *Command:* `/impeccable distill`
- **[P2, unverified] Dual layout on ≤320dp / 200% font.** Two full controller cards in `weight(1f)`
  halves with fixed-dp glyphs may crowd. Needs a device check. *Command:* `/impeccable adapt`
- **[P3] No help affordance.** Self-documenting now, but no discoverable help for first-timers.

## Persona Red Flags

**Casey (one-handed mobile):** the P1 that hurt them is gone — a stray tap is now an undo away,
targets are ≥48dp, the view toggle is a single easy switch.

**Sam (low vision):** telemetry + battery now clear WCAG AA; states use fill + ink, not colour
alone. Remaining: the removable card still lacks an explicit semantic role/announcement.

**Riley (stress tester):** open questions remain at the edges — controller drop mid-session, 4
detailed players scroll length, extreme small screens / large font.

## Minor Observations
- Stick coordinates + IMU are still a lot of live digits; tabular figures help alignment.
- The removable player card is a bare `clickable` — an explicit a11y role would aid screen readers.

## Questions to Consider
- What should the connected view show the moment a controller drops?
- Does the IMU need raw numbers on screen by default, or behind a disclosure?
- Would an explicit "remove" semantic role make the tap-to-remove screen-reader-friendly?
