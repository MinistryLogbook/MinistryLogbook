package app.ministrylogbook.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.ministrylogbook.shared.ModalBottomSheetLayout
import app.ministrylogbook.shared.PopupLayout
import app.ministrylogbook.shared.rememberBottomSheetNavigator
import app.ministrylogbook.shared.rememberPopupNavigator
import app.ministrylogbook.ui.home.homeGraph
import app.ministrylogbook.ui.settings.settingsGraph
import app.ministrylogbook.ui.share.shareGraph
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = AppGraph.Home.route
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val popupNavigator = rememberPopupNavigator()
    val navController = rememberAnimatedNavController(popupNavigator, bottomSheetNavigator)

    CompositionLocalProvider(LocalAppNavController provides navController) {
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
                AnimatedNavHost(
                    modifier = modifier,
                    navController = navController,
                    startDestination = startDestination
                ) {
                    homeGraph()
                    settingsGraph()
                    shareGraph()
                }
            }
        }
    }
}

typealias AppNavHostController = NavHostController

val LocalAppNavController =
    compositionLocalOf<AppNavHostController> { error("LocalNavHostController error") }

const val SlideInTransitionMillis = 250
const val SlideOutTransitionMillis = 250
const val FadeInTransitionMillis = 150
const val FadeOutTransitionMillis = 150
