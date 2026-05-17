package com.example.a90phase.data.sync

interface SyncScheduler {
    fun enqueueSleepLogSync()
    fun enqueueUserProfileSync()
    fun schedulePeriodicFirebaseSync()
    fun triggerManualFirebaseSync()
    fun scheduleSmartWakeMonitor()
    fun cancelSmartWakeMonitor()
}
