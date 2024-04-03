package app.ministrylogbook.ui.home.time

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.ExpandAnimatedVisibility
import app.ministrylogbook.shared.utilities.getLocale
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.share.navigateToShare
import app.ministrylogbook.ui.shared.Tile
import java.time.format.TextStyle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus

@Composable
fun SendReportHint(state: HomeState, dispatch: (intent: HomeIntent) -> Unit = {}) {
    val navController = LocalAppNavController.current

    if (state.lastMonthReportSent != null) {
        val show = !state.lastMonthReportSent
        ExpandAnimatedVisibility(show) {
            val lastMonth = state.month.minus(DatePeriod(months = 1))
            val locale = getLocale()
            val lastMonthName = lastMonth.month.getDisplayName(TextStyle.FULL, locale)

            Column {
                Spacer(Modifier.height(16.dp))

                Tile(
                    title = {
                        Text(stringResource(R.string.send_lastr_report, lastMonthName))
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                navController.navigateToShare(lastMonth.year, lastMonth.monthNumber)
                            }
                        ) {
                            Text(stringResource(R.string.send_report))
                        }
                    },
                    onDismiss = {
                        dispatch(HomeIntent.DismissSendReportHint)
                    }
                ) {
                    Text(stringResource(R.string.send_last_report_description))
                }
            }
        }
    }
}
