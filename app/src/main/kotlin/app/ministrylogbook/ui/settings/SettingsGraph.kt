package app.ministrylogbook.ui.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import app.ministrylogbook.lib.stayOut
import app.ministrylogbook.ui.AppGraph
import app.ministrylogbook.ui.FadeInTransitionMillis
import app.ministrylogbook.ui.FadeOutTransitionMillis
import app.ministrylogbook.ui.SlideInTransitionMillis
import app.ministrylogbook.ui.SlideOutTransitionMillis
import app.ministrylogbook.ui.settings.license.LicenseDetailPage
import app.ministrylogbook.ui.settings.license.LicensesPage
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation

sealed class SettingsGraph(private val rawRoute: String, val arguments: List<NamedNavArgument> = listOf()) {
    object Root : SettingsGraph("")

    object Name : SettingsGraph("name")

    object Goal : SettingsGraph("goal")

    object Licenses : SettingsGraph("licenses")

    object LicenseDetail : SettingsGraph(
        rawRoute = "licenses/{id}",
        arguments = listOf(
            navArgument("id") {}
        )
    ) {
        fun createDestination(id: String) = "${AppGraph.Settings.route}/licenses/$id"
    }

    val route
        get() = "${AppGraph.Settings.route}/$rawRoute"
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph() {
    navigation(
        route = AppGraph.Settings.route,
        startDestination = SettingsGraph.Root.route,
        enterTransition = {
            val toPrevious = initialState.destination.route?.startsWith(targetState.destination.route.orEmpty())
                ?: false

            if (toPrevious) {
                return@navigation EnterTransition.None
            }
            slideInHorizontally(tween(SlideInTransitionMillis)) { it / 6 } + fadeIn(
                tween(
                    FadeInTransitionMillis
                )
            )
        },
        exitTransition = {
            val toChild = targetState.destination.route?.startsWith(initialState.destination.route.orEmpty())
                ?: false

            if (toChild) {
                return@navigation stayOut(SlideOutTransitionMillis)
            }
            slideOutHorizontally(tween(SlideOutTransitionMillis)) { it / 6 } + fadeOut(
                tween(
                    FadeOutTransitionMillis
                )
            )
        }
    ) {
        composable(SettingsGraph.Root.route) {
            SettingsPage()
        }

        composable(SettingsGraph.Name.route) {
            NamePage()
        }

        composable(SettingsGraph.Goal.route) {
            GoalPage()
        }

        composable(SettingsGraph.Licenses.route) {
            LicensesPage()
        }

        composable(
            SettingsGraph.LicenseDetail.route,
            arguments = SettingsGraph.LicenseDetail.arguments
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            LicenseDetailPage(id)
        }
    }
}
