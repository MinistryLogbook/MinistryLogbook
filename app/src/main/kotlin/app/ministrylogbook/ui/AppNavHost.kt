package app.ministrylogbook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import app.ministrylogbook.shared.layouts.LocalBottomSheetStateLock
import app.ministrylogbook.shared.layouts.ModalBottomSheetLayout
import app.ministrylogbook.shared.layouts.PopupLayout
import app.ministrylogbook.shared.layouts.rememberBottomSheetNavigator
import app.ministrylogbook.shared.layouts.rememberBottomSheetStateLock
import app.ministrylogbook.shared.layouts.rememberPopupNavigator
import app.ministrylogbook.ui.home.backup.backupGraph
import app.ministrylogbook.ui.home.homeGraph
import app.ministrylogbook.ui.intro.introGraph
import app.ministrylogbook.ui.settings.settingsGraph
import app.ministrylogbook.ui.share.shareGraph

@Composable
fun AppNavHost(startDestination: String = AppGraph.Home.route) {
    val bottomSheetStateLock = rememberBottomSheetStateLock()
    val bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetStateLock)
    val popupNavigator = rememberPopupNavigator()
    val navController = rememberNavController(popupNavigator, bottomSheetNavigator)

    CompositionLocalProvider(
        LocalAppNavController provides navController,
        LocalBottomSheetStateLock provides bottomSheetStateLock
    ) {
        Surface(Modifier.background(MaterialTheme.colorScheme.surface)) {
            ModalBottomSheetLayout(
                sheetContent = bottomSheetNavigator.sheetContent,
                sheetState = bottomSheetNavigator.sheetState,
                sheetShape = MaterialTheme.shapes.large.copy(
                    bottomStart = CornerSize(0),
                    bottomEnd = CornerSize(0)
                ),
                sheetElevation = 2.dp,
                scrimColor = MaterialTheme.colorScheme.surface.copy(0.5f)
            ) {
                PopupLayout(popupNavigator = popupNavigator, popupState = popupNavigator.popupState) {
                    NavHost(navController = navController, startDestination = startDestination) {
                        shareGraph()
                        backupGraph()
                        homeGraph()
                        settingsGraph()
                        introGraph()
                    }
                }
            }
        }
    }
}

typealias AppNavHostController = NavHostController

val LocalAppNavController =
    compositionLocalOf<AppNavHostController> { error("LocalAppNavController error") }

const val SlideInTransitionMillis = 250
const val SlideOutTransitionMillis = 250
const val FadeInTransitionMillis = 150
const val FadeOutTransitionMillis = 150
