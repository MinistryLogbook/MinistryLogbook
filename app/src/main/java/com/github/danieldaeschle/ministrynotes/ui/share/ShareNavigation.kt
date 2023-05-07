package com.github.danieldaeschle.ministrynotes.ui.share

import androidx.navigation.NavController
import com.github.danieldaeschle.ministrynotes.ui.home.HomeGraph

fun NavController.navigateToShare(year: Int, monthNumber: Int) = navigate(ShareGraph.Root.route) {
    popUpTo(HomeGraph.Root.createDestination(year, monthNumber))
}