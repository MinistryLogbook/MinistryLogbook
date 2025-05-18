package app.ministrylogbook.ui.home.time

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.ExtendableFloatingActionButton
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.navigateToEntryDetails
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.PartySystem

@Composable
fun TimePage(
    state: HomeState,
    dispatch: (intent: HomeIntent) -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    var goalDialogOpen by remember { mutableStateOf(false) }
    val isYearlyGoalReached = remember { state.yearlyProgress.hours >= state.yearlyGoal }
    var fabExtended by remember { mutableStateOf(true) }
    val navController = LocalAppNavController.current

    LaunchedEffect(scrollState) {
        var prev = 0
        snapshotFlow { scrollState.value }.collect {
            fabExtended = it <= prev
            prev = it
        }
    }

    LaunchedEffect(isYearlyGoalReached, state.yearlyGoal, state.yearlyProgress) {
        if (!isYearlyGoalReached && state.yearlyProgress.hours >= state.yearlyGoal) {
            goalDialogOpen = true
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 82.dp, top = 24.dp)
        ) {
            DetailsSection(state)

            Spacer(Modifier.height(16.dp))

            YearlyProgress(state)
            TransferHint(state, dispatch)
            RestHint(state, dispatch)
            SendReportHint(state, dispatch)

            if (state.entries.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(0.05f))
            }

            HistorySection(state, dispatch)
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ExtendableFloatingActionButton(
                onClick = {
                    navController.navigateToEntryDetails(state.month)
                },
                extended = fabExtended,
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = null // TODO: contentDescription
                    )
                },
                text = {
                    Text(stringResource(R.string.create_entry))
                }
            )
        }

        if (goalDialogOpen) {
            AlertDialog(
                title = { Text("🎉 Gratulation!") },
                text = { Text("Du hast dein Jahresziel erreicht!") },
                onDismissRequest = { goalDialogOpen = false },
                confirmButton = {
                    TextButton(onClick = { goalDialogOpen = false }) {
                        Text("Yay!")
                    }
                }
            )
        }

        if (state.yearlyParties.isNotEmpty()) {
            DisposableEffect(Unit) {
                onDispose {
                    dispatch(HomeIntent.YearlyPartyFinished)
                }
            }
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = state.yearlyParties,
                updateListener = object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                        dispatch(HomeIntent.YearlyPartyFinished)
                    }
                }
            )
        } else if (state.monthlyParties.isNotEmpty()) {
            DisposableEffect(Unit) {
                onDispose {
                    dispatch(HomeIntent.MonthlyPartyFinished)
                }
            }
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = state.monthlyParties,
                updateListener = object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                        dispatch(HomeIntent.MonthlyPartyFinished)
                    }
                }
            )
        }
    }
}
