package app.ministrylogbook.ui.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import app.ministrylogbook.shared.layouts.stayOut
import app.ministrylogbook.ui.AppGraph
import app.ministrylogbook.ui.FADE_IN_TRANSITION_MILLIS
import app.ministrylogbook.ui.FADE_OUT_TRANSITION_MILLIS
import app.ministrylogbook.ui.SLIDE_IN_TRANSITION_MILLIS
import app.ministrylogbook.ui.SLIDE_OUT_TRANSITION_MILLIS
import app.ministrylogbook.ui.home.HomeGraph
import app.ministrylogbook.ui.settings.license.LicenseDetailPage
import app.ministrylogbook.ui.settings.license.LicensesPage

sealed class SettingsGraph(private val rawRoute: String, val arguments: List<NamedNavArgument> = listOf()) {
    data object Root : SettingsGraph("")

    data object Name : SettingsGraph("name")

    data object Goal : SettingsGraph("goal")

    data object Licenses : SettingsGraph("licenses")

    data object LicenseDetail : SettingsGraph(
        rawRoute = "licenses/{id}",
        arguments = listOf(navArgument("id") {})
    ) {
        fun createDestination(id: String) = "${AppGraph.Settings}/licenses/$id"
    }

    val route
        get() = "${AppGraph.Settings}/$rawRoute"
}

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
            slideInHorizontally(tween(SLIDE_IN_TRANSITION_MILLIS)) { it / 6 } + fadeIn(
                tween(FADE_IN_TRANSITION_MILLIS)
            )
        },
        exitTransition = {
            val toChild = targetState.destination.route?.startsWith(initialState.destination.route.orEmpty())
                ?: false

            if (toChild) {
                return@navigation stayOut(SLIDE_OUT_TRANSITION_MILLIS)
            }
            slideOutHorizontally(tween(SLIDE_OUT_TRANSITION_MILLIS)) { it / 6 } + fadeOut(
                tween(FADE_OUT_TRANSITION_MILLIS)
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

fun NavHostController.navigateToSettings() = navigate(SettingsGraph.Root.route) {
    popUpTo(HomeGraph.Root.route)
}

fun NavHostController.navigateToSettingsName() = navigate(SettingsGraph.Name.route) {
    popUpTo(SettingsGraph.Root.route)
}

fun NavHostController.navigateToSettingsGoal() = navigate(SettingsGraph.Goal.route) {
    popUpTo(SettingsGraph.Root.route)
}

fun NavHostController.navigateToOpenSourceLicenses() = navigate(SettingsGraph.Licenses.route) {
    popUpTo(SettingsGraph.Root.route)
}

fun NavHostController.navigateToLicenseDetail(id: String) =
    navigate(SettingsGraph.LicenseDetail.createDestination(id)) {
        popUpTo(SettingsGraph.Licenses.route)
    }
