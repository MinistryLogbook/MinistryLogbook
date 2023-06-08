package app.ministrylogbook.ui.home

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument

sealed class InnerHomeGraph(
    private val rawRoute: String,
    val arguments: List<NamedNavArgument> = listOf()
) {
    object Overview : InnerHomeGraph(
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
        fun createDestination(year: Int, monthNumber: Int) = "?year=$year&monthNumber=$monthNumber"
    }

    override fun toString() = route

    val route
        get() = rawRoute
}

fun HomeNavHostController.navigateToMonth(year: Int, monthNumber: Int) =
    navigate(InnerHomeGraph.Overview.createDestination(year, monthNumber)) {
        popBackStack()
    }
