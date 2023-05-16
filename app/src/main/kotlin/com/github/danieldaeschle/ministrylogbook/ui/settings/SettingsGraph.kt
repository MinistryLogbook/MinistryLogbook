package com.github.danieldaeschle.ministrylogbook.ui.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import com.github.danieldaeschle.ministrylogbook.lib.stayOut
import com.github.danieldaeschle.ministrylogbook.ui.AppGraph
import com.github.danieldaeschle.ministrylogbook.ui.FadeInTransitionMillis
import com.github.danieldaeschle.ministrylogbook.ui.FadeOutTransitionMillis
import com.github.danieldaeschle.ministrylogbook.ui.SlideInTransitionMillis
import com.github.danieldaeschle.ministrylogbook.ui.SlideOutTransitionMillis
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation

sealed class SettingsGraph(private val rawRoute: String) {
    object Root : SettingsGraph("/")

    object Name : SettingsGraph("name")

    object Goal : SettingsGraph("goal")

    val route
        get() = "${AppGraph.Settings.route}/${rawRoute}"
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph() {
    navigation(
        route = AppGraph.Settings.route,
        startDestination = SettingsGraph.Root.route,
        enterTransition = {
            val fromAnySettingsPage =
                initialState.destination.route?.startsWith(AppGraph.Settings.route) ?: false
            val toRootSettingsPage = targetState.destination.route == SettingsGraph.Root.route
            if (fromAnySettingsPage && toRootSettingsPage) {
                return@navigation EnterTransition.None
            }
            slideInHorizontally(tween(SlideInTransitionMillis)) { it / 6 } + fadeIn(
                tween(
                    FadeInTransitionMillis
                )
            )
        },
        exitTransition = {
            val fromRootSettingsPage = initialState.destination.route == SettingsGraph.Root.route
            val toAnySettingsPage =
                targetState.destination.route?.startsWith(AppGraph.Settings.route) ?: false
            if (fromRootSettingsPage && toAnySettingsPage) {
                return@navigation stayOut(SlideOutTransitionMillis)
            }
            slideOutHorizontally(tween(SlideOutTransitionMillis)) { it / 6 } + fadeOut(
                tween(
                    FadeOutTransitionMillis
                )
            )
        },
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
    }
}
