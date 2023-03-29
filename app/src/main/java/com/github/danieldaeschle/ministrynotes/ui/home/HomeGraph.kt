package com.github.danieldaeschle.ministrynotes.ui.home

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.github.danieldaeschle.ministrynotes.lib.bottomSheet
import com.github.danieldaeschle.ministrynotes.lib.popup
import com.github.danieldaeschle.ministrynotes.ui.AppGraph
import com.github.danieldaeschle.ministrynotes.ui.home.recorddetails.EntryDetailsBottomSheetContent
import com.github.danieldaeschle.ministrynotes.ui.home.recorddetails.StudiesBottomSheetContent
import com.github.danieldaeschle.ministrynotes.ui.home.share.ShareDialogContent
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.EntryDetailsViewModel
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.StudiesDetailsViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.getViewModel

sealed class HomeGraph(val route: String, val arguments: List<NamedNavArgument> = listOf()) {
    object Root : HomeGraph(
        route = "root?year={year}&monthNumber={monthNumber}", arguments = listOf(
            navArgument("year") { nullable = true; defaultValue = null },
            navArgument("monthNumber") { nullable = true; defaultValue = null },
        )
    ) {
        fun createRoute(year: Int, monthNumber: Int): String {
            return "root?year=${year}&monthNumber=${monthNumber}"
        }
    }

    object Share : HomeGraph(
        route = "home/{year}/{monthNumber}/share", arguments = listOf(
            navArgument("year") { type = NavType.IntType },
            navArgument("monthNumber") { type = NavType.IntType },
        )
    ) {
        fun createRoute(year: Int, monthNumber: Int): String {
            return "home/${year}/${monthNumber}/share"
        }
    }

    object Studies : HomeGraph(
        route = "home/{year}/{monthNumber}/studies", arguments = listOf(
            navArgument("year") { type = NavType.IntType },
            navArgument("monthNumber") { type = NavType.IntType },
        )
    ) {
        fun createRoute(year: Int, monthNumber: Int): String {
            return "home/${year}/${monthNumber}/studies"
        }
    }

    object EntryDetails : HomeGraph(
        route = "home/entry-details/{id}", arguments = listOf(
            navArgument("id") { nullable = true; defaultValue = null },
        )
    ) {
        fun createRoute(id: Int? = null): String {
            if (id == null) {
                return "home/entry-details/new"
            }
            return "home/entry-details/$id"
        }
    }

    object ProfileMenu : HomeGraph(route = "home/profile")
}

fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    navigation(route = AppGraph.Home.route, startDestination = HomeGraph.Root.route) {
        composable(HomeGraph.Root.route, arguments = HomeGraph.Root.arguments) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = it.arguments?.getString("year")?.toInt() ?: currentDate.year
            val monthNumber =
                it.arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber

            HomePage(year, monthNumber)
        }

        dialog(HomeGraph.Share.route, arguments = HomeGraph.Share.arguments) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = it.arguments?.getString("year")?.toInt() ?: currentDate.year
            val monthNumber =
                it.arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber

            ShareDialogContent()
        }

        bottomSheet(HomeGraph.EntryDetails.route, arguments = HomeGraph.Root.arguments) {
            val id = it.arguments?.getString("id")?.let { value ->
                if (value == "new") {
                    null
                } else {
                    value.toInt()
                }
            }

            val entryDetailsViewModel = getViewModel<EntryDetailsViewModel>()
            EntryDetailsBottomSheetContent(id, entryDetailsViewModel)
        }

        bottomSheet(HomeGraph.Studies.route, arguments = HomeGraph.Root.arguments) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = it.arguments?.getString("year")?.toInt() ?: currentDate.year
            val monthNumber =
                it.arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber

            val studiesDetailsViewModel = getViewModel<StudiesDetailsViewModel>()
            StudiesBottomSheetContent(year, monthNumber, studiesDetailsViewModel)
        }

        popup(HomeGraph.ProfileMenu.route) {
            ProfilePopup()
        }
    }
}