package uvg.saz.antony.data.models


import com.google.gson.annotations.SerializedName


data class AssetsResponse(
    val data: List<CryptoAsset>,
    val timestamp: Long
)

// Modelo principal de criptomoneda
data class CryptoAsset(
    val id: String,
    val rank: String,
    val symbol: String,
    val name: String,
    val supply: String,
    @SerializedName("maxSupply")
    val maxSupply: String?,
    @SerializedName("marketCapUsd")
    val marketCapUsd: String,
    @SerializedName("volumeUsd24Hr")
    val volumeUsd24Hr: String,
    @SerializedName("priceUsd")
    val priceUsd: String,
    @SerializedName("changePercent24Hr")
    val changePercent24Hr: String,
    @SerializedName("vwap24Hr")
    val vwap24Hr: String?
)


data class LocalCryptoData(
    val assets: List<CryptoAsset>,
    val timestamp: Long,
    val isOfflineMode: Boolean = false
)


data class AssetDetailResponse(
    val data: CryptoAsset,
    val timestamp: Long
)