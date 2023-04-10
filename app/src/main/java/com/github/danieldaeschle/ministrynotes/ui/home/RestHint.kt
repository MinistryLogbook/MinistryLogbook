package com.github.danieldaeschle.ministrynotes.ui.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.lib.Time
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel

@Composable
fun RestHint(viewModel: HomeViewModel = koinViewModel()) {
    val rest by viewModel.rest.collectAsState(Time.Empty)
    val actualDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val show = rest.minutes > 0 && (viewModel.month.year < actualDate.year
            || viewModel.month.monthNumber < actualDate.monthNumber)

    if (show) {
        Spacer(Modifier.height(16.dp))

        Tile(
            title = {
                Text(stringResource(R.string.this_month_time_remaining_title))
            },
            actions = {
                TextButton(onClick = { viewModel.transferToNextMonth(rest.minutes) }) {
                    Text(stringResource(R.string.transfer_to_next_month))
                }
            }
        ) {
            Text(stringResource(R.string.this_month_time_remaining_description, rest.minutes))
        }
    }
}