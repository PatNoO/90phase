# Onboarding Flow

> Complete specification for the first-time user experience in Sleep Cycle Optimizer

---

## Design Principles

1. **Every feature is a conscious opt-in** — nothing is silently enabled
2. **Skip is always available and always equal prominence** — no guilt, no re-prompting
3. **Plain language only** — no jargon, no "maximize your circadian optimization"
4. **After onboarding the app opens directly to the home screen** — done, no more setup
5. **User can change every choice later in Settings** — onboarding is not a contract

---

## Flow Structure

```
App first launch
      │
      ▼
Welcome Screen
      │
      ▼
System Permissions
  • Notification permission (Android 13+)
  • Exact alarm permission (Android 12+)
  • Alarm read permission — optional, skip available
      │
      ▼
Set Default Wake Time
  (pre-fills from system alarm if detected)
      │
      ▼
Feature Card 1: Daily Check-in
Feature Card 2: Bedtime Reminder
Feature Card 3: Morning Check-in (two switches)
Feature Card 4: Smart Wake Window
Feature Card 5: Discovery Phase (info only)
      │
      ▼
Home Screen
```

---

## Screen Specifications

### Welcome Screen

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                     🌙                                  │
│                                                         │
│          Sleep Cycle Optimizer                          │
│                                                         │
│    Calculate your ideal bedtime based on               │
│    natural 90-minute sleep cycles.                     │
│                                                         │
│    Simple. Private. No sensors required.               │
│                                                         │
│              [ Get started ]                            │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

### System Permissions Screen

Shown before feature cards. Each permission has its own row with a clear action button and a Skip option.

```
┌─────────────────────────────────────────────────────────┐
│  A few permissions                                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  🔔 Notifications                                       │
│     Required for bedtime reminders                      │
│     [ Allow ]                                           │
│                                                         │
│  ⏰ Exact alarms                                        │
│     Required for precise timing                         │
│     [ Allow ]                                           │
│                                                         │
│  📅 Read system alarm (optional)                        │
│     Lets us detect your next alarm                      │
│     and pre-fill your wake time                         │
│     [ Allow ]   [ Skip ]                               │
│                                                         │
│              [ Continue ]                               │
└─────────────────────────────────────────────────────────┘
```

---

### Default Wake Time Screen

```
┌─────────────────────────────────────────────────────────┐
│  When do you usually wake up?                           │
│                                                         │
│  We use this to calculate your bedtimes.               │
│                                                         │
│         ┌───────────────────┐                          │
│         │       07:00       │  ← clock roller          │
│         └───────────────────┘                          │
│                                                         │
│  (Pre-filled from your system alarm if detected)       │
│                                                         │
│              [ Continue ]                               │
└─────────────────────────────────────────────────────────┘
```

---

### Feature Card 1 — Daily Check-in

```
┌─────────────────────────────────────────────────────────┐
│  🌙  Daily Check-in                                     │
│                                                         │
│  Every day at 18:00 we ask when you want to wake up   │
│  tomorrow. We calculate your bedtimes. That's it.     │
│                                                         │
│  You can change the time or turn this off in           │
│  Settings at any time.                                 │
│                                                         │
│         [ Turn on ]          [ Skip ]                  │
└─────────────────────────────────────────────────────────┘
```

**If Turn on:** schedules the first daily check-in alarm.
**If Skip:** feature remains off. Never re-prompted.

---

### Feature Card 2 — Bedtime Reminder

```
┌─────────────────────────────────────────────────────────┐
│  🔔  Bedtime Reminder                                   │
│                                                         │
│  15 minutes before your chosen bedtime we send a      │
│  gentle nudge to start winding down.                  │
│                                                         │
│  Only fires on nights you've set a bedtime.           │
│                                                         │
│         [ Turn on ]          [ Skip ]                  │
└─────────────────────────────────────────────────────────┘
```

---

### Feature Card 3 — Morning Check-in

One card, two independent switches. Both default to OFF.

