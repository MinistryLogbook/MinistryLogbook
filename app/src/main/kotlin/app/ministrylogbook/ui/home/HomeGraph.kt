package app.ministrylogbook.ui.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.ministrylogbook.lib.bottomSheet
import app.ministrylogbook.lib.popup
import app.ministrylogbook.lib.stayOut
import app.ministrylogbook.ui.AppGraph
import app.ministrylogbook.ui.SlideOutTransitionMillis
import app.ministrylogbook.ui.home.recorddetails.EntryDetailsBottomSheetContent
import app.ministrylogbook.ui.home.recorddetails.StudiesBottomSheetContent
import app.ministrylogbook.ui.home.viewmodel.EntryDetailsViewModel
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.home.viewmodel.StudiesDetailsViewModel
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

sealed class HomeGraph(
    private val rawRoute: String,
    val arguments: List<NamedNavArgument> = listOf()
) {
    object Root : HomeGraph(
        rawRoute = "?year={year}&monthNumber={monthNumber}", arguments = listOf(
            navArgument("year") {
                nullable = true
            },
            navArgument("monthNumber") {
                nullable = true
            },
        )
    ) {
        fun createDestination(year: Int, monthNumber: Int): String {
            return "${AppGraph.Home.route}/?year=${year}&monthNumber=${monthNumber}"
        }
    }

    object Studies : HomeGraph(
        rawRoute = "{year}/{monthNumber}/studies", arguments = listOf(
            navArgument("year") { type = NavType.IntType },
            navArgument("monthNumber") { type = NavType.IntType },
        )
    ) {
        fun createDestination(year: Int, monthNumber: Int): String {
            return "${AppGraph.Home.route}/${year}/${monthNumber}/studies"
        }
    }

    object EntryDetails : HomeGraph(
        rawRoute = "{year}/{monthNumber}/entry-details/{id}", arguments = listOf(
            navArgument("id") { nullable = true; defaultValue = null },
        )
    ) {
        fun createDestination(month: LocalDate, id: Int? = null): String {
            if (id == null) {
                return "${AppGraph.Home.route}/${month.year}/${month.monthNumber}/entry-details/new"
            }
            return "${AppGraph.Home.route}/${month.year}/${month.monthNumber}/entry-details/$id"
        }
    }

    object Menu : HomeGraph(rawRoute = "menu")

    val route
        get() = "${AppGraph.Home.route}/${rawRoute}"
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.homeGraph() {
    navigation(
        route = AppGraph.Home.route, startDestination = HomeGraph.Root.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { stayOut(SlideOutTransitionMillis) },
    ) {
        composable(
            HomeGraph.Root.route,
            arguments = HomeGraph.Root.arguments,
        ) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val yearArgument = it.arguments?.getString("year")
            val year = yearArgument?.toInt() ?: currentDate.year
            val monthNumberArgument = it.arguments?.getString("monthNumber")
            val monthNumber = monthNumberArgument?.toInt() ?: currentDate.monthNumber
            val month = LocalDate(year, monthNumber, 1)

            val homeViewModel =
                getViewModel<HomeViewModel>(parameters = { parametersOf(month) })
            HomePage(homeViewModel)
        }

        bottomSheet(HomeGraph.EntryDetails.route, arguments = HomeGraph.Root.arguments) {
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

            val entryDetailsViewModel = getViewModel<EntryDetailsViewModel>(parameters = {
                parametersOf(month, id)
            })
            EntryDetailsBottomSheetContent(entryDetailsViewModel)
        }

        bottomSheet(HomeGraph.Studies.route, arguments = HomeGraph.Root.arguments) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = it.arguments?.getString("year")?.toInt() ?: currentDate.year
            val monthNumber =
                it.arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber
            val month = LocalDate(year, monthNumber, 1)

            val studiesDetailsViewModel = getViewModel<StudiesDetailsViewModel>(parameters = {
                parametersOf(month)
            })
            StudiesBottomSheetContent(studiesDetailsViewModel)
        }

        popup(HomeGraph.Menu.route) {
            MenuPopup()
        }
    }
}