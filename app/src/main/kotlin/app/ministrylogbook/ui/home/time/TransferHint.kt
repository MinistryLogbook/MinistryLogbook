package app.ministrylogbook.ui.home.time

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
import app.ministrylogbook.R
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.layouts.ExpandAnimatedVisibility
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState

@Composable
fun TransferHint(state: HomeState, dispatch: (intent: HomeIntent) -> Unit = {}) {
    val hasTransfer by remember(state) {
        derivedStateOf {
            state.entries.any { it.type == EntryType.Transfer }
        }
    }
    val show = state.restLastMonth.isNotEmpty && !hasTransfer

    ExpandAnimatedVisibility(show) {
        Column {
            Spacer(Modifier.height(16.dp))

            Tile(
                title = {
                    Text(stringResource(R.string.last_month_time_remaining_title))
                },
                actions = {
                    TextButton(
                        onClick = {
                            val intent = HomeIntent.TransferFromLastMonth(state.restLastMonth.minutes)
                            dispatch(intent)
                        },
                        enabled = show
                    ) {
                        Text(stringResource(R.string.to_transfer))
                    }
                },
                onDismiss = {
                    val intent = HomeIntent.TransferFromLastMonth(0)
                    dispatch(intent)
                }
            ) {
                Text(
                    stringResource(
                        R.string.last_month_time_remaining_description,
                        state.restLastMonth.minutes
                    )
                )
            }
        }
    }
}
