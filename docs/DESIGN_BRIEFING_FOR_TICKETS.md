# Design Briefing for Ticket Handler
> Temporary file — delete after GitHub tickets have been updated.
>
> Read this before creating or updating any GitHub issues for 90phase.
> The UI design has been significantly refined since the initial tickets were written.
> All implementation tickets must reference the updated specs below.

---

## What Changed (Summary)

1. **Color system updated** — CyanGlow dimmed from `#00D9FF` → `#00B8D9`. Less neon, more premium.
2. **Nebula palette added** — Three new colors: `NebulaViolet #7C3AED`, `NebulaPurple #4C1D95`, `NebulaDeep #1E1040`. Used on all onboarding screens.
3. **Star sparkle background** — Main app screens (Calculator, History, Settings) get a `StarFieldBackground` composable with ~40 subtle white dots on Canvas. Not on onboarding.
4. **Onboarding redesigned** — 8 screens total (was 3 in early tickets). All feature cards now share one unified template: nebula background + centered icon with glow bloom + floating text + single toggle. No separate warning cards.
5. **Onboarding screen count** — Welcome → Permissions → Wake Time → Daily Check-in → Bedtime Reminder → Morning Check-in → Smart Wake Window → Discovery Phase → Home (8 screens, not 5).
6. **Smart Wake Window onboarding card** — now uses a SleepToggle (not a Turn on / Skip button pair). Warning text is inline body text, not a separate card.
7. **Morning Check-in** — Two independent toggles on one screen: sleep quality rating AND bedtime log. Neither is on by default. Neither requires the other.
8. **Icon family** — Material Symbols Rounded throughout. Each onboarding screen has a specific icon (see table below).

---

## Source of Truth Files

Read these in order before touching any ticket:

| File | What it covers |
|---|---|
| `docs/UI_SPECIFICATIONS.md` | Complete screen layouts, color tokens, typography, components, animations, navigation graph |
| `docs/ONBOARDING_FLOW.md` | Onboarding logic, UX rules, domain model (`UserOnboardingState`) |
| `docs/PROJECT_OVERVIEW.md` | Full feature set, algorithm, architecture, roadmap |
| `CLAUDE.md` | Code rules, architecture constraints, commit format, PR template |

---

## Onboarding Screens — Quick Reference

| # | Route / Screen | Icon | Glow Color | Action |
|---|---|---|---|---|
| 1 | Welcome | `nightlight` | `CyanGlow` | Get started button |
| 2 | Permissions | `shield` | `CyanGlow` | 3 permission rows + Continue button |
| 3 | Wake Time | `alarm` | `CyanGlow` | Time picker (88sp) + Continue button |
| 4 | Daily Check-in | `edit_calendar` | `CyanGlow` | SleepToggle (defaults OFF) |
| 5 | Bedtime Reminder | `bedtime` | `CyanGlow` | SleepToggle (defaults OFF) |
| 6 | Morning Check-in | `wb_sunny` | `GoodAmber` | Two independent SleepToggles + Continue |
| 7 | Smart Wake Window | `vibration` | `CyanGlow` | SleepToggle (defaults OFF) |
| 8 | Discovery Phase | `auto_awesome` | `IndigoGlow` | Got it button (no toggle) |

All screens: nebula night-sky background (`OnboardingBackgroundGradient` + `OnboardingNebulaWash`), no star sparkles.

---

## Color Tokens — Updated Values

```kotlin
// Use these exact values — not the older versions from earlier tickets
val CyanGlow     = Color(0xFF00B8D9)   // was 0xFF00D9FF
val NebulaViolet = Color(0xFF7C3AED)   // NEW
val NebulaPurple = Color(0xFF4C1D95)   // NEW
val NebulaDeep   = Color(0xFF1E1040)   // NEW
```

---

## Domain Model — Onboarding State

