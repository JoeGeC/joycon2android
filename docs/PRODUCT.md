# Product

## Register

product

## Users

Android gaming and emulation enthusiasts using Nintendo Switch 2 **Joy-Con 2** and Pro Controllers
as gamepads on a phone or tablet. They are comfortable with Developer Options, Shizuku and
per-emulator config. They use the app mid-setup, at a desk or on a couch, often in a dim room and
*holding a controller in one hand*, and want to get into a game fast and then have the app stay out
of the way.

The primary jobs:
- **Connect** — pair one or more Joy-Con 2 over BLE and confirm they're live.
- **Assign** — map controllers to player slots (P1–P8), single or dual (L+R) layouts.
- **Enable output** — turn on the virtual gamepad and/or DSU motion server.
- **Configure an emulator** — one-tap write of controller/motion bindings (Eden, Dolphin).
- **Verify** — glance at live button/stick/IMU/battery state to confirm everything works.

## Product Purpose

The controllers speak a custom BLE GATT service rather than HID-over-GATT, so Android can't pair
them from Bluetooth settings. The app connects itself, presents each player as a virtual HID gamepad
through UHID, runs a DSU motion server, and writes emulator configs.

Success: SYNC to "working in my game" in well under a minute, multiplayer that just works (one device
per player), and an app trustworthy enough to leave running in the background.

## Brand Personality

**Playful gaming gear** — energetic, characterful, unmistakably *about controllers and play*,
not a generic system utility. Three words: **playful, precise, native-to-gaming**.

Personality comes from substance, not decoration — above all each controller's **real shell
colour** ([DESIGN.md](DESIGN.md#color)). Colour, motion and layout should celebrate the hardware while
staying technically credible: playful with restraint, never toy-like.

Voice: confident and direct, in the user's language (BLE, DSU, UHID, emulator names). Toggles
describe their use case rather than giving step-by-step instructions.

## Anti-references

- **Generic Material template.** Stock Material 3, default purple, no identity — reads as a
  scaffold or class project. The controller-color theming exists precisely to avoid this.
- **Consumer-app cutesy.** No blobby mascots, confetti, oversized illustrations, or gamified
  reward theatre. This is enthusiast gear, not a toy.
- **Enterprise dashboard.** Not cold, gray, corporate settings-screen density with no character.
- **Cluttered / overwhelming.** Not a wall of toggles and raw readouts with no hierarchy. Live
  data (IMU, battery, raw state) is available but must be organised, glanceable, and calm.

## Design Principles

1. **The hardware is the hero.** Real controller shell colors, faithful button/stick/IMU
   visualisation, and Switch combo/LED conventions are the identity. Design decisions should
   amplify the physical controller, not abstract it away.
2. **Fast to working, then invisible.** Optimise the SYNC → assigned → in-game path; once set
   up, the app should recede into a reliable background service.
3. **Playful, but never toy.** Energy and character come from color, motion, and the hardware
   theme — held to a tasteful, technically-credible bar.
4. **Show state, don't bury it.** Enthusiasts want to see battery, connection, and live input.
   Surface it with clear hierarchy so richness never becomes clutter.
5. **Speak the user's language.** Assume competence. Use correct domain terms (DSU, UHID,
   Shizuku, emulator names); keep copy generic and self-explanatory rather than hand-holding.

## Accessibility & Inclusion

- **Contrast (WCAG AA).** Body text ≥4.5:1, large/bold text ≥3:1 — watch muted text on the dark
  theme.
- **Reduced motion.** Any animation or live-readout motion honours the system reduced-motion
  setting with a calm fallback.
- **Large touch targets.** ≥48dp for primary controls — the app is often operated one-handed
  while the other hand holds a controller.
- **Don't rely on colour alone.** Battery and connection state pair colour with an icon or text.
