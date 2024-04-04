package app.ministrylogbook.shared.layouts.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun LinearProgressIndicator(
    progresses: List<ProgressKind>,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap
) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = size.height
            drawLinearIndicator(0f, 1f, trackColor, strokeWidth, strokeCap)
        }
        progresses.filterIsInstance<ProgressKind.Progress>().forEach { progress ->
            val animatedProgress = remember { Animatable(0f) }

            LaunchedEffect(progress) {
                val coercedProgress = progress.percent.coerceIn(0f, 1f)
                animatedProgress.animateTo(coercedProgress, animationSpec = SpringSpec())
            }
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidth = size.height
                drawLinearIndicator(0f, animatedProgress.value, progress.color, strokeWidth, strokeCap)
            }
        }
        progresses.filterIsInstance<ProgressKind.Indicator>().forEach { progress ->
            val animatedIndicator = remember { Animatable(0f) }
            val coercedProgress = progress.percent.coerceIn(0f, 1f)

            LaunchedEffect(progress) {
                animatedIndicator.animateTo(1f, animationSpec = TweenSpec(delay = 600))
            }
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val x = (size.width - strokeWidth) * coercedProgress
                drawLine(
                    progress.color.copy(alpha = animatedIndicator.value),
                    Offset(x, -2.dp.toPx()),
                    Offset(x, size.height + 2.dp.toPx()),
                    strokeWidth,
                    StrokeCap.Round
                )
            }
        }
    }
}

private fun DrawScope.drawLinearIndicator(
    startFraction: Float,
    endFraction: Float,
    color: Color,
    strokeWidth: Float,
    strokeCap: StrokeCap
) {
    val width = size.width
    val height = size.height
    // Start drawing from the vertical center of the stroke
    val yOffset = height / 2

    val isLtr = layoutDirection == LayoutDirection.Ltr
    val barStart = (if (isLtr) startFraction else 1f - endFraction) * width
    val barEnd = (if (isLtr) endFraction else 1f - startFraction) * width

    // if there isn't enough space to draw the stroke caps, fall back to StrokeCap.Butt
    if (strokeCap == StrokeCap.Butt || height > width) {
        // Progress line
        drawLine(color, Offset(barStart, yOffset), Offset(barEnd, yOffset), strokeWidth)
    } else {
        // need to adjust barStart and barEnd for the stroke caps
        val strokeCapOffset = strokeWidth / 2
        val coerceRange = strokeCapOffset..(width - strokeCapOffset)
        val adjustedBarStart = barStart.coerceIn(coerceRange)
        val adjustedBarEnd = barEnd.coerceIn(coerceRange)

        if (abs(endFraction - startFraction) > 0) {
            // Progress line
            drawLine(
                color,
                Offset(adjustedBarStart, yOffset),
                Offset(adjustedBarEnd, yOffset),
                strokeWidth,
                strokeCap
            )
        }
    }
}
