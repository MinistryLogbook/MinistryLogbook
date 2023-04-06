package com.github.danieldaeschle.ministrynotes.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.lib.ExtendableFloatingActionButton
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.detailssection.DetailsSection
import com.github.danieldaeschle.ministrynotes.ui.home.historysection.HistorySection
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import com.github.danieldaeschle.ministrynotes.ui.shared.Toolbar
import org.koin.androidx.compose.koinViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage(homeViewModel: HomeViewModel = koinViewModel()) {
    var fabExtended by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val navController = LocalAppNavController.current
    val entries by homeViewModel.entries.collectAsState()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    LaunchedEffect(currentRoute) {
        if (currentRoute == HomeGraph.Root.route) {
            homeViewModel.load()
        }
    }

    LaunchedEffect(scrollState) {
        var prev = 0
        snapshotFlow { scrollState.value }.collect {
            fabExtended = it <= prev
            prev = it
        }
    }

    Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp), floatingActionButton = {
        ExtendableFloatingActionButton(onClick = {
            navController.navigate(HomeGraph.EntryDetails.createRoute())
        }, extended = fabExtended, icon = {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null)
        }, text = {
            Text("Add to report")
        })
    }) {
        Box {
            Toolbar(
                modifier = Modifier.zIndex(1f),
                elevation = if (scrollState.value > 0) 4.dp else 0.dp,
            ) {
                ToolbarMonthSelect()
                Spacer(Modifier.weight(1f))
                ToolbarActions()
            }
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(bottom = 82.dp)
            ) {
                Box(modifier = Modifier.statusBarsPadding()) {
                    Spacer(modifier = Modifier.height(56.dp))
                }

                Spacer(Modifier.height(16.dp))

                Box(Modifier.padding(horizontal = 16.dp)) {
                    DetailsSection()
                }

                TransferHint()

                if (entries.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onBackground.copy(0.05f))
                }

                HistorySection()
            }
        }
    }
}
