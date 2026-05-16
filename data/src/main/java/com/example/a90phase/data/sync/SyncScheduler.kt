package com.example.a90phase.data.sync

interface SyncScheduler {
    fun enqueueSleepLogSync()
    fun scheduleSmartWakeMonitor()
    fun cancelSmartWakeMonitor()
}
