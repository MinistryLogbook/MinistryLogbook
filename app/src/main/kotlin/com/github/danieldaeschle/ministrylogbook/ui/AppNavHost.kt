package com.github.danieldaeschle.ministrylogbook.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.danieldaeschle.ministrylogbook.lib.ModalBottomSheetLayout
import com.github.danieldaeschle.ministrylogbook.lib.PopupLayout
import com.github.danieldaeschle.ministrylogbook.lib.rememberBottomSheetNavigator
import com.github.danieldaeschle.ministrylogbook.lib.rememberPopupNavigator
import com.github.danieldaeschle.ministrylogbook.ui.home.homeGraph
import com.github.danieldaeschle.ministrylogbook.ui.settings.settingsGraph
import com.github.danieldaeschle.ministrylogbook.ui.share.shareGraph
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

sealed class AppGraph(val route: String) {
    object Home : AppGraph("home")

    object Settings : AppGraph("settings")

    object Share : AppGraph("share")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = AppGraph.Home.route,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val popupNavigator = rememberPopupNavigator()
    val navController = rememberAnimatedNavController(bottomSheetNavigator, popupNavigator)

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
                AnimatedNavHost(
                    modifier = modifier,
                    navController = navController,
                    startDestination = startDestination,
                ) {
                    homeGraph()
                    settingsGraph()
                    shareGraph()
                }
            }
        }
    }
}

val LocalAppNavController =
    compositionLocalOf<NavHostController> { error("NavHostController error") }

val SlideInTransitionMillis = 250
val SlideOutTransitionMillis = 250
val FadeInTransitionMillis = 150
val FadeOutTransitionMillis = 150
