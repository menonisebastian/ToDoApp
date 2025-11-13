package com.example.todoapp

// Puedes crear un nuevo archivo .kt para esta clase o añadirla a MainActivity.kt
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {
    private var lastShakeTime: Long = 0
    private var shakeCount: Int = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gForce = sqrt((x * x + y * y + z * z).toDouble()) / SensorManager.GRAVITY_EARTH

            if (gForce > SHAKE_THRESHOLD) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime < 500) {
                    return
                }

                if (now - lastShakeTime < 2000) {
                    shakeCount++
                } else {
                    shakeCount = 1
                }

                lastShakeTime = now

                if (shakeCount >= MIN_SHAKES) {
                    onShake()
                    shakeCount = 0
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario para este caso
    }

    companion object {
        private const val SHAKE_THRESHOLD = 2.7f // Umbral de fuerza G para detectar agitación
        private const val MIN_SHAKES = 2 // Número mínimo de agitaciones en un corto período
    }
}
