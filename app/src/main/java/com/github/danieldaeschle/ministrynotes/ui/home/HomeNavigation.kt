package com.github.danieldaeschle.ministrynotes.ui.home

import androidx.navigation.NavController
import kotlinx.datetime.LocalDate

fun NavController.navigateToStudies(year: Int, monthNumber: Int) =
    navigate(HomeGraph.Studies.createRoute(year, monthNumber)) {
        popUpTo(HomeGraph.Root.route)
    }


fun NavController.navigateToEntryDetails(month: LocalDate, id: Int? = null) =
    navigate(HomeGraph.EntryDetails.createRoute(month, id)) {
        popUpTo(HomeGraph.Root.route)

    }

fun NavController.navigateToMenu() = navigate(HomeGraph.Menu.route)

fun NavController.navigateToMonth(year: Int, monthNumber: Int) =
    navigate(HomeGraph.Root.createRoute(year, monthNumber)) {
        popBackStack()
    }