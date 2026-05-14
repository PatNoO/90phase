package com.example.a90phase.data.sensors

import com.example.a90phase.domain.entities.SmartWakeWindow
import com.example.a90phase.domain.sensors.AccelerometerDataSource
import com.example.a90phase.domain.usecases.ProcessMovementEventUseCase
import com.example.a90phase.domain.usecases.ScheduleSmartWakeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class SmartWakeScheduler @Inject constructor(
    private val accelerometerDataSource: AccelerometerDataSource,
    private val scheduleUseCase: ScheduleSmartWakeUseCase,
    private val processMovementUseCase: ProcessMovementEventUseCase,
) {
    private var monitorJob: Job? = null

    fun start(
        window: SmartWakeWindow,
        scope: CoroutineScope,
        onAlarmTriggered: () -> Unit,
    ) {
        if (!window.isEnabled) return

        val schedule = scheduleUseCase(window)

        monitorJob = scope.launch {
            accelerometerDataSource
                .observeAcceleration()
                .takeWhile { Instant.now().isBefore(schedule.windowEnd) }
                .collect { reading ->
                    if (processMovementUseCase.process(reading)) {
                        stop()
                        onAlarmTriggered()
                    }
                }
        }
    }

    fun stop() {
        monitorJob?.cancel()
        monitorJob = null
    }
}
