package app.ministrylogbook.ui.share

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.ministrylogbook.ui.AppGraph
import app.ministrylogbook.ui.FADE_IN_TRANSITION_MILLIS
import app.ministrylogbook.ui.FADE_OUT_TRANSITION_MILLIS
import app.ministrylogbook.ui.SLIDE_IN_TRANSITION_MILLIS
import app.ministrylogbook.ui.SLIDE_OUT_TRANSITION_MILLIS
import app.ministrylogbook.ui.share.viewmodel.ShareViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed class ShareGraph(private val rawRoute: String, val arguments: List<NamedNavArgument> = listOf()) {
    data object Root : ShareGraph(
        "?year={year}&monthNumber={monthNumber}",
        arguments = listOf(
            navArgument("year") {
                nullable = true
            },
            navArgument("monthNumber") {
                nullable = true
            }
        )
    ) {
        fun createDestination(year: Int, monthNumber: Int): String =
            "${AppGraph.Share}/?year=$year&monthNumber=$monthNumber"
    }

    val route
        get() = "${AppGraph.Share}/$rawRoute"
}

fun NavGraphBuilder.shareGraph() {
    navigation(
        route = AppGraph.Share.route,
        startDestination = ShareGraph.Root.route,
        enterTransition = {
            slideInHorizontally(tween(SLIDE_IN_TRANSITION_MILLIS)) { it / 6 } + fadeIn(
                tween(FADE_IN_TRANSITION_MILLIS)
            )
        },
        exitTransition = {
            slideOutHorizontally(tween(SLIDE_OUT_TRANSITION_MILLIS)) { it / 6 } + fadeOut(
                tween(FADE_OUT_TRANSITION_MILLIS)
            )
        }
    ) {
        composable(
            ShareGraph.Root.route,
            ShareGraph.Root.arguments,
            deepLinks = listOf(
                navDeepLink { uriPattern = "ministrylogbook://share/?year={year}&monthNumber={monthNumber}" }
            )
        ) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = it.arguments?.getString("year")?.toInt() ?: currentDate.year
            val monthNumber =
                it.arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber
            val isCurrentMonth = year == currentDate.year && monthNumber == currentDate.monthNumber
            val month = if (isCurrentMonth) {
                currentDate
            } else {
                LocalDate(year, monthNumber, 1) + DatePeriod(months = 1) - DatePeriod(days = 1)
            }
            val shareViewModel = koinViewModel<ShareViewModel>(parameters = {
                parametersOf(month)
            })
            SharePage(shareViewModel)
        }
    }
}

fun NavController.navigateToShare(year: Int, monthNumber: Int) =
    navigate(ShareGraph.Root.createDestination(year, monthNumber))
