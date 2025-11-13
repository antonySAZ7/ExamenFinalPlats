package uvg.saz.antony.ui.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uvg.saz.antony.data.models.CryptoAsset
import uvg.saz.antony.data.repository.CryptoRepository
import uvg.saz.antony.data.repository.DataResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CryptoUiState(
    val assets: List<CryptoAsset> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFromCache: Boolean = false,
    val lastUpdateTime: String? = null,
    val isOfflineMode: Boolean = false,
    val selectedAsset: CryptoAsset? = null
)

class CryptoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CryptoRepository(application)

    private val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    init {
        loadAssets()
        checkOfflineMode()
    }

    fun loadAssets(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = repository.getAssets(forceRefresh)) {
                is DataResult.Success -> {
                    val timestamp = result.data.timestamp?.let {
                        formatTimestamp(it)
                    }
                    _uiState.value = _uiState.value.copy(
                        assets = result.data.assets,
                        isLoading = false,
                        error = null,
                        isFromCache = result.data.isFromCache,
                        lastUpdateTime = timestamp
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is DataResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun loadAssetDetail(assetId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = repository.getAssetDetail(assetId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        selectedAsset = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is DataResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            val currentMode = _uiState.value.isOfflineMode
            repository.setOfflineMode(!currentMode)
            _uiState.value = _uiState.value.copy(isOfflineMode = !currentMode)


            loadAssets(forceRefresh = !currentMode)
        }
    }

    private fun checkOfflineMode() {
        viewModelScope.launch {
            val isOffline = repository.isOfflineModeEnabled()
            _uiState.value = _uiState.value.copy(isOfflineMode = isOffline)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSelectedAsset() {
        _uiState.value = _uiState.value.copy(selectedAsset = null)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}