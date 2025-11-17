package com.example.todoapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class LightSensorDetector(
    private val onThemeChange: (Boolean) -> Unit
) : SensorEventListener {

    private var lastValue: Float? = null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario para este caso
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]

            // Evitar cambios constantes con pequeñas fluctuaciones
            if (lastValue == null || kotlin.math.abs(lightValue - lastValue!!) > LIGHT_THRESHOLD) {
                lastValue = lightValue
                // Si la luz es baja, activa el modo oscuro, de lo contrario lo desactiva
                onThemeChange(lightValue < DARK_MODE_THRESHOLD)
            }
        }
    }

    companion object {
        private const val DARK_MODE_THRESHOLD = 20.0f
        private const val LIGHT_THRESHOLD = 5.0f
    }
}
