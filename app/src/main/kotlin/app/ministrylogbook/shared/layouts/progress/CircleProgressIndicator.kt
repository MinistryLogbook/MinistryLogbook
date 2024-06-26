package app.ministrylogbook.shared.layouts.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ministrylogbook.ui.theme.ProgressNegative
import app.ministrylogbook.ui.theme.ProgressPositive

@Composable
fun CircleProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    animationSpec: AnimationSpec<Float> = tween(400),
    progresses: List<ProgressKind>,
    baseLineColor: Color
) {
    val initialValue = 0.0001f

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (size.width - strokeWidthPx) / 2
            drawCircle(
                baseLineColor,
                radius = radius,
                center = Offset(radius + strokeWidthPx / 2, radius + strokeWidthPx / 2),
                style = Stroke(width = strokeWidthPx)
            )
        }

        progresses.filterIsInstance<ProgressKind.Progress>().forEach { progress ->
            val animatable = remember { Animatable(initialValue) }

            LaunchedEffect(progress.percent) {
                val finalPercent = progress.percent.coerceIn(0f, 1f)
                animatable.animateTo(finalPercent, animationSpec = animationSpec)
            }

            Canvas(Modifier.fillMaxSize()) {
                val strokeWidthPx = strokeWidth.toPx()
                val radius = (size.width - strokeWidthPx) / 2
                drawArc(
                    progress.color,
                    startAngle = -90f,
                    sweepAngle = animatable.value * 360f,
                    useCenter = false,
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }
    }
}

@Preview
@Composable
fun CircleProgressIndicatorPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .height(200.dp)
                .width(200.dp)
        ) {
            CircleProgressIndicator(
                modifier = Modifier.size(200.dp, 200.dp),
                baseLineColor = ProgressPositive,
                progresses = listOf(
                    ProgressKind.Progress(percent = .6f, color = ProgressNegative),
                    ProgressKind.Progress(percent = .45f, color = ProgressPositive)
                )
            )
        }
    }
}
