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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun TimePage(
    state: HomeState,
    dispatch: (intent: HomeIntent) -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    var fabExtended by remember { mutableStateOf(true) }
    val navController = LocalAppNavController.current

    LaunchedEffect(scrollState) {
        var prev = 0
        snapshotFlow { scrollState.value }.collect {
            fabExtended = it <= prev
            prev = it
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
    }
}
