package uvg.saz.antony.data.repository

import android.content.Context
import kotlinx.coroutines.flow.first
import uvg.saz.antony.data.local.LocalDataStore
import uvg.saz.antony.data.models.CryptoAsset
import uvg.saz.antony.data.network.CoinCapApiService

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val message: String) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}

data class CryptoDataState(
    val assets: List<CryptoAsset>,
    val isFromCache: Boolean,
    val timestamp: Long?
)

class CryptoRepository(context: Context) {

    private val apiService = CoinCapApiService()
    private val localDataStore = LocalDataStore(context)


    suspend fun getAssets(forceRefresh: Boolean = false): DataResult<CryptoDataState> {
        return try {

            val isOfflineMode = localDataStore.isOfflineModeEnabled().first()

            if (!forceRefresh && isOfflineMode) {

                val cachedAssets = localDataStore.getCryptoAssets().first()
                val timestamp = localDataStore.getTimestamp().first()

                if (cachedAssets != null && cachedAssets.isNotEmpty()) {
                    return DataResult.Success(
                        CryptoDataState(
                            assets = cachedAssets,
                            isFromCache = true,
                            timestamp = timestamp
                        )
                    )
                } else {
                    return DataResult.Error("No hay datos guardados. Active el modo online para obtener datos.")
                }
            }


            try {
                val response = apiService.getAssets()
                // Guardar en caché
                localDataStore.saveCryptoAssets(response.data)
                DataResult.Success(
                    CryptoDataState(
                        assets = response.data,
                        isFromCache = false,
                        timestamp = response.timestamp
                    )
                )
            } catch (e: Exception) {
                // Si falla la API, intentar con datos locales
                val cachedAssets = localDataStore.getCryptoAssets().first()
                val timestamp = localDataStore.getTimestamp().first()

                if (cachedAssets != null && cachedAssets.isNotEmpty()) {
                    DataResult.Success(
                        CryptoDataState(
                            assets = cachedAssets,
                            isFromCache = true,
                            timestamp = timestamp
                        )
                    )
                } else {
                    DataResult.Error("Error de conexión: ${e.message}")
                }
            }
        } catch (e: Exception) {
            DataResult.Error("Error: ${e.message}")
        }
    }


    suspend fun getAssetDetail(id: String): DataResult<CryptoAsset> {
        return try {
            val response = apiService.getAssetDetail(id)
            DataResult.Success(response.data)
        } catch (e: Exception) {
            DataResult.Error("Error al obtener detalles: ${e.message}")
        }
    }


    suspend fun setOfflineMode(enabled: Boolean) {
        localDataStore.setOfflineMode(enabled)
    }


    suspend fun isOfflineModeEnabled(): Boolean {
        return localDataStore.isOfflineModeEnabled().first()
    }


    suspend fun getLastUpdateTimestamp(): Long? {
        return localDataStore.getTimestamp().first()
    }


    fun closeClient() {
        apiService.close()
    }
}