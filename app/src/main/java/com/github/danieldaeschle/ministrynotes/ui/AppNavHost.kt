package com.github.danieldaeschle.ministrynotes.ui

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.github.danieldaeschle.ministrynotes.lib.ModalBottomSheetLayout
import com.github.danieldaeschle.ministrynotes.lib.PopupLayout
import com.github.danieldaeschle.ministrynotes.lib.rememberBottomSheetNavigator
import com.github.danieldaeschle.ministrynotes.lib.rememberPopupNavigator
import com.github.danieldaeschle.ministrynotes.ui.home.homeGraph
import com.github.danieldaeschle.ministrynotes.ui.settings.settingsGraph

sealed class AppGraph(val route: String) {
    object Home : AppGraph("home")
    object Settings : AppGraph("settings")
}

val LocalAppNavController =
    compositionLocalOf<NavHostController> { error("NavHostController error") }

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier, startDestination: String = AppGraph.Home.route
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val popupNavigator = rememberPopupNavigator()
    val navController = rememberNavController(bottomSheetNavigator, popupNavigator)

    CompositionLocalProvider(LocalAppNavController provides navController) {
        ModalBottomSheetLayout(
            bottomSheetNavigator = bottomSheetNavigator,
            sheetShape = MaterialTheme.shapes.large.copy(
                bottomStart = CornerSize(0),
                bottomEnd = CornerSize(0)
            ),
            sheetElevation = 2.dp,
            scrimColor = MaterialTheme.colorScheme.surface.copy(0.5f),
        ) {
            PopupLayout(popupNavigator = popupNavigator) {
                NavHost(
                    modifier = modifier,
                    navController = navController,
                    startDestination = startDestination
                ) {
                    homeGraph(navController)
                    settingsGraph(navController)
                }
            }
        }
    }
}

