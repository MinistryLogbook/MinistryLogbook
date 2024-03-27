package app.ministrylogbook.ui.home

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.ministrylogbook.shared.layouts.bottomSheet
import app.ministrylogbook.shared.layouts.popup
import app.ministrylogbook.shared.layouts.stayOut
import app.ministrylogbook.shared.utilities.activity
import app.ministrylogbook.ui.AppGraph
import app.ministrylogbook.ui.SlideOutTransitionMillis
import app.ministrylogbook.ui.home.entrydetails.EntryDetailsBottomSheetContent
import app.ministrylogbook.ui.home.viewmodel.EntryDetailsViewModel
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

sealed class HomeGraph(private val rawRoute: String, val arguments: List<NamedNavArgument> = listOf()) {
    object Root : HomeGraph(
        rawRoute = "?year={year}&monthNumber={monthNumber}",
        arguments = listOf(
            navArgument("year") {
                nullable = true
            },
            navArgument("monthNumber") {
                nullable = true
            }
        )
    ) {
        fun createDestination(year: Int, monthNumber: Int) = "${AppGraph.Home}/?year=$year&monthNumber=$monthNumber"
    }

    object FromDeepLink : HomeGraph("?fromDeepLink=true")

    object Menu : HomeGraph(rawRoute = "menu")

    object EntryDetails : HomeGraph(
        rawRoute = "{year}/{monthNumber}/entry-details/{id}",
        arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
    ) {
        fun createDestination(month: LocalDate, id: Int? = null): String {
            if (id == null) {
                return "${AppGraph.Home}/${month.year}/${month.monthNumber}/entry-details/new"
            }
            return "${AppGraph.Home}/${month.year}/${month.monthNumber}/entry-details/$id"
        }
    }

    override fun toString() = route

    val route
        get() = "${AppGraph.Home}/$rawRoute"
}

fun NavGraphBuilder.homeGraph() {
    navigation(
        route = AppGraph.Home.route,
        startDestination = HomeGraph.Root.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { stayOut(SlideOutTransitionMillis) }
    ) {
        composable(HomeGraph.Root.route) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val yearArgument = it.arguments?.getString("year")
            val year = yearArgument?.toInt() ?: currentDate.year
            val monthNumberArgument = it.arguments?.getString("monthNumber")
            val monthNumber = monthNumberArgument?.toInt() ?: currentDate.monthNumber
            val month = LocalDate(year, monthNumber, 1)

            val viewModel = getViewModel<HomeViewModel>(parameters = { parametersOf(month) })
            val state by viewModel.state.collectAsStateWithLifecycle()

            HomePage(state, viewModel::dispatch)
        }

        composable(
            HomeGraph.FromDeepLink.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "content://.*"
                    action = Intent.ACTION_VIEW
                },
                navDeepLink {
                    uriPattern = "file://.*"
                    action = Intent.ACTION_VIEW
                }
            )
        ) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val yearArgument = it.arguments?.getString("year")
            val year = yearArgument?.toInt() ?: currentDate.year
            val monthNumberArgument = it.arguments?.getString("monthNumber")
            val monthNumber = monthNumberArgument?.toInt() ?: currentDate.monthNumber
            val month = LocalDate(year, monthNumber, 1)
            val context = LocalContext.current
            val viewModel = getViewModel<HomeViewModel>(parameters = {
                parametersOf(month)
                parametersOf(context.activity?.intent?.data)
            })
            val state by viewModel.state.collectAsStateWithLifecycle()

            HomePage(state, viewModel::dispatch)
        }

        popup(HomeGraph.Menu.route) {
            MenuPopup()
        }

        bottomSheet(
            HomeGraph.EntryDetails.route,
            arguments = HomeGraph.EntryDetails.arguments
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
            val id = it.arguments?.getString("id")?.let { value ->
                if (value == "new") {
                    null
                } else {
                    value.toInt()
                }
            }

            val viewModel = getViewModel<EntryDetailsViewModel>(parameters = { parametersOf(month, id) })
            EntryDetailsBottomSheetContent(viewModel)
        }
    }
}

fun NavController.navigateToHome() = navigate(AppGraph.Home.route) {
    popUpTo(0)
}

fun NavController.navigateToHomeMenu() = navigate(HomeGraph.Menu.route)

fun NavController.navigateToEntryDetails(month: LocalDate, id: Int? = null) =
    navigate(HomeGraph.EntryDetails.createDestination(month, id)) {
        popUpTo(HomeGraph.Root.route)
    }

fun NavController.navigateToMonth(year: Int, monthNumber: Int) =
    navigate(HomeGraph.Root.createDestination(year, monthNumber)) {
        popBackStack()
    }