```
┌─────────────────────────────────────────────────────────┐
│  ☀️  Morning Check-in                                   │
│                                                         │
│  A quick optional notification each morning.          │
│  Enable what you want — or neither.                   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │  How did you sleep? (1–5)       [ ○ OFF ]       │  │
│  └─────────────────────────────────────────────────┘  │
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │  What time did you go to bed?   [ ○ OFF ]       │  │
│  └─────────────────────────────────────────────────┘  │
│                                                         │
│                   [ Continue ]                         │
└─────────────────────────────────────────────────────────┘
```

**Both OFF → no morning notification at all.**
**Rating ON, clock OFF → sends star rating notification only.**
**Both ON → star rating first, clock roller bottom sheet on tap.**

---

### Feature Card 4 — Smart Wake Window

```
┌─────────────────────────────────────────────────────────┐
│  ⏰  Smart Wake Window                                  │
│                                                         │
│  Your phone detects movement to wake you at a         │
│  lighter moment within a window you set.              │
│                                                         │
│  No microphone. No uploads. Completely local.         │
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │  ⚠️  Your phone must be on your bed to work.    │  │
│  │     If you charge your phone away from your     │  │
│  │     bed — skip this, it won't help you.         │  │
│  └─────────────────────────────────────────────────┘  │
│                                                         │
│         [ Turn on ]          [ Skip ]                  │
└─────────────────────────────────────────────────────────┘
```

**Privacy note is not fine print — it's in the card body at normal size.**

---

### Feature Card 5 — Discovery Phase (Info Only)

```
┌─────────────────────────────────────────────────────────┐
│  🔬  Discovery Phase                                    │
│                                                         │
│  After a few weeks the app can test small              │
│  adjustments to find your personal sleep cycle        │
│  length — 85, 90, or 105 minutes, for example.        │
│                                                         │
│  This is completely optional and you activate it      │
│  yourself in Settings when you're ready.              │
│                                                         │
│                   [ Got it ]                           │
└─────────────────────────────────────────────────────────┘
```

**No toggle. No action. Awareness only.**
App opens to home screen immediately after this card.

---

## Morning Notification — Full Spec

### Notification (system tray)

```
☀️  How did you sleep?
[ ⭐1 ]  [ ⭐2 ]  [ ⭐3 ]  [ ⭐4 ]  [ ⭐5 ]     [ Skip ]

Turn off these reminders
```

- Star buttons and Skip are equal size and equal visual weight
- Skip dismisses the notification and logs nothing — no follow-up
- "Turn off these reminders" is a direct link that disables the morning rating toggle in Settings

### If user taps a star → Bottom Sheet

```
┌──────────────────────────────────┐
│  What time did you go to bed?    │
│                                  │
│      [ clock roller ]            │
│                                  │
│   [ Save ]     [ Skip this ]     │
└──────────────────────────────────┘
```

- Only appears if the "bedtime log" switch is ON in Settings
- "Skip this" saves the rating without a bedtime — partial data is fine
- Maximum two interactions from notification to done

---

## Settings — Notification Toggles

All notification types are independently controllable in Settings. No bundled toggles.

```
Notifications
────────────────────────────────────────────────────
Daily check-in (18:00)               [ ON / OFF ]
  Change time: [ 18:00 ▼ ]

Bedtime reminder                     [ ON / OFF ]
  (fires 15 min before set bedtime)

Morning rating (1–5 stars)           [ ON / OFF ]
Morning bedtime log (clock roller)   [ ON / OFF ]
────────────────────────────────────────────────────
Features
────────────────────────────────────────────────────
Smart Wake Window                    [ ON / OFF ]
  (accelerometer, phone must be on bed)

Discovery Phase                      [ Activate → ]
  (available after 7+ days of data)

Pattern insights in History          [ ON / OFF ]
  (subtle cards in History tab only)

Consistency score in History         [ ON / OFF ]
  (visible in History tab only)
────────────────────────────────────────────────────
Data & Privacy
────────────────────────────────────────────────────
Firebase Sync                        [ ON / OFF ]
  Last synced: 2 min ago

Export data                          [ Export → ]
```

---

## Domain Model Changes

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

Each field maps directly to a DataStore preference key. All default to `false` — nothing is enabled without explicit user action during onboarding or in Settings.

---

*This flow ensures every user makes conscious choices about their experience. The app's default state is quiet and minimal — features opt in to being active, not opt out.*
