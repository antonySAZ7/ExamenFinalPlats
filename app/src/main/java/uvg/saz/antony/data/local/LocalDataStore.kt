package uvg.saz.antony.data.local


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uvg.saz.antony.data.models.CryptoAsset

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "crypto_preferences")

class LocalDataStore(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val CRYPTO_DATA_KEY = stringPreferencesKey("crypto_data")
        private val TIMESTAMP_KEY = longPreferencesKey("timestamp")
        private val OFFLINE_MODE_KEY = stringPreferencesKey("offline_mode_enabled")
    }


    suspend fun saveCryptoAssets(assets: List<CryptoAsset>) {
        context.dataStore.edit { preferences ->
            val json = gson.toJson(assets)
            preferences[CRYPTO_DATA_KEY] = json
            preferences[TIMESTAMP_KEY] = System.currentTimeMillis()
        }
    }


    fun getCryptoAssets(): Flow<List<CryptoAsset>?> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[CRYPTO_DATA_KEY]
            if (json != null) {
                val type = object : TypeToken<List<CryptoAsset>>() {}.type
                gson.fromJson(json, type)
            } else {
                null
            }
        }
    }


    fun getTimestamp(): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[TIMESTAMP_KEY]
        }
    }

    // Guardar estado de modo offline
    suspend fun setOfflineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OFFLINE_MODE_KEY] = enabled.toString()
        }
    }


    fun isOfflineModeEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[OFFLINE_MODE_KEY]?.toBoolean() ?: false
        }
    }


    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}