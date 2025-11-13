package uvg.saz.antony.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun AssetDetailScreen(
    assetId: String,
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(assetId) {
        viewModel.loadAssetDetail(assetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Asset") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error al cargar detalles",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.selectedAsset != null -> {
                AssetDetailContent(
                    asset = uiState.selectedAsset!!,
                    isFromCache = uiState.isFromCache,
                    lastUpdateTime = uiState.lastUpdateTime,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun AssetDetailContent(
    asset: CryptoAsset,
    isFromCache: Boolean,
    lastUpdateTime: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = asset.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatAssetPrice(asset.priceUsd),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                val changePercent = asset.changePercent24Hr.toDoubleOrNull() ?: 0.0
                val isPositive = changePercent >= 0

                Text(
                    text = "${if (isPositive) "▲" else "▼"} ${String.format("%.2f", changePercent)}% (24h)",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Información Detallada",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailRow("Supply", formatLargeNumber(asset.supply))
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                DetailRow(
                    "Máximo Supply",
                    asset.maxSupply?.let { formatLargeNumber(it) } ?: "N/A"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                DetailRow("Market Cap USD", formatAssetPrice(asset.marketCapUsd))
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                DetailRow("Volumen 24h USD", formatAssetPrice(asset.volumeUsd24Hr))
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                DetailRow(
                    "VWAP 24h",
                    asset.vwap24Hr?.let { formatAssetPrice(it) } ?: "N/A"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (isFromCache) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFB3E5FC)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💾",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (lastUpdateTime != null) {
                                "Viendo data del $lastUpdateTime"
                            } else {
                                "Viendo datos guardados"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (lastUpdateTime != null) {
                            Text(
                                text = "Guardado: $lastUpdateTime",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFC8E6C9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✅",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Viendo data más reciente",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}


private fun formatAssetPrice(priceString: String): String {
    val price = priceString.toDoubleOrNull() ?: return "$0.00"
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(price)
}

private fun formatLargeNumber(numberString: String): String {
    val number = numberString.toDoubleOrNull() ?: return "0"
    return when {
        number >= 1_000_000_000_000 -> String.format("%.2fT", number / 1_000_000_000_000)
        number >= 1_000_000_000 -> String.format("%.2fB", number / 1_000_000_000)
        number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000)
        number >= 1_000 -> String.format("%.2fK", number / 1_000)
        else -> String.format("%.2f", number)
    }
}