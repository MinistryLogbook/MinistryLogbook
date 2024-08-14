package app.ministrylogbook.ui.home.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.layouts.progress.LinearProgressIndicator
import app.ministrylogbook.shared.layouts.progress.ProgressKind
import app.ministrylogbook.shared.sum
import app.ministrylogbook.shared.toTime
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.splitIntoMonths
import app.ministrylogbook.shared.utilities.theocraticAssignmentTimeSum
import app.ministrylogbook.shared.utilities.theocraticSchoolTimeSum
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.shared.Tile
import app.ministrylogbook.ui.theme.ProgressPositive

@Composable
fun YearlyProgress(state: HomeState) {
    val show by remember(state.role, state.beginOfPioneeringInServiceYear, state.month) {
        derivedStateOf {
            val isPioneer = state.role == Role.SpecialPioneer || state.role == Role.RegularPioneer
            isPioneer && state.beginOfPioneeringInServiceYear != null &&
                state.beginOfPioneeringInServiceYear <= state.month
        }
    }

    if (show) {
        Spacer(Modifier.height(16.dp))

        Tile(title = { Text(stringResource(R.string.progress_of_yearly_goal)) }) {
            val maxHoursWithCredit by remember(state.goal) {
                derivedStateOf { Time(state.roleGoal?.plus(5) ?: 0, 0) }
            }
            val time by remember(state.entriesInServiceYear, maxHoursWithCredit) {
                derivedStateOf {
                    state.entriesInServiceYear.splitIntoMonths().map {
                        val ministryTimeSum = it.ministryTimeSum()
                        val ministryHoursTime = ministryTimeSum.hours.toTime()
                        val theocraticSchoolTimeSum = it.theocraticSchoolTimeSum()
                        val theocraticAssignmentTimeSum = it.theocraticAssignmentTimeSum()
                        val max = maxOf(ministryTimeSum, maxHoursWithCredit)
                        minOf(max, ministryHoursTime + theocraticAssignmentTimeSum) + theocraticSchoolTimeSum
                    }.sum()
                }
            }
            val ministryTime by remember(state.entriesInServiceYear) {
                derivedStateOf {
                    state.entriesInServiceYear.ministryTimeSum()
                }
            }
            val remaining by remember(time, state.yearlyGoal) {
                derivedStateOf { state.yearlyGoal - time.hours }
            }

            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = AnnotatedString(
                        time.hours.toString(),
                        spanStyle = SpanStyle(fontSize = 20.sp)
                    ) + AnnotatedString(" ${stringResource(R.string.of)} ") + AnnotatedString(
                        text = stringResource(R.string.hours_value_short_unit, state.yearlyGoal),
                        spanStyle = SpanStyle(fontSize = 20.sp)
                    ),
                    color = ProgressPositive,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(16.dp))

//                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
//                val september = LocalDate(today.year, 9, 1)
//                val daysInServiceYear = state.beginOfPioneeringInServiceYear?.until(september, DateTimeUnit.DAY) ?: 1
//                val dayInServiceYear = min(
//                    state.beginOfPioneeringInServiceYear?.until(today, DateTimeUnit.DAY) ?: 0,
//                    state.beginOfPioneeringInServiceYear?.until(state.month.lastDayOfMonth, DateTimeUnit.DAY)
//                        ?: 0
//                )
                LinearProgressIndicator(
                    progresses = listOf(
                        ProgressKind.Progress(
                            percent = (1f / state.yearlyGoal * time.hours),
                            color = ProgressPositive.copy(alpha = .6f)
                        ),
                        ProgressKind.Progress(
                            percent = (1f / state.yearlyGoal * ministryTime.hours),
                            color = ProgressPositive
                        )
//                        ProgressKind.Indicator(
//                            percent = 1f / daysInServiceYear * dayInServiceYear,
//                            color = MaterialTheme.colorScheme.secondary
//                        )
                    ),
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth(),
                    strokeCap = StrokeCap.Round
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val text = if (remaining > 0) {
                    pluralStringResource(R.plurals.hours_remaining, remaining, remaining)
                } else {
                    stringResource(R.string.goal_reached)
                }
                Text(
                    text = text,
                    fontSize = 14.sp
                )
            }
        }
    }
}
