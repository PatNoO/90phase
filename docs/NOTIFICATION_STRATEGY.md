# Notification Strategy

> Complete specification for all notification types, scheduling, and user interaction flows

---

## Table of Contents
1. [Notification Design Principles](#notification-design-principles)
2. [Overview](#overview)
3. [Notification Channels](#notification-channels)
4. [Daily Check-in (18:00)](#daily-check-in-1800)
5. [Bedtime Reminder (Dynamic)](#bedtime-reminder-dynamic)
6. [Morning Feedback (Wake-up)](#morning-feedback-wake-up)
7. [Implementation Details](#implementation-details)
8. [Testing Strategy](#testing-strategy)

---

## Notification Design Principles

These rules apply to every notification in the app:

1. **Every notification has a Skip/Dismiss path with equal visual prominence** — never hidden, never guilt-trippy ("Skip anyway?"), just Skip.
2. **Each notification type has an independent toggle in Settings** — no hunting through system settings.
3. **"Turn off these reminders" is a direct link in every notification** — one tap to silence that notification type forever without opening the app.
4. **Partial data is always acceptable** — if a user rates their sleep but skips the bedtime log, that's fine. If they skip everything, that's fine too.
5. **No re-prompting** — if a user skips a feature in onboarding or dismisses a notification, the app does not ask again.

---

## Overview

### Notification Flow Timeline

```
┌────────────────────────────────────────────────────────────┐
│                    DAILY NOTIFICATION CYCLE                │
└────────────────────────────────────────────────────────────┘

18:00 ───► Daily Check-in Notification
           "När ska du vakna imorgon?"
           │
           ├─► User selects wake time (e.g., 07:00)
           │
           └─► App calculates bedtimes:
               • 21:45 (6 cycles - optimal)
               • 23:15 (5 cycles)
               • 00:45 (4 cycles)

21:30 ───► Bedtime Reminder (if user selected 21:45)
           "Dags att varva ner"
           (Triggered 15 min before selected bedtime)

07:00 ───► [User's alarm goes off - not our app]

07:15 ───► Morning Feedback Notification
           "Hur kändes sömnen?"
           (Triggered 15 min after wake time)
           │
           └─► User rates sleep (1-5 stars)
               │
               └─► If Discovery Phase active:
                   Analyze rating & adjust parameters
```

---

## Notification Channels

### Channel Definitions

```kotlin
// data/local/notifications/NotificationChannels.kt
object NotificationChannels {
    const val DAILY_CHECK_IN_ID = "daily_check_in"
    const val BEDTIME_REMINDER_ID = "bedtime_reminder"
    const val MORNING_FEEDBACK_ID = "morning_feedback"
    
    fun createChannels(context: Context) {
        val notificationManager = context.getSystemService<NotificationManager>()
        
        val channels = listOf(
            NotificationChannel(
                DAILY_CHECK_IN_ID,
                "Daglig sömnplanering",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Påminnelse kl 18:00 om att planera din sömn"
                enableLights(true)
                lightColor = Color.CYAN
                setSound(
                    Uri.parse("android.resource://${context.packageName}/raw/soft_chime"),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            },
            
            NotificationChannel(
                BEDTIME_REMINDER_ID,
                "Sänggångspåminnelse",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Påminnelse när det är dags att börja varva ner"
                enableLights(true)
                lightColor = Color.BLUE
                setSound(
                    Uri.parse("android.resource://${context.packageName}/raw/gentle_bell"),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            },
            
            NotificationChannel(
                MORNING_FEEDBACK_ID,
                "Morgonfeedback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Begär feedback om sömnkvalitet"
                enableLights(false)
                setSound(null, null) // Silent - user just woke up
            }
        )
        
        notificationManager?.createNotificationChannels(channels)
    }
}
```

---

## Daily Check-in (18:00)

### Purpose
Proactively ask user when they want to wake up tomorrow, then provide bedtime recommendations.

### Scheduling

```kotlin
// data/local/notifications/DailyCheckInScheduler.kt
class DailyCheckInScheduler @Inject constructor(
    private val alarmManager: AlarmManager,
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun scheduleCheckIn() {
        val reminderTime = userPreferencesRepository.getReminderTime() // "18:00"
        val (hour, minute) = reminderTime.split(":").map { it.toInt() }
        
        val intent = Intent(context, DailyCheckInReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_CHECK_IN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate next 18:00
        val now = LocalDateTime.now()
        var scheduledTime = LocalDateTime.of(
            now.toLocalDate(),
            LocalTime.of(hour, minute)
        )
        
        if (scheduledTime.isBefore(now)) {
            // If it's already past 18:00 today, schedule for tomorrow
            scheduledTime = scheduledTime.plusDays(1)
        }
        
        val triggerAtMillis = scheduledTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        // Use setExactAndAllowWhileIdle for precise timing even in Doze mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        
        Timber.d("Daily check-in scheduled for $scheduledTime")
    }
    
    fun cancelCheckIn() {
        val intent = Intent(context, DailyCheckInReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_CHECK_IN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
```

### Broadcast Receiver

```kotlin
// data/local/notifications/DailyCheckInReceiver.kt
class DailyCheckInReceiver : BroadcastReceiver() {
    
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var alarmRepository: AlarmRepository
    @Inject lateinit var scheduler: DailyCheckInScheduler
    
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as SleepOptimizerApplication)
            .appComponent
            .inject(this)
        
        // Launch coroutine to fetch system alarm
        CoroutineScope(Dispatchers.IO).launch {
            val systemAlarm = alarmRepository.getNextAlarm()
            showCheckInNotification(context, systemAlarm)
            
            // Reschedule for tomorrow
            scheduler.scheduleCheckIn()
        }
    }
    
    private fun showCheckInNotification(
        context: Context,
        systemAlarm: SystemAlarm?
    ) {
        val notification = NotificationCompat.Builder(context, DAILY_CHECK_IN_ID)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle("Sleep Cycle Optimizer")
            .setContentText("När ska du vakna imorgon?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                if (systemAlarm != null) {
                    // User has a system alarm - offer quick action
                    val alarmTime = systemAlarm.time.atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                    
                    addAction(
                        R.drawable.ic_alarm,
                        "$alarmTime (Från larm)",
                        createUseSystemAlarmIntent(context, systemAlarm.time)
                    )
                    
                    addAction(
                        R.drawable.ic_edit,
                        "Anpassa",
                        createCustomTimeIntent(context)
                    )
                } else {
                    // No system alarm - just open to time picker
                    setContentIntent(createCustomTimeIntent(context))
                }
            }
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_DAILY_CHECK_IN, notification)
    }
    
    private fun createUseSystemAlarmIntent(
        context: Context,
        wakeTime: Instant
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_WAKE_TIME, wakeTime.toEpochMilli())
            putExtra(EXTRA_AUTO_CALCULATE, true)
        }
        
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_USE_SYSTEM_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createCustomTimeIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_SHOW_TIME_PICKER, true)
        }
        
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CUSTOM_TIME,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
```

---

## Bedtime Reminder (Dynamic)

### Purpose
Remind user 15 minutes before their selected optimal bedtime to start winding down.

### Scheduling (One-Time)

```kotlin
// data/local/notifications/BedtimeReminderScheduler.kt
class BedtimeReminderScheduler @Inject constructor(
    private val alarmManager: AlarmManager,
    private val context: Context
) {
    fun scheduleBedtimeReminder(bedtime: LocalTime) {
        // Cancel any existing bedtime reminder first
        cancelBedtimeReminder()
        
        // Calculate reminder time (15 min before bedtime)
        val reminderTime = bedtime.minusMinutes(15)
        
        val intent = Intent(context, BedtimeReminderReceiver::class.java).apply {
            putExtra(EXTRA_BEDTIME, bedtime.format(DateTimeFormatter.ofPattern("HH:mm")))
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BEDTIME_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate trigger time for today/tonight
        val now = LocalDateTime.now()
        var scheduledDateTime = LocalDateTime.of(now.toLocalDate(), reminderTime)
        
        if (scheduledDateTime.isBefore(now)) {
            // If reminder time already passed today, schedule for tomorrow
            scheduledDateTime = scheduledDateTime.plusDays(1)
        }
        
        val triggerAtMillis = scheduledDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        
        Timber.d("Bedtime reminder scheduled for $scheduledDateTime (bedtime: $bedtime)")
    }
    
    fun cancelBedtimeReminder() {
        val intent = Intent(context, BedtimeReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BEDTIME_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
```

### Broadcast Receiver

```kotlin
// data/local/notifications/BedtimeReminderReceiver.kt
class BedtimeReminderReceiver : BroadcastReceiver() {
    
    @Inject lateinit var notificationManager: NotificationManager
    
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as SleepOptimizerApplication)
            .appComponent
            .inject(this)
        
        val bedtime = intent.getStringExtra(EXTRA_BEDTIME) ?: "unknown"
        showBedtimeNotification(context, bedtime)
    }
    
    private fun showBedtimeNotification(context: Context, bedtime: String) {
        val notification = NotificationCompat.Builder(context, BEDTIME_REMINDER_ID)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle("Dags att varva ner")
            .setContentText("För optimal sömn, lägg dig klockan $bedtime")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("För optimal sömn, lägg dig klockan $bedtime\n\n" +
                            "Lägg undan skärmar och börja varva ner nu.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check,
                "Jag ligger redan",
                createDismissIntent(context)
            )
            .addAction(
                R.drawable.ic_snooze,
                "Påminn om 10 min",
                createSnoozeIntent(context, bedtime)
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BEDTIME_REMINDER, notification)
    }
    
    private fun createSnoozeIntent(context: Context, bedtime: String): PendingIntent {
        val intent = Intent(context, BedtimeReminderReceiver::class.java).apply {
            putExtra(EXTRA_BEDTIME, bedtime)
            putExtra(EXTRA_IS_SNOOZE, true)
        }
        
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BEDTIME_SNOOZE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
```

---

## Morning Feedback (Wake-up)

### Purpose
Collect sleep quality rating for Discovery Phase analysis and user history.

### Scheduling

```kotlin
// data/local/notifications/MorningFeedbackScheduler.kt
class MorningFeedbackScheduler @Inject constructor(
    private val alarmManager: AlarmManager,
    private val context: Context
) {
    fun scheduleMorningFeedback(wakeTime: LocalTime, sleepLogId: String) {
        // Schedule 15 minutes after wake time
        val feedbackTime = wakeTime.plusMinutes(15)
        
        val intent = Intent(context, MorningFeedbackReceiver::class.java).apply {
            putExtra(EXTRA_SLEEP_LOG_ID, sleepLogId)
            putExtra(EXTRA_WAKE_TIME, wakeTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MORNING_FEEDBACK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate trigger time
        val now = LocalDateTime.now()
        var scheduledDateTime = LocalDateTime.of(now.toLocalDate(), feedbackTime)
        
        if (scheduledDateTime.isBefore(now)) {
            // If already past feedback time today, schedule for tomorrow
            scheduledDateTime = scheduledDateTime.plusDays(1)
        }
        
        val triggerAtMillis = scheduledDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        
        Timber.d("Morning feedback scheduled for $scheduledDateTime")
    }
    
    fun cancelMorningFeedback() {
        val intent = Intent(context, MorningFeedbackReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MORNING_FEEDBACK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
```

### Interactive Notification with Rating

```kotlin
// data/local/notifications/MorningFeedbackReceiver.kt
class MorningFeedbackReceiver : BroadcastReceiver() {
    
    @Inject lateinit var notificationManager: NotificationManager
    
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as SleepOptimizerApplication)
            .appComponent
            .inject(this)
        
        val sleepLogId = intent.getStringExtra(EXTRA_SLEEP_LOG_ID) ?: return
        showMorningFeedbackNotification(context, sleepLogId)
    }
    
    private fun showMorningFeedbackNotification(context: Context, sleepLogId: String) {
        // Create a custom RemoteViews layout for star rating
        val notificationLayout = RemoteViews(
            context.packageName,
            R.layout.notification_morning_feedback
        )
        
        // Set up click listeners for each star (1-5)
        for (rating in 1..5) {
            val ratingIntent = createRatingIntent(context, sleepLogId, rating)
            notificationLayout.setOnClickPendingIntent(
                getRatingButtonId(rating),
                ratingIntent
            )
        }
        
        val notification = NotificationCompat.Builder(context, MORNING_FEEDBACK_ID)
            .setSmallIcon(R.drawable.ic_sun)
            .setContentTitle("God morgon!")
            .setContentText("Hur kändes sömnen?")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_later,
                "Betygsätt senare",
                createOpenAppIntent(context, sleepLogId)
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_MORNING_FEEDBACK, notification)
    }
    
    private fun createRatingIntent(
        context: Context,
        sleepLogId: String,
        rating: Int
    ): PendingIntent {
        val intent = Intent(context, RatingActionReceiver::class.java).apply {
            putExtra(EXTRA_SLEEP_LOG_ID, sleepLogId)
            putExtra(EXTRA_RATING, rating)
        }
        
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RATING_BASE + rating,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

// Separate receiver to handle rating submission
class RatingActionReceiver : BroadcastReceiver() {
    
    @Inject lateinit var logSleepSessionUseCase: LogSleepSessionUseCase
    @Inject lateinit var notificationManager: NotificationManager
    
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as SleepOptimizerApplication)
            .appComponent
            .inject(this)
        
        val sleepLogId = intent.getStringExtra(EXTRA_SLEEP_LOG_ID) ?: return
        val rating = intent.getIntExtra(EXTRA_RATING, 0)
        
        // Save rating asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            logSleepSessionUseCase.updateRating(sleepLogId, rating)
            
            // Dismiss notification
            notificationManager.cancel(NOTIFICATION_ID_MORNING_FEEDBACK)
            
            // Show confirmation
            showRatingConfirmation(context, rating)
        }
    }
    
    private fun showRatingConfirmation(context: Context, rating: Int) {
        val message = when (rating) {
            in 1..2 -> "Synd att höra. Vi justerar dina sömncykler."
            3 -> "Tack för feedbacken!"
            in 4..5 -> "Fantastiskt! Fortsätt så här."
            else -> "Tack!"
        }
        
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
```

---

## Implementation Details

### Permissions

**AndroidManifest.xml:**
```xml
<!-- Exact alarm scheduling (Android 12+) -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- Notification permission (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Read system alarms (Android 12+) -->
<uses-permission android:name="android.permission.READ_ALARM" />

<!-- Receivers -->
<receiver
    android:name=".data.local.notifications.DailyCheckInReceiver"
    android:enabled="true"
    android:exported="false" />

<receiver
    android:name=".data.local.notifications.BedtimeReminderReceiver"
    android:enabled="true"
    android:exported="false" />

<receiver
    android:name=".data.local.notifications.MorningFeedbackReceiver"
    android:enabled="true"
    android:exported="false" />

<receiver
    android:name=".data.local.notifications.RatingActionReceiver"
    android:enabled="true"
    android:exported="false" />

<!-- Boot receiver to reschedule notifications after device restart -->
<receiver
    android:name=".data.local.notifications.BootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

### Boot Receiver

```kotlin
// data/local/notifications/BootReceiver.kt
class BootReceiver : BroadcastReceiver() {
    
    @Inject lateinit var dailyCheckInScheduler: DailyCheckInScheduler
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            (context.applicationContext as SleepOptimizerApplication)
                .appComponent
                .inject(this)
            
            // Reschedule daily check-in
            CoroutineScope(Dispatchers.IO).launch {
                val notificationsEnabled = userPreferencesRepository
                    .getUserProfile()
                    .notificationsEnabled
                
                if (notificationsEnabled) {
                    dailyCheckInScheduler.scheduleCheckIn()
                    Timber.d("Notifications rescheduled after boot")
                }
            }
        }
    }
}
```

### Permission Request Flow

```kotlin
// presentation/viewmodels/OnboardingViewModel.kt
class OnboardingViewModel @Inject constructor(
    private val dailyCheckInScheduler: DailyCheckInScheduler
) : ViewModel() {
    
    val permissionState = MutableStateFlow(PermissionState())
    
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATION_PERMISSION
            )
        }
    }
    
    fun onPermissionGranted() {
        viewModelScope.launch {
            permissionState.value = permissionState.value.copy(
                notificationGranted = true
            )
            
            // Schedule first check-in
            dailyCheckInScheduler.scheduleCheckIn()
        }
    }
    
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService<AlarmManager>()
            
            if (alarmManager?.canScheduleExactAlarms() == false) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }
    }
}
```

---

## Testing Strategy

### Unit Tests

```kotlin
@RunWith(JUnit4::class)
class DailyCheckInSchedulerTest {
    
    private lateinit var alarmManager: AlarmManager
    private lateinit var context: Context
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var scheduler: DailyCheckInScheduler
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        alarmManager = mock()
        userPreferencesRepository = FakeUserPreferencesRepository()
        scheduler = DailyCheckInScheduler(alarmManager, context, userPreferencesRepository)
    }
    
    @Test
    fun `scheduleCheckIn sets alarm for 18_00 today if before that time`() = runTest {
        // Given: Current time is 15:00
        val now = LocalDateTime.of(2024, 5, 10, 15, 0)
        
        // When
        scheduler.scheduleCheckIn()
        
        // Then
        val expectedTime = LocalDateTime.of(2024, 5, 10, 18, 0)
        verify(alarmManager).setExactAndAllowWhileIdle(
            eq(AlarmManager.RTC_WAKEUP),
            eq(expectedTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
            any()
        )
    }
    
    @Test
    fun `scheduleCheckIn sets alarm for 18_00 tomorrow if after that time`() = runTest {
        // Given: Current time is 20:00
        val now = LocalDateTime.of(2024, 5, 10, 20, 0)
        
        // When
        scheduler.scheduleCheckIn()
        
        // Then: Should schedule for next day at 18:00
        val expectedTime = LocalDateTime.of(2024, 5, 11, 18, 0)
        verify(alarmManager).setExactAndAllowWhileIdle(
            eq(AlarmManager.RTC_WAKEUP),
            eq(expectedTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
            any()
        )
    }
}
```

### Integration Tests

```kotlin
@RunWith(AndroidJUnit4::class)
@MediumTest
class NotificationFlowIntegrationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `complete notification flow from check-in to morning feedback`() = runTest {
        // 1. Trigger daily check-in
        val checkInIntent = Intent(context, DailyCheckInReceiver::class.java)
        context.sendBroadcast(checkInIntent)
        
        // Verify notification shown
        composeTestRule.waitUntil(timeout = 5000) {
            getNotificationCount() > 0
        }
        
        // 2. User selects wake time via notification action
        val wakeTime = LocalTime.of(7, 0)
        simulateNotificationAction(wakeTime)
        
        // 3. Verify bedtime reminder scheduled
        val bedtimeScheduled = awaitBedtimeReminderScheduled()
        assertThat(bedtimeScheduled).isTrue()
        
        // 4. Trigger bedtime reminder
        advanceTimeBy(Duration.ofHours(3)) // Simulate time passing
        val bedtimeIntent = Intent(context, BedtimeReminderReceiver::class.java)
        context.sendBroadcast(bedtimeIntent)
        
        // 5. Verify morning feedback scheduled
        val morningScheduled = awaitMorningFeedbackScheduled()
        assertThat(morningScheduled).isTrue()
    }
}
```

---

## Edge Cases & Error Handling

### 1. User Disables Notifications
```kotlin
fun checkNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
    return true // Pre-Android 13, notifications always allowed
}

