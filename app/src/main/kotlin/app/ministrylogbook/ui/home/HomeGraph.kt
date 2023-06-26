package app.ministrylogbook.ui.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import app.ministrylogbook.shared.bottomSheet
import app.ministrylogbook.shared.popup
import app.ministrylogbook.shared.stayOut
import app.ministrylogbook.ui.AppGraph
import app.ministrylogbook.ui.AppNavHostController
import app.ministrylogbook.ui.SlideOutTransitionMillis
import app.ministrylogbook.ui.home.entrydetails.EntryDetailsBottomSheetContent
import app.ministrylogbook.ui.home.entrydetails.StudiesBottomSheetContent
import app.ministrylogbook.ui.home.viewmodel.EntryDetailsViewModel
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

sealed class HomeGraph(private val rawRoute: String, val arguments: List<NamedNavArgument> = listOf()) {
    object Root : HomeGraph(rawRoute = "")

    object Menu : HomeGraph(rawRoute = "menu")

    object Studies : HomeGraph(rawRoute = "{year}/{monthNumber}/studies") {
        fun createDestination(year: Int, monthNumber: Int) = "${AppGraph.Home}/$year/$monthNumber/studies"
    }

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

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.homeGraph() {
    navigation(
        route = AppGraph.Home.route,
        startDestination = HomeGraph.Root.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { stayOut(SlideOutTransitionMillis) }
    ) {
        composable(HomeGraph.Root.route) {
            HomePage()
        }

        popup(HomeGraph.Menu.route) {
            MenuPopup()
        }

        bottomSheet(HomeGraph.EntryDetails.route, arguments = HomeGraph.EntryDetails.arguments) {
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

        bottomSheet(HomeGraph.Studies.route, arguments = HomeGraph.Studies.arguments) {
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
    }
}

fun NavController.navigateToHome() = navigate(AppGraph.Home.route) {
    popUpTo(AppGraph.Intro.route) {
        inclusive = true
    }
}

fun AppNavHostController.navigateToHomeMenu() = navigate(HomeGraph.Menu.route)

fun HomeNavHostController.navigateToStudies(year: Int, monthNumber: Int) =
    navigate(HomeGraph.Studies.createDestination(year, monthNumber)) {
        popUpTo(HomeGraph.Root.route)
    }

fun HomeNavHostController.navigateToEntryDetails(month: LocalDate, id: Int? = null) =
    navigate(HomeGraph.EntryDetails.createDestination(month, id)) {
        popUpTo(HomeGraph.Root.route)
    }
