package com.example.myapplication.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.domain.model.Preferencias
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Una sola instancia de DataStore por proceso.
 *
 * Se declara como extension de Context a nivel de archivo porque
 * DataStore lanza excepcion si se crean dos instancias apuntando
 * al mismo archivo. Este patron lo garantiza.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferencias_robot"
)

/**
 * Persistencia de ajustes de usuario.
 *
 * DataStore reemplaza a SharedPreferences: es asincrono, no
 * bloquea el hilo principal, y expone los datos como Flow, asi
 * que la UI se actualiza sola cuando algo cambia.
 */
class PreferenciasRepository(private val context: Context) {

    private object Claves {
        val IP            = stringPreferencesKey("ip_robot")
        val VELOCIDAD     = floatPreferencesKey("velocidad_maxima")
        val MODO_OSCURO   = booleanPreferencesKey("modo_oscuro")
        val FAHRENHEIT    = booleanPreferencesKey("usar_fahrenheit")
        val UMBRAL_GAS    = intPreferencesKey("umbral_gas")
        val MODO_JOYSTICK = booleanPreferencesKey("modo_joystick")
        val HAPTICA       = booleanPreferencesKey("haptica_activa")
    }

    val preferencias: Flow<Preferencias> = context.dataStore.data
        .catch { e ->
            // Si el archivo se corrompe, arrancamos con valores por
            // defecto en lugar de tumbar la app.
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { p ->
            Preferencias(
                ipRobot         = p[Claves.IP] ?: Preferencias.IP_POR_DEFECTO,
                velocidadMaxima = p[Claves.VELOCIDAD] ?: 0.7f,
                modoOscuro      = p[Claves.MODO_OSCURO] ?: true,
                usarFahrenheit  = p[Claves.FAHRENHEIT] ?: false,
                umbralGas       = p[Claves.UMBRAL_GAS] ?: 60,
                modoJoystick    = p[Claves.MODO_JOYSTICK] ?: true,
                hapticaActiva   = p[Claves.HAPTICA] ?: true
            )
        }

    suspend fun guardarIp(ip: String) {
        context.dataStore.edit { it[Claves.IP] = ip.trim() }
    }

    suspend fun guardarVelocidadMaxima(valor: Float) {
        context.dataStore.edit { it[Claves.VELOCIDAD] = valor.coerceIn(0.1f, 1f) }
    }

    suspend fun guardarModoOscuro(activo: Boolean) {
        context.dataStore.edit { it[Claves.MODO_OSCURO] = activo }
    }

    suspend fun guardarUsarFahrenheit(activo: Boolean) {
        context.dataStore.edit { it[Claves.FAHRENHEIT] = activo }
    }

    suspend fun guardarUmbralGas(valor: Int) {
        context.dataStore.edit { it[Claves.UMBRAL_GAS] = valor.coerceIn(0, 100) }
    }

    suspend fun guardarModoJoystick(activo: Boolean) {
        context.dataStore.edit { it[Claves.MODO_JOYSTICK] = activo }
    }

    suspend fun guardarHaptica(activo: Boolean) {
        context.dataStore.edit { it[Claves.HAPTICA] = activo }
    }
}