// In settings screen
if (!checkNotificationPermission()) {
    // Show dialog explaining why notifications are needed
    showPermissionRationaleDialog()
}
```

### 2. Device in Doze Mode
Using `setExactAndAllowWhileIdle()` ensures alarms fire even in Doze mode.

### 3. User Changes Wake Time After Bedtime Reminder Sent
```kotlin
fun onWakeTimeChanged(newWakeTime: LocalTime) {
    // Cancel existing bedtime reminder
    bedtimeReminderScheduler.cancelBedtimeReminder()
    
    // Recalculate and schedule new reminder
    val newBedtime = calculateOptimalBedtime(newWakeTime)
    bedtimeReminderScheduler.scheduleBedtimeReminder(newBedtime)
}
```

### 4. Multiple Notifications Stacking
```kotlin
// Use distinct notification IDs to prevent stacking
const val NOTIFICATION_ID_DAILY_CHECK_IN = 1001
const val NOTIFICATION_ID_BEDTIME_REMINDER = 1002
const val NOTIFICATION_ID_MORNING_FEEDBACK = 1003

// Auto-cancel older notifications of same type
notificationManager.cancel(NOTIFICATION_ID_DAILY_CHECK_IN)
notificationManager.notify(NOTIFICATION_ID_DAILY_CHECK_IN, newNotification)
```

---

*This document defines the complete notification strategy ensuring timely, non-intrusive sleep assistance throughout the user's day.*
