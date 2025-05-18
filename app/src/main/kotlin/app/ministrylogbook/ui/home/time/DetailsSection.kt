package app.ministrylogbook.ui.home.time

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.layouts.ExpandAnimatedVisibility
import app.ministrylogbook.shared.layouts.placeholder.PlaceholderHighlight
import app.ministrylogbook.shared.layouts.placeholder.material3.fade
import app.ministrylogbook.shared.layouts.placeholder.material3.placeholder
import app.ministrylogbook.shared.layouts.progress.CircleProgressIndicator
import app.ministrylogbook.shared.layouts.progress.ProgressKind
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.theocraticAssignmentTimeSum
import app.ministrylogbook.shared.utilities.theocraticSchoolTimeSum
import app.ministrylogbook.shared.utilities.timeSum
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.theme.ProgressPositive
import kotlin.math.min

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DetailsSection(state: HomeState) {
    // credit will be added until goal + 5 hours are reached
    // example: goal = 50, credit = 55
    val maxHoursWithCredit = remember(state.roleGoal) { Time((state.roleGoal ?: 0) + 5, 0) }
    val transferredTime = remember(state.transferred) { state.transferred.timeSum() }
    val ministryTime = remember(state.entries, transferredTime) { state.entries.ministryTimeSum() - transferredTime }
    val theocraticAssignmentsTime = remember(state.entries) { state.entries.theocraticAssignmentTimeSum() }
    val theocraticSchoolTime = remember(state.entries) { state.entries.theocraticSchoolTimeSum() }
    val credit = remember(
        ministryTime,
        theocraticAssignmentsTime,
        maxHoursWithCredit,
        theocraticSchoolTime
    ) {
        minOf(theocraticAssignmentsTime, maxHoursWithCredit - ministryTime) + theocraticSchoolTime
    }
    val accumulatedTime by remember(
        ministryTime,
        theocraticAssignmentsTime,
        maxHoursWithCredit,
        theocraticSchoolTime
    ) {
        derivedStateOf {
            ministryTime.let {
                if (it.hours < maxHoursWithCredit.hours) {
                    minOf(
                        ministryTime + theocraticAssignmentsTime,
                        maxHoursWithCredit
                    )
                } else {
                    it
                }
            } + theocraticSchoolTime
        }
    }
    val remainingHours by remember(state.role, state.goal, accumulatedTime, ministryTime) {
        derivedStateOf {
            val hours = if (state.role.canHaveCredit) {
                accumulatedTime.hours
            } else {
                ministryTime.hours
            }
            if (state.goal != null) {
                state.goal - min(hours, state.goal)
            } else {
                null
            }
        }
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        BoxWithConstraints {
            val widthDp = LocalDensity.current.run {
                min(constraints.maxWidth.toDp(), LocalConfiguration.current.screenHeightDp.dp)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(widthDp / 2)
                        .width(widthDp / 2)
                ) {
                    val accPercent = if (state.goal != null) {
                        minOf(1f, 1f / state.goal * (accumulatedTime.hours + accumulatedTime.minutes.toFloat() / 60))
                    } else {
                        1f
                    }
                    val ministryPercent = if (state.goal != null) {
                        minOf(1f, 1f / state.goal * (ministryTime.hours + ministryTime.minutes.toFloat() / 60))
                    } else {
                        1f
                    }
                    CircleProgressIndicator(
                        modifier = Modifier.size(widthDp / 2, widthDp / 2),
                        baseLineColor = ProgressPositive.copy(0.15f),
                        progresses = listOfNotNull(
                            ProgressKind.Progress(
                                percent = accPercent,
                                color = ProgressPositive.copy(0.6f)
                            ).takeIf { state.role.canHaveCredit && credit.isNotEmpty },
                            ProgressKind.Progress(
                                percent = ministryPercent,
                                color = ProgressPositive
                            ).takeIf { ministryTime.isNotEmpty }
                        )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Counter(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            time = ministryTime
                        )

                        AnimatedVisibility(
                            visible = state.role.canHaveCredit && credit > Time(0, 0),
                            enter = expandVertically(
                                tween(
                                    durationMillis = 200,
                                    delayMillis = 600
                                )
                            ) + fadeIn(tween(delayMillis = 850)),
                            exit = shrinkVertically(
                                tween(
                                    durationMillis = 200,
                                    delayMillis = AnimationConstants.DefaultDurationMillis + 50
                                )
                            ) + fadeOut(tween())
                        ) {
                            Row(
                                Modifier
                                    .clip(CircleShape)
                                    .defaultMinSize(minWidth = 48.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
                                    .padding(horizontal = 8.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val creditMinutes =
                                    if (credit.minutes > 0) {
                                        ":${credit.minutes.toString().padStart(2, '0')}"
                                    } else {
                                        ""
                                    }

                                Icon(
                                    painterResource(R.drawable.ic_volunteer_activism),
                                    contentDescription = null, // TODO: contentDescription
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                val text = stringResource(
                                    R.string.hours_short_unit,
                                    "${credit.hours}$creditMinutes"
                                )
                                Text(
                                    text,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        ExpandAnimatedVisibility(
            show = (state.hasGoal == null || state.hasGoal) && (remainingHours == null || (remainingHours ?: 0) > 0)
        ) {
            Column {
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val text = if ((remainingHours ?: 0) > 0) {
                        val remainingHoursAnimated by animateIntAsState(
                            targetValue = remainingHours ?: 0,
                            animationSpec = tween(400),
                            label = "remainingHours"
                        )
                        pluralStringResource(
                            R.plurals.hours_remaining,
                            remainingHoursAnimated,
                            remainingHoursAnimated
                        )
                    } else {
                        pluralStringResource(R.plurals.hours_remaining, 0, 0)
                    }

                    Text(
                        text,
                        color = ProgressPositive,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.placeholder(
                            visible = (remainingHours ?: 0) == 0,
                            highlight = PlaceholderHighlight.fade()
                        )
                    )
                }
            }
        }
    }
}
