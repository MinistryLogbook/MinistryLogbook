package app.ministrylogbook.shared.layouts

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import app.ministrylogbook.R
import app.ministrylogbook.shared.utilities.condition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun MonthPickerMonth(text: String, selected: Boolean = false, disabled: Boolean = false, onClick: () -> Unit) {
    val selectedBackground = MaterialTheme.colorScheme.primary.copy(0.2f)
    val modifier = Modifier
        .clip(CircleShape)
        .condition(selected) {
            background(selectedBackground)
        }
        .condition(!disabled) {
            clickable(onClick = onClick)
        }
        .padding(16.dp, 6.dp)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CompositionLocalProvider(
            LocalContentColor provides if (disabled) {
                LocalContentColor.current.copy(0.4f)
            } else {
                LocalContentColor.current
            }
        ) {
            Text(text)
        }
    }
}

@Composable
fun MonthPickerDialog(
    isOpen: Boolean,
    initialMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    onDismissRequest: () -> Unit = {},
    onSelect: (month: LocalDate) -> Unit = {}
) {
    var selectedMonth by remember(initialMonth) { mutableStateOf(initialMonth) }

    AlertDialog(
        modifier = Modifier.width(240.dp),
        isOpen = isOpen,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onSelect(selectedMonth) }) {
                Text(stringResource(android.R.string.ok))
            }
        }
    ) {
        Box(Modifier.padding(horizontal = 24.dp)) {
            MonthPicker(selectedMonth, onSelect = { selectedMonth = it })
        }
    }
}

@Composable
fun MonthPicker(selectedMonth: LocalDate, onSelect: (month: LocalDate) -> Unit = {}) {
    val months = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    val actualDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val actualYear = actualDate.year
    var selectedYearIndex by remember(selectedMonth) {
        mutableIntStateOf(selectedMonth.year - actualYear)
    }
    val selectedYear = actualYear + selectedYearIndex
    val isActualMonth =
        selectedMonth.year == actualYear && selectedMonth.monthNumber == actualDate.monthNumber

    Column {
        YearPicker(selectedYear, onChange = {
            if (it < actualYear + 1) {
                selectedYearIndex = it - actualYear
            }
        })
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(3),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(months) { month ->
                val monthName = Month(month).getShortDisplayName()
                val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val currentMonth = currentDate.month.value
                val currentYear = actualYear + selectedYearIndex
                val disabled = (month > currentMonth && currentYear == actualYear) ||
                    currentYear > actualYear
                val selected = currentYear == selectedMonth.year &&
                    month == selectedMonth.monthNumber

                MonthPickerMonth(
                    monthName,
                    selected = selected,
                    disabled = disabled,
                    onClick = {
                        val newMonth = LocalDate(
                            currentYear,
                            month,
                            1
                        )
                        onSelect(newMonth)
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .condition(!isActualMonth) {
                    clickable {
                        val currentMonth = LocalDate(
                            actualYear,
                            actualDate.monthNumber,
                            1
                        )
                        onSelect(currentMonth)
                    }
                }
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            val contentColor = if (isActualMonth) {
                LocalContentColor.current.copy(0.4f)
            } else {
                LocalContentColor.current
            }
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Text(stringResource(R.string.current_month))
            }
        }
    }
}

@Composable
fun MonthPickerPopup(
    expanded: Boolean,
    selectedMonth: LocalDate,
    onDismissRequest: () -> Unit = {},
    onSelect: (month: LocalDate) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val expandedStates = remember { MutableTransitionState(false) }

    LaunchedEffect(expanded) {
        expandedStates.targetState = expanded
    }

    suspend fun handleDismissRequest() {
        delay(OUT_TRANSITION_DURATION.toLong())
        onDismissRequest()
    }

    if (expandedStates.currentState || expandedStates.targetState) {
        val density = LocalDensity.current
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val popupPositionProvider = MonthPickerPositionProvider(
            DpOffset(0.dp, 8.dp),
            density
        ) { parentBounds, menuBounds ->
            transformOriginState.value = calculateTransformOrigin(parentBounds, menuBounds)
        }

        Popup(
            popupPositionProvider = popupPositionProvider,
            properties = PopupProperties(focusable = true),
            onDismissRequest = {
                coroutineScope.launch {
                    handleDismissRequest()
                }
            }
        ) {
            MonthPickerPopupContent(
                expandedStates = expandedStates,
                transformOriginState = transformOriginState
            ) {
                MonthPicker(selectedMonth, onSelect)
            }
        }
    }
}

@Composable
private fun Month.getShortDisplayName() = when (this) {
    Month.JANUARY -> stringResource(R.string.january_short)
    Month.FEBRUARY -> stringResource(R.string.february_short)
    Month.MARCH -> stringResource(R.string.march_short)
    Month.APRIL -> stringResource(R.string.april_short)
    Month.MAY -> stringResource(R.string.may_short)
    Month.JUNE -> stringResource(R.string.june_short)
    Month.JULY -> stringResource(R.string.july_short)
    Month.AUGUST -> stringResource(R.string.august_short)
    Month.SEPTEMBER -> stringResource(R.string.september_short)
    Month.OCTOBER -> stringResource(R.string.october_short)
    Month.NOVEMBER -> stringResource(R.string.november_short)
    Month.DECEMBER -> stringResource(R.string.december_short)
}

@Composable
fun YearPicker(selectedYear: Int, onChange: (newYear: Int) -> Unit) {
    val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.ic_chevron_left),
            contentDescription = "Arrow left", // TODO: translation
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(100))
                .clickable {
                    onChange(selectedYear - 1)
                }
                .padding(12.dp)
        )

        Text(selectedYear.toString())

        val disabled = selectedYear >= currentYear
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(100))
                .condition(!disabled) {
                    clickable {
                        onChange(selectedYear + 1)
                    }
                }
                .padding(12.dp),
            contentDescription = "Arrow right",
            tint = if (disabled) LocalContentColor.current.copy(0.4f) else LocalContentColor.current
        )
    }
}

