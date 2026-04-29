package app.ministrylogbook.ui.home.time

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.snap
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.layouts.progress.CircleProgressIndicator
import app.ministrylogbook.shared.layouts.progress.CircleProgressSegment
import app.ministrylogbook.shared.layouts.progress.ProgressKind
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.theme.ProgressPositive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Clock

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DetailsSection(state: HomeState) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val timeSummary = remember(
        state.month,
        today,
        state.entries,
        state.entriesLastMonth,
        state.transferred,
        state.role,
        state.goal,
        state.roleGoal
    ) {
        HomeTimeCalculator.calculateSummary(
            month = state.month,
            today = today,
            entries = state.entries,
            entriesLastMonth = state.entriesLastMonth,
            transferred = state.transferred,
            role = state.role,
            goal = state.goal,
            roleGoal = state.roleGoal
        )
    }
    var animateCreditPill by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoaded) {
        if (state.isLoaded) {
            animateCreditPill = true
        }
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        BoxWithConstraints {
            val widthDp = LocalDensity.current.run {
                min(constraints.maxWidth.toDp(), LocalConfiguration.current.screenHeightDp.dp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val circleSize = widthDp * 0.55f
                val circleStrokeWidth = 26.dp
                Box(
                    modifier = Modifier
                        .height(circleSize)
                        .width(circleSize)
                ) {
                    val accPercent = if (state.goal != null) {
                        1f / state.goal * timeSummary.accumulatedTime.toFloat()
                    } else {
                        1f
                    }
                    val todayStartPercent = if (state.goal != null) {
                        1f / state.goal * timeSummary.accumulatedBeforeToday.toFloat()
                    } else {
                        0f
                    }
                    val animatedTodayAddedHours = remember { Animatable(0f) }
                    val minimumTodaySegmentPercent = with(LocalDensity.current) {
                        val textWidth = 24.dp.toPx()
                        val strokeWidthPx = circleStrokeWidth.toPx()
                        val radius = circleSize.toPx() / 2f - strokeWidthPx / 2f
                        ((textWidth + 4.dp.toPx()) / (radius * 2f * PI.toFloat())).coerceAtLeast(0.04f)
                    }
                    val todaySegmentStartPercent = if (timeSummary.todayAdded.isNotEmpty) {
                        todayStartPercent
                    } else {
                        (accPercent - minimumTodaySegmentPercent).coerceAtLeast(0f)
                    }
                    val todaySegmentEndPercent =
                        accPercent.coerceAtLeast(todaySegmentStartPercent + minimumTodaySegmentPercent)
                    val progressAnimationSpec = remember { tween<Float>(400) }
                    val animatedAccPercent = remember { Animatable(0.0001f) }
                    val animatedTodaySegmentStartPercent = remember { Animatable(0.0001f) }
                    val animatedTodaySegmentEndPercent = remember { Animatable(0.0001f) }

                    LaunchedEffect(
                        accPercent,
                        todaySegmentStartPercent,
                        todaySegmentEndPercent,
                        timeSummary.todayAdded.hours
                    ) {
                        launch {
                            animatedAccPercent.animateTo(
                                accPercent.coerceAtLeast(0f),
                                animationSpec = progressAnimationSpec
                            )
                        }
                        launch {
                            animatedTodaySegmentStartPercent.animateTo(
                                todaySegmentStartPercent.coerceAtLeast(0f),
                                animationSpec = progressAnimationSpec
                            )
                        }
                        launch {
                            animatedTodaySegmentEndPercent.animateTo(
                                todaySegmentEndPercent.coerceAtLeast(0f),
                                animationSpec = progressAnimationSpec
                            )
                        }
                        launch {
                            animatedTodayAddedHours.animateTo(
                                timeSummary.todayAdded.hours.toFloat(),
                                animationSpec = progressAnimationSpec
                            )
                        }
                    }

                    val animatedMainPercent = animatedAccPercent.value
                    val animatedSegmentEndPercent = if (animatedMainPercent < 1f) {
                        min(animatedTodaySegmentEndPercent.value, animatedMainPercent)
                    } else {
                        animatedTodaySegmentEndPercent.value
                    }
                    val animatedSegmentStartPercent =
                        min(animatedTodaySegmentStartPercent.value, animatedSegmentEndPercent)
                    CircleProgressIndicator(
                        modifier = Modifier.size(circleSize, circleSize),
                        strokeWidth = circleStrokeWidth,
                        animationSpec = snap(),
                        baseLineColor = ProgressPositive.copy(0.15f),
                        progress = ProgressKind.Progress(
                            percent = animatedMainPercent,
                            color = ProgressPositive
                        ).takeIf { timeSummary.accumulatedTime.isNotEmpty },
                        segment = if (timeSummary.showTodaySegment) {
                            CircleProgressSegment(
                                startPercent = animatedSegmentStartPercent,
                                endPercent = animatedSegmentEndPercent,
                                color = Color(0xFF317A56),
                                label = "+${animatedTodayAddedHours.value.roundToInt()}",
                                labelColor = ProgressPositive
                            )
                        } else {
                            null
                        }
                    )
                    CurvedRingLabel(
                        text = stringResource(R.string.monthly_goal),
                        strokeWidth = circleStrokeWidth,
                        gap = 6.dp,
                        verticalOffset = 12.dp,
                        letterSpacing = 0.12f,
                        color = MaterialTheme.colorScheme.onBackground.toArgb(),
                        arcStartAngle = 200f,
                        arcSweepAngle = 140f,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Counter(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(circleSize * 0.48f),
                            time = timeSummary.accumulatedTime
                        )
                    }
                    val remainingHours = timeSummary.remainingHours
                    if (
                        (state.hasGoal == null || state.hasGoal) &&
                        (remainingHours == null || remainingHours > 0)
                    ) {
                        val remainingHoursTextAlpha by animateFloatAsState(
                            targetValue = if (remainingHours != null) 1f else 0f,
                            animationSpec = tween(400),
                            label = "remainingHoursTextAlpha"
                        )
                        val remainingHoursValue = remainingHours ?: 0
                        val text = if (remainingHoursValue > 0) {
                            pluralStringResource(
                                R.plurals.short_hours_remaining,
                                remainingHoursValue,
                                remainingHoursValue
                            )
                        } else {
                            pluralStringResource(R.plurals.short_hours_remaining, 0, 0)
                        }

                        CurvedRingLabel(
                            text = text,
                            strokeWidth = circleStrokeWidth,
                            gap = 6.dp,
                            verticalOffset = 0.dp,
                            letterSpacing = 0.12f,
                            color = ProgressPositive.toArgb(),
                            arcStartAngle = 160f,
                            arcSweepAngle = -140f,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(remainingHoursTextAlpha)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                GoalPillColumn(
                    credit = timeSummary.credit,
                    weeklyProgress = timeSummary.weeklyProgress,
                    weeklyTime = timeSummary.weeklyTime,
                    fieldServiceTime = timeSummary.fieldServiceTime,
                    animateCreditPill = animateCreditPill,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GoalPillColumn(
    credit: Time,
    weeklyProgress: Float,
    weeklyTime: Time,
    fieldServiceTime: Time,
    animateCreditPill: Boolean,
    modifier: Modifier = Modifier
) {
    val creditColor = EntryType.TheocraticAssignment.color()
    val fieldServiceColor = MaterialTheme.colorScheme.primary
    val weeklyColor = ProgressPositive

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val animatedWeeklyMinutes by animateIntAsState(
            targetValue = weeklyTime.toMinutes(),
            animationSpec = tween(400),
            label = "weeklyMinutes"
        )
        GoalPill(
            icon = painterResource(R.drawable.ic_today),
            title = stringResource(R.string.weekly_progress),
            value = stringResource(
                R.string.hours_short_unit,
                Time.fromMinutes(animatedWeeklyMinutes).formatForShortUnit()
            ),
            backgroundColor = weeklyColor.copy(alpha = 0.14f),
            contentColor = weeklyColor,
            progress = weeklyProgress,
            progressColor = weeklyColor.copy(alpha = 0.22f)
        )
        GoalPill(
            icon = painterResource(R.drawable.ic_work),
            title = stringResource(R.string.field_service_this_month),
            value = stringResource(R.string.hours_short_unit, fieldServiceTime.formatForShortUnit()),
            backgroundColor = fieldServiceColor.copy(alpha = 0.12f),
            contentColor = fieldServiceColor
        )
        AnimatedVisibility(
            visible = credit.isNotEmpty,
            enter = if (animateCreditPill) {
                expandVertically(animationSpec = tween(durationMillis = 180)) +
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 120,
                                delayMillis = 200
                            )
                        )
            } else {
                EnterTransition.None
            },
            exit = if (animateCreditPill) {
                fadeOut(animationSpec = tween(durationMillis = 120)) +
                        shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 180,
                                delayMillis = 140
                            )
                        )
            } else {
                ExitTransition.None
            }
        ) {
            GoalPill(
                icon = painterResource(R.drawable.ic_volunteer_activism),
                title = stringResource(R.string.credits),
                value = stringResource(R.string.hours_short_unit, credit.formatForShortUnit()),
                backgroundColor = creditColor.copy(alpha = 0.16f),
                contentColor = creditColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun GoalPill(
    icon: Painter,
    title: String,
    value: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    progressColor: Color = contentColor.copy(alpha = 0.2f)
) {
    val pillShape = RoundedCornerShape(21.dp)
    val iconShape = RoundedCornerShape(15.dp)
    val animatedProgress by animateFloatAsState(
        targetValue = progress?.coerceIn(0f, 1f) ?: 0f,
        animationSpec = tween(400),
        label = "pillProgress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(pillShape)
            .background(backgroundColor)
    ) {
        progress?.let {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(maxWidth * animatedProgress)
                    .background(progressColor)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .clip(iconShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = contentColor
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.84f)
                )
                Text(
                    value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

private fun Time.formatForShortUnit(): String = if (minutes == 0) {
    hours.toString()
} else {
    toString()
}

private fun Time.toMinutes(): Int = hours * 60 + minutes

private fun Time.Companion.fromMinutes(minutes: Int): Time {
    val coercedMinutes = minutes.coerceAtLeast(0)
    return Time(coercedMinutes / 60, coercedMinutes % 60)
}
