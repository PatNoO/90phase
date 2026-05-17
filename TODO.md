# TODO — Pending Manual Steps

## Urbanist Font (PH-14)

The Night Sky design system uses **Urbanist** as its font family. Currently `SleepTypography` falls back to
`FontFamily.Default` until the font files are added.

### How to add Urbanist

1. Download the Urbanist font from [Google Fonts](https://fonts.google.com/specimen/Urbanist)
2. Place the following TTF files in `presentation/src/main/res/font/`:
   - `urbanist_light.ttf` (weight 300)
   - `urbanist_regular.ttf` (weight 400)
   - `urbanist_medium.ttf` (weight 500)
   - `urbanist_semibold.ttf` (weight 600)
3. In `presentation/src/main/kotlin/com/example/a90phase/presentation/theme/Type.kt`,
   replace the placeholder with:
   ```kotlin
   import androidx.compose.ui.text.font.Font
   import com.example.a90phase.presentation.R

   private val Urbanist = FontFamily(
       Font(R.font.urbanist_light, FontWeight.Light),
       Font(R.font.urbanist_regular, FontWeight.Normal),
       Font(R.font.urbanist_medium, FontWeight.Medium),
       Font(R.font.urbanist_semibold, FontWeight.SemiBold),
   )
   ```
4. Remove the `// TODO` comment block above the `private val Urbanist` line.

---


## Notification Channel Sounds (PH-32)

The AC for PH-32 mentions "soft chime" for Daily Check-in and "gentle bell" for Bedtime Reminder. These are intent descriptions — no audio assets were added as part of PH-32.

### If custom sounds are needed later

1. Obtain audio files (OGG or WAV recommended)
2. Place them in `app/src/main/res/raw/`, e.g. `soft_chime.ogg` and `gentle_bell.ogg`
3. In `NotificationChannels.kt`, apply the sound to each channel **before** it is first registered (Android ignores sound changes to already-registered channels unless the user clears app data):
   ```kotlin
   val chimeUri = Uri.parse("android.resource://${context.packageName}/${R.raw.soft_chime}")
   val audioAttributes = AudioAttributes.Builder()
       .setUsage(AudioAttributes.USAGE_NOTIFICATION)
       .build()
   dailyCheckin.setSound(chimeUri, audioAttributes)
   ```
4. Uninstall and reinstall the app to force channel re-creation with the new sound.

> **Note:** `POST_NOTIFICATIONS` permission (Android 13+) is handled separately in PH-38.

---


## Daily Check-in Scheduling Not Yet Wired (PH-33)

`DailyCheckInScheduler.schedule()` exists but is not called from anywhere yet. The alarm will not fire until something triggers it.

### Where to wire it

Call `DailyCheckInScheduler.schedule(reminderTime)` in two places:

1. **On first app launch / onboarding completion** — in the onboarding ViewModel or `NinetyPhaseApplication.onCreate()` (guarded by `dailyCheckInEnabled` from DataStore)
2. **When the user changes the reminder time in Settings** — in the Settings ViewModel after calling `setReminderTime()`

Until this is wired, the only way to test the receiver manually is via adb:
```bash
adb shell am broadcast -a com.example.a90phase.DAILY_CHECKIN \
  -n com.example.a90phase/.notifications.DailyCheckInReceiver
```

> **Note:** BootReceiver (PH-37) will re-schedule the alarm after device reboot, but the initial schedule still needs to be triggered from app code.

---


## Notable Architecture Decisions (PH-15)

- **StarRating uses unicode ★/☆** — `material-icons-core` is not a transitive dep of `material3`, so star icons would need a new Gradle dependency. Unicode characters render identically and require nothing extra.
- **BedtimeQuality reuses the domain enum** — `BedtimeQuality` already existed in `domain/entities/BedtimeRecommendation.kt`. No duplicate enum was created in the presentation layer.
- **`@Preview` functions are `internal`** — detekt's `UnusedPrivateMember` rule flags private `@Preview` composables. Making them `internal` satisfies the rule without suppression annotations.
- **Notification receivers inject DataStore directly** (PH-33) — `DailyCheckInReceiver` and `DisableCheckInReceiver` inject `UserPreferencesDataStore` rather than the domain `UserPreferencesRepository`. This avoids adding `implementation(project(":domain"))` to `:app`, since `:domain` is not transitively exposed from `:data` (which uses `implementation`, not `api`).
