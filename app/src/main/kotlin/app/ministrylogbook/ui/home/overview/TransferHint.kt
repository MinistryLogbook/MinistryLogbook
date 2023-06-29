package app.ministrylogbook.ui.home.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.layouts.ExpandAnimatedVisibility
import app.ministrylogbook.ui.home.viewmodel.OverviewViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransferHint(viewModel: OverviewViewModel = koinViewModel()) {
    val restLastMonth by viewModel.restLastMonth.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val hasTransfer by remember {
        derivedStateOf {
            entries.any { it.type == EntryType.Transfer }
        }
    }
    val show = restLastMonth.isNotEmpty && !hasTransfer

    ExpandAnimatedVisibility(show) {
        Column {
            Spacer(Modifier.height(16.dp))

            Tile(
                title = {
                    Text(stringResource(R.string.last_month_time_remaining_title))
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.transferFromLastMonth(restLastMonth.minutes) },
                        enabled = show
                    ) {
                        Text(stringResource(R.string.to_transfer))
                    }
                },
                onDismiss = { viewModel.transferFromLastMonth(0) }
            ) {
                Text(
                    stringResource(
                        R.string.last_month_time_remaining_description,
                        restLastMonth.minutes
                    )
                )
            }
        }
    }
}
