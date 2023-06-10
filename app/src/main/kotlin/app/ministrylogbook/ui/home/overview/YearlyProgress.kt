package app.ministrylogbook.ui.home.overview

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.ministryTimeSum
import app.ministrylogbook.shared.progress.LinearProgressIndicator
import app.ministrylogbook.shared.progress.Progress
import app.ministrylogbook.shared.splitIntoMonths
import app.ministrylogbook.shared.sum
import app.ministrylogbook.shared.timeSum
import app.ministrylogbook.ui.home.viewmodel.OverviewViewModel
import app.ministrylogbook.ui.theme.ProgressPositive
import org.koin.androidx.compose.koinViewModel

@Composable
fun YearlyProgress(viewModel: OverviewViewModel = koinViewModel()) {
    val role by viewModel.role.collectAsStateWithLifecycle()
    val pioneerSince by viewModel.pioneerSince.collectAsStateWithLifecycle()
    val show by remember(role, pioneerSince) {
        derivedStateOf {
            val isPioneer = role == Role.SpecialPioneer || role == Role.RegularPioneer
            isPioneer && pioneerSince != null && pioneerSince!! <= viewModel.month
        }
    }

    if (show) {
        Spacer(Modifier.height(16.dp))

        Tile(title = { Text(stringResource(R.string.progress_of_yearly_goal)) }) {
            val yearlyGoal by viewModel.yearlyGoal.collectAsStateWithLifecycle()
            val goal by viewModel.roleGoal.collectAsStateWithLifecycle()
            val maxHoursWithCredit by remember(goal) { derivedStateOf { Time(goal + 5, 0) } }
            val entriesInServiceYear by viewModel.entriesInServiceYear.collectAsStateWithLifecycle()
            val time by remember(entriesInServiceYear, maxHoursWithCredit) {
                derivedStateOf {
                    entriesInServiceYear.splitIntoMonths().map {
                        val ministryTimeSum = it.ministryTimeSum()
                        minOf(maxOf(ministryTimeSum, maxHoursWithCredit), it.timeSum())
                    }.sum()
                }
            }
            val ministryTime by remember(entriesInServiceYear) {
                derivedStateOf {
                    entriesInServiceYear.ministryTimeSum()
                }
            }
            val remaining by remember(time, yearlyGoal) { derivedStateOf { yearlyGoal - time.hours } }

            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = AnnotatedString(
                        time.hours.toString(),
                        spanStyle = SpanStyle(fontSize = 20.sp)
                    ) + AnnotatedString(" ${stringResource(R.string.of)} ") + AnnotatedString(
                        text = stringResource(R.string.hours_value_short_unit, yearlyGoal),
                        spanStyle = SpanStyle(fontSize = 20.sp)
                    ),
                    color = ProgressPositive,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(16.dp))

                LinearProgressIndicator(
                    progresses = listOf(
                        Progress(percent = (1f / yearlyGoal * time.hours), color = ProgressPositive.copy(alpha = .6f)),
                        Progress(percent = (1f / yearlyGoal * ministryTime.hours), color = ProgressPositive)
                    ),
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth(),
                    strokeCap = StrokeCap.Round
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    text = stringResource(R.string.hours_remaining, remaining),
                    fontSize = 14.sp
                )
            }
        }
    }
}
