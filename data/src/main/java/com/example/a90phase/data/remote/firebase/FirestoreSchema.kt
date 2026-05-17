package com.example.a90phase.data.remote.firebase

/**
 * Firestore document structure:
 *
 * users/{userId}/profile              — FirestoreUserProfileDocument
 *   userId:                  String
 *   optimalCycleMinutes:     Long
 *   sleepLatencyMinutes:     Long
 *   preferredCycleCount:     Long
 *   reminderTime:            String   ("HH:mm")
 *   notificationsEnabled:    Boolean
 *   smartWakeWindowEnabled:  Boolean
 *   discoveryPhase:          Map?     (see DiscoveryPhase fields below)
 *   updatedAt:               Timestamp
 *
 * discoveryPhase (nested map):
 *   startDate:               String   (ISO date "yyyy-MM-dd")
 *   targetCycleMinutes:      Long
 *   currentWeek:             Long
 *   isActive:                Boolean
 *   completedAt:             String?  (ISO date or null)
 *
 * users/{userId}/sleep_logs/{logId}  — FirestoreSleepLogDocument
 *   id:                      String   (matches Room primary key)
 *   date:                    String   (ISO date "yyyy-MM-dd")
 *   bedtime:                 Timestamp?
 *   wakeTime:                Timestamp
 *   qualityRating:           Long?    (1–5, null if not yet rated)
 *   cycleCount:              Long
 *   cycleDurationUsed:       Long     (minutes)
 *   sleepLatencyUsed:        Long     (minutes)
 *   notes:                   String?
 *   createdAt:               Timestamp
 *   updatedAt:               Timestamp
 */
object FirestoreSchema {

    const val USERS = "users"

    object Profile {
        const val DOCUMENT = "profile"
        const val USER_ID = "userId"
        const val OPTIMAL_CYCLE_MINUTES = "optimalCycleMinutes"
        const val SLEEP_LATENCY_MINUTES = "sleepLatencyMinutes"
        const val PREFERRED_CYCLE_COUNT = "preferredCycleCount"
        const val REMINDER_TIME = "reminderTime"
        const val NOTIFICATIONS_ENABLED = "notificationsEnabled"
        const val SMART_WAKE_WINDOW_ENABLED = "smartWakeWindowEnabled"
        const val DISCOVERY_PHASE = "discoveryPhase"
        const val UPDATED_AT = "updatedAt"
    }

    object SleepLogs {
        const val COLLECTION = "sleep_logs"
        const val ID = "id"
        const val DATE = "date"
        const val BEDTIME = "bedtime"
        const val WAKE_TIME = "wakeTime"
        const val QUALITY_RATING = "qualityRating"
        const val CYCLE_COUNT = "cycleCount"
        const val CYCLE_DURATION_USED = "cycleDurationUsed"
        const val SLEEP_LATENCY_USED = "sleepLatencyUsed"
        const val NOTES = "notes"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }
}
