# Product

## Register

product

## Users

Android gaming and emulation enthusiasts who want to use Nintendo Switch 2 **Joy-Con 2**
controllers as system-wide gamepads on their phone or tablet. They are comfortable with
Developer Options, Shizuku, and per-emulator config — this is not a mainstream consumer
audience. Their context is hands-on: often mid-setup at a desk or on a couch, frequently
*holding a controller in one hand* while operating the app with the other, wanting to get
connected and into a game (or emulator) with as little friction as possible and then have
the app stay out of the way.

The primary jobs, screen by screen:
- **Connect** — pair one or more Joy-Con 2 over BLE and confirm they're live.
- **Assign** — map controllers to player slots (P1–P4), single or dual (L+R) layouts.
- **Enable output** — turn on the virtual gamepad and/or DSU motion server.
- **Configure an emulator** — one-tap write of controller/motion bindings (Eden, Dolphin).
- **Verify** — glance at live button/stick/IMU/battery state to confirm everything works.

## Product Purpose

Joy-Con 2 controllers speak BLE over a custom GATT service rather than standard
HID-over-GATT, so Android cannot pair them through normal Bluetooth settings. Joycon2Android
bridges that gap: it connects over GATT, sends the vendor init sequence, parses raw
notification packets, and exposes each assigned player as its own standard virtual HID
gamepad via UHID (through Shizuku's privileged path). It also runs a DSU motion server so
emulators get gyro/accel, and can write emulator controller configs directly.

Success is: a controller goes from SYNC-button to "working in my game/emulator" in well
under a minute, multiplayer "just works" (one distinct device per player), and the app is
trustworthy enough to leave running in the background without a second thought.

## Brand Personality

**Playful gaming gear** — energetic, characterful, unmistakably *about controllers and play*,
not a generic system utility. Three words: **playful, precise, native-to-gaming**.

The personality is carried by substance, not decoration: the standout identity move is that
each controller's card border is drawn from the controller's **real shell color** read out of
SPI flash (saturation-boosted so it reads on the dark UI). The design should lean into that —
color, motion, and layout that celebrate the hardware — while staying tasteful and technically
credible. Playful with restraint, never toy-like.

Voice: confident and direct, speaks the user's language (BLE, DSU, UHID, emulator names) without
over-explaining. Copy is generic and self-explanatory — toggles describe their use case rather
than giving app-specific step-by-step instructions.

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

- **Contrast (WCAG AA).** Body text ≥4.5:1, large/bold text ≥3:1 against its background — a real
  risk on the dark theme with muted-gray text (`TextDim #8B98A5`); verify and bump toward ink
  where borderline.
- **Reduced motion.** Any animation or live-readout motion honours the system reduced-motion
  setting with a calm fallback.
- **Large touch targets.** ≥48dp for primary controls — the app is often operated one-handed
  while the other hand holds a controller.
- **Don't rely on color alone.** Battery and connection state pair color with icon/text so
  status survives color-blindness (currently battery is color-only via `batteryColor()`).
