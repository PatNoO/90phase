package com.example.a90phase.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.a90phase.domain.sensors.AccelerometerDataSource
import com.example.a90phase.domain.sensors.AccelerometerReading
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AccelerometerDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AccelerometerDataSource {

    override fun observeAcceleration(): Flow<AccelerometerReading> =
        callbackFlow {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accelerometer == null) {
                close()
                return@callbackFlow
            }

            val listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        trySend(
                            AccelerometerReading(
                                x = event.values[0],
                                y = event.values[1],
                                z = event.values[2],
                                timestampMs = System.currentTimeMillis(),
                            ),
                        )
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor,
                        accuracy: Int,
                    ) = Unit
                }

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

            awaitClose {
                sensorManager.unregisterListener(listener)
            }
        }
}
