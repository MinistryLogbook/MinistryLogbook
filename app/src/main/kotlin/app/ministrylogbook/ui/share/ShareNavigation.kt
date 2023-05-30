package app.ministrylogbook.ui.share

import androidx.navigation.NavController
import app.ministrylogbook.ui.home.HomeGraph

fun NavController.navigateToShare(year: Int, monthNumber: Int) =
    navigate(ShareGraph.Root.createDestination(year, monthNumber)) {
        popUpTo(HomeGraph.Root.createDestination(year, monthNumber))
    }