package uvg.saz.antony.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uvg.saz.antony.ui.screens.AssetDetailScreen
import uvg.saz.antony.ui.screens.AssetsListScreen
import uvg.saz.antony.ui.viewmodel.CryptoViewModel

sealed class Screen(val route: String) {
    object AssetsList : Screen("assets_list")
    object AssetDetail : Screen("asset_detail/{assetId}") {
        fun createRoute(assetId: String) = "asset_detail/$assetId"
    }
}

@Composable
fun CryptoNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: CryptoViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AssetsList.route
    ) {
        composable(Screen.AssetsList.route) {
            AssetsListScreen(
                viewModel = viewModel,
                onAssetClick = { assetId ->
                    navController.navigate(Screen.AssetDetail.createRoute(assetId))
                }
            )
        }

        composable(
            route = Screen.AssetDetail.route,
            arguments = listOf(
                navArgument("assetId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("assetId") ?: return@composable
            AssetDetailScreen(
                assetId = assetId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}