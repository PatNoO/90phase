package com.example.a90phase.data.sync

import java.time.Instant

object SleepLogConflictResolver {

    /**
     * Resolves a conflict between local Room and remote Firestore versions of a sleep log
     * using last-write-wins on updatedAt. Local wins on tie — offline data is never discarded.
     */
    fun resolve(
        localUpdatedAt: Instant,
        remoteUpdatedAt: Instant,
    ): ConflictResolution =
        if (localUpdatedAt >= remoteUpdatedAt) {
            ConflictResolution.LOCAL_WINS
        } else {
            ConflictResolution.REMOTE_WINS
        }
}
