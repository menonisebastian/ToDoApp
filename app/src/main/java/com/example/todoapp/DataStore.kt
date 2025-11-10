// En un nuevo archivo: DataStore.kt
package com.example.todoapp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Instancia única de DataStore a nivel de aplicación
val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context)
{

    // Clave para guardar el estado del modo oscuro (true = activado)
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")

    // Flow para leer el valor del modo oscuro.
    // Emite un nuevo valor cada vez que cambia. Por defecto es 'false'.
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    // Función suspendida para escribir el valor del modo oscuro.
    suspend fun setDarkMode(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[DARK_MODE_KEY] = isEnabled
        }
    }
}
