// En un nuevo archivo: DataStore.kt
package com.example.todoapp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Instancia única de DataStore a nivel de aplicación
val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context)
{
    private val TASK_TEXT_COLOR_KEY = stringPreferencesKey("task_text_color")

    // Expón el color como un Flow<String>. El valor por defecto puede ser "Default" o un color inicial.
    val taskTextColor: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TASK_TEXT_COLOR_KEY] ?: "Default" // Usamos "Default" como valor inicial
        }

    // Clave para guardar el estado del modo oscuro (true = activado)
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    private val AUTO_DARK_MODE_KEY = booleanPreferencesKey("auto_dark_mode_enabled")


    // Flow para leer el valor del modo oscuro.
    // Emite un nuevo valor cada vez que cambia. Por defecto es 'false'.
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val isAutoDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_DARK_MODE_KEY] ?: true
        }

    // Función suspendida para escribir el valor del modo oscuro.
    suspend fun setDarkMode(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[DARK_MODE_KEY] = isEnabled
        }
    }

    suspend fun setAutoDarkMode(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[AUTO_DARK_MODE_KEY] = isEnabled
        }
    }

    // Función para guardar el nuevo color
    suspend fun setTaskTextColor(colorName: String) {
        context.dataStore.edit { preferences ->
            preferences[TASK_TEXT_COLOR_KEY] = colorName
        }
    }
}