```kotlin
data class UserOnboardingState(
    val isCompleted: Boolean,
    val dailyCheckInEnabled: Boolean,
    val bedtimeReminderEnabled: Boolean,
    val morningRatingEnabled: Boolean,
    val morningBedtimeLogEnabled: Boolean,
    val smartWakeWindowEnabled: Boolean,
    val discoveryPhaseInfoShown: Boolean
)
```

All fields default to `false`. Nothing is enabled without explicit user action.

---

## Key Rules for Implementers

- **Never bundle the morning toggles** — `morningRatingEnabled` and `morningBedtimeLogEnabled` are independent. Toggling one must never affect the other.
- **Discovery Phase is locked until 7+ rating days** — show a lock icon and grey out the row in Settings.
- **Smart Wake Window warning** — the "phone must be on bed" text must be normal body size (16sp), not small print. This is a design requirement from `UI_SPECIFICATIONS.md`.
- **No star sparkles on onboarding** — the nebula wash replaces them.
- **Skip is always equal visual weight to Turn on** — never a ghost/disabled-looking secondary button. Use `SecondaryButton` from the spec.
- **After onboarding → Calculator screen** — pop the entire onboarding backstack with `popUpTo(SPLASH) { inclusive = true }`.

---

## Mockup Images

Pencil mockups have been created for all 11 screens (Calculator, History, Settings, 8 onboarding screens).
Images will be added to GitHub tickets in a separate pass — tickets can be created without them first.

Node IDs in the Pencil file for reference:
- Calculator: `bi8Au`
- Welcome: `wl3eA`
- Permissions: `a56fs`
- Wake Time: `J2rrSx`
- Daily Check-in: `m5Yq6`
- Bedtime Reminder: `WQXzW`
- Morning Check-in: `iZybG`
- Smart Wake Window: `wstdl`
- Discovery Phase: `BSzy3`
- History: `olXFq`
- Settings: `oDZSQ`

---

---

## New Backlog Feature: Contextual Avatar

> Create a post-MVP backlog ticket for this. No implementation yet — concept only.

A small illustrated avatar that lives in the app and changes expression based on context. No user input required — it reacts automatically to time of day and sleep data already logged.

### Core idea
The avatar is a quiet companion, not a gamification mechanic. It gives the app personality without adding noise. It should feel tasteful and small, not Bitmoji-level expressive.

### Avatar states (time-based — no action needed)

| Context | Expression |
|---|---|
| Mid-day check (e.g. 14:00) | Neutral, calm — nothing to do right now |
| 18:00 daily check-in notification | Attentive, leaning forward — ready to plan your night |
| Approaching bedtime (bedtime reminder fires) | Yawning, cozy, eyes drooping |
| Past optimal bedtime (late, should be sleeping) | Eyes half-shut, slightly guilty |
| Wake-up time window (morning) | Stretching, squinting, just woken up |

### Avatar states (reaction-based — after user action)

| Context | Expression |
|---|---|
| User picks an optimal bedtime | Happy, relieved, small nod |
| Morning rating logged 4–5 stars | Glowing, refreshed, energetic |
| Morning rating logged 1–2 stars | Droopy, empathetic, no judgment |
| User skips morning rating | Shrug — totally fine |
| Discovery Phase active | Tiny scientist accessory (goggles or clipboard) |

### Where it appears
- **Calculator screen** — top area, reacts to time of day
- **Notification icon** — face changes per notification type (check-in, bedtime, morning)
- **History summary card** — mood reflects the week's average rating
- **Morning rating bottom sheet** — curious face, "how was it?"

### Design constraints
- Keeps the "quiet, private" brand — subtle illustration, not a cartoon character
- No extra user setup needed — avatar works from day one with zero configuration
- Customization (skin tone, hair, pajama style) is optional, not required for MVP of the feature
- Small footprint in the UI — never takes over the screen, always secondary to the data

### Suggested ticket
- Title: `[Feature] Contextual avatar companion`
- Phase: Post-MVP backlog
- Labels: `enhancement`, `design`, `post-mvp`

---

*Delete this file after the GitHub tickets have been updated.*
