package com.github.danieldaeschle.ministrylogbook.ui.home

import androidx.navigation.NavController
import kotlinx.datetime.LocalDate

fun NavController.navigateToStudies(year: Int, monthNumber: Int) =
    navigate(HomeGraph.Studies.createDestination(year, monthNumber)) {
        popUpTo(HomeGraph.Root.route)
    }

fun NavController.navigateToEntryDetails(month: LocalDate, id: Int? = null) =
    navigate(HomeGraph.EntryDetails.createDestination(month, id)) {
        popUpTo(HomeGraph.Root.route)
    }

fun NavController.navigateToMenu() = navigate(HomeGraph.Menu.route)

fun NavController.navigateToMonth(year: Int, monthNumber: Int) =
    navigate(HomeGraph.Root.createDestination(year, monthNumber)) {
        popBackStack()
    }