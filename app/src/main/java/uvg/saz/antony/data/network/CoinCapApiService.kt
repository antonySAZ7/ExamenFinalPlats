package uvg.saz.antony.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import uvg.saz.antony.data.models.AssetDetailResponse
import uvg.saz.antony.data.models.AssetsResponse

class CoinCapApiService {

    companion object {
        private const val BASE_URL = "https://rest.coincap.io/v3/"
        private const val API_KEY = "6f8c2f757cc81e9950a05aeed8292abff853114ebc731977f3f5a580b1e9371a"
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }

        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
        }

        defaultRequest {
            url(BASE_URL)
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun getAssets(): AssetsResponse {
        return client.get("assets").body()
    }

    suspend fun getAssetDetail(id: String): AssetDetailResponse {
        return client.get("assets/$id").body()
    }

    fun close() {
        client.close()
    }
}