@Composable
fun MonthPickerPopupContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(expandedStates, "MonthPickerContent")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = IN_TRANSITION_DURATION,
                    easing = LinearOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = OUT_TRANSITION_DURATION,
                    easing = LinearOutSlowInEasing
                )
            }
        },
        label = ""
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0.8f
        }
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = IN_TRANSITION_DURATION)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OUT_TRANSITION_DURATION)
            }
        },
        label = ""
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                transformOrigin = transformOriginState.value
            }
            .width(240.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Box(Modifier.padding(10.dp)) {
            content()
        }
    }
}

internal data class MonthPickerPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // The min margin above and below the menu, relative to the screen.
        val verticalMargin = with(density) { 48.dp.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        // Compute horizontal position.
        val toRight = anchorBounds.left + contentOffsetX
        val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight,
                toLeft,
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else {
            sequenceOf(
                toLeft,
                toRight,
                // If the anchor gets outside of the window on the right, we want to position
                // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft

        // Compute vertical position.
        val toBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
        val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
            it >= verticalMargin &&
                it + popupContentSize.height <= windowSize.height - verticalMargin
        } ?: toTop

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
        )
        return IntOffset(x, y)
    }
}

internal fun calculateTransformOrigin(parentBounds: IntRect, menuBounds: IntRect): TransformOrigin {
    val pivotX = when {
        menuBounds.left >= parentBounds.right -> 0f
        menuBounds.right <= parentBounds.left -> 1f
        menuBounds.width == 0 -> 0f
        else -> {
            val intersectionCenter = (
                kotlin.math.max(
                    parentBounds.left,
                    menuBounds.left
                ) + kotlin.math.min(parentBounds.right, menuBounds.right)
                ) / 2
            (intersectionCenter - menuBounds.left).toFloat() / menuBounds.width
        }
    }
    val pivotY = when {
        menuBounds.top >= parentBounds.bottom -> 0f
        menuBounds.bottom <= parentBounds.top -> 1f
        menuBounds.height == 0 -> 0f
        else -> {
            val intersectionCenter = (
                kotlin.math.max(
                    parentBounds.top,
                    menuBounds.top
                ) + kotlin.math.min(parentBounds.bottom, menuBounds.bottom)
                ) / 2
            (intersectionCenter - menuBounds.top).toFloat() / menuBounds.height
        }
    }
    return TransformOrigin(pivotX, pivotY)
}

// Menu open/close animation.
internal const val IN_TRANSITION_DURATION = 120
internal const val OUT_TRANSITION_DURATION = 80
