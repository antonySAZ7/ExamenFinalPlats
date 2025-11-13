package uvg.saz.antony.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uvg.saz.antony.data.models.CryptoAsset
import uvg.saz.antony.ui.viewmodel.CryptoViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsListScreen(
    viewModel: CryptoViewModel,
    onAssetClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criptomonedas") },
                actions = {

                    IconButton(onClick = { viewModel.toggleOfflineMode() }) {
                        Icon(
                            imageVector = if (uiState.isOfflineMode)
                                Icons.Default.Warning
                            else
                                Icons.Default.Info,
                            contentDescription = "Toggle Offline Mode",
                            tint = if (uiState.isOfflineMode)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón de refrescar
                    IconButton(
                        onClick = { viewModel.loadAssets(forceRefresh = true) },
                        enabled = !uiState.isOfflineMode
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de estado
            StatusIndicator(
                isFromCache = uiState.isFromCache,
                lastUpdateTime = uiState.lastUpdateTime,
                isOfflineMode = uiState.isOfflineMode
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error ?: "Error desconocido",
                        onRetry = { viewModel.loadAssets() }
                    )
                }

                uiState.assets.isEmpty() -> {
                    EmptyView()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.assets) { asset ->
                            CryptoAssetCard(
                                asset = asset,
                                onClick = { onAssetClick(asset.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(
    isFromCache: Boolean,
    lastUpdateTime: String?,
    isOfflineMode: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when {
            isOfflineMode -> Color(0xFFFFE082)
            isFromCache -> Color(0xFFB3E5FC)
            else -> Color(0xFFC8E6C9)
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = when {
                    isOfflineMode -> "🔴 Modo Offline"
                    isFromCache && lastUpdateTime != null -> "💾 Viendo datos del $lastUpdateTime"
                    else -> "✅ Viendo data más reciente"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            if (!isOfflineMode && lastUpdateTime != null) {
                Text(
                    text = "Última actualización: $lastUpdateTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CryptoAssetCard(
    asset: CryptoAsset,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = asset.symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCryptoPrice(asset.priceUsd),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val changePercent = asset.changePercent24Hr.toDoubleOrNull() ?: 0.0
                    val isPositive = changePercent >= 0

                    Text(
                        text = "${if (isPositive) "▲" else "▼"} ${String.format("%.2f", changePercent)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌ Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay datos disponibles",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


private fun formatCryptoPrice(priceString: String): String {
    val price = priceString.toDoubleOrNull() ?: return "$0.00"
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(price)
}