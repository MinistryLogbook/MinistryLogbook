package com.github.danieldaeschle.ministrynotes.ui.home.recorddetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.HomeGraph
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.StudiesDetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun StudiesBottomSheetContent(
    year: Int, monthNumber: Int, studiesDetailsViewModel: StudiesDetailsViewModel = koinViewModel()
) {
    val navController = LocalAppNavController.current
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val studyEntry = studiesDetailsViewModel.studyEntry.collectAsState()
    var tempStudies by remember(studyEntry.value) { mutableStateOf(studyEntry.value?.count ?: 0) }

    val handleClose: () -> Unit = {
        navController.popBackStack()
    }

    val handleSave: () -> Unit = {
        studiesDetailsViewModel.save(tempStudies)
        handleClose()
    }

    val handleChange: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            tempStudies = it
        }
    }

    LaunchedEffect(year, monthNumber, currentRoute) {
        if (currentRoute == HomeGraph.Studies.route) {
            studiesDetailsViewModel.load(year, monthNumber)
        }
    }

    Column(modifier = Modifier.navigationBarsPadding()) {
        DragLine()
        Toolbar(
            onClose = handleClose,
            onSave = handleSave,
            isSavable = true,
        )
        Divider()
        Column(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
                .fillMaxWidth()
        ) {
            UnitRow(
                "Studies",
                description = "Number of Different Bible Studies Conducted",
                icon = painterResource(R.drawable.ic_local_library)
            ) {
                NumberPicker(tempStudies, onChange = handleChange)
            }
        }
    }
}