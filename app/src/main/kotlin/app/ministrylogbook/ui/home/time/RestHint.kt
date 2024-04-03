package app.ministrylogbook.ui.home.time

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.shared.Tile
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun RestHint(state: HomeState, dispatch: (intent: HomeIntent) -> Unit = {}) {
    val actualDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val show = state.rest.minutes > 0 && (
        state.month.year < actualDate.year || state.month.monthNumber < actualDate.monthNumber
        )

    if (show) {
        Spacer(Modifier.height(16.dp))

        Tile(
            title = {
                Text(stringResource(R.string.this_month_time_remaining_title))
            },
            actions = {
                TextButton(
                    onClick = {
                        val intent = HomeIntent.TransferToTextMonth(state.rest.minutes)
                        dispatch(intent)
                    }
                ) {
                    Text(stringResource(R.string.transfer_to_next_month))
                }
            }
        ) {
            Text(stringResource(R.string.this_month_time_remaining_description, state.rest.minutes))
        }
    }
}
