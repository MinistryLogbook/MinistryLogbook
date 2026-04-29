package app.ministrylogbook.shared.layouts.progress

import android.graphics.Paint
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.ui.theme.ProgressNegative
import app.ministrylogbook.ui.theme.ProgressPositive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class CircleProgressSegment(
    val startPercent: Float = 0f,
    val endPercent: Float = 0f,
    val color: Color,
    val label: String? = null,
    val labelColor: Color = color
)

@Composable
fun CircleProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    animationSpec: AnimationSpec<Float> = tween(400),
    progress: ProgressKind.Progress?,
    segment: CircleProgressSegment? = null,
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

        val progressAnimatable = remember { Animatable(initialValue) }

        LaunchedEffect(progress?.percent) {
            val finalPercent = progress?.percent?.coerceAtLeast(0f) ?: initialValue
            progressAnimatable.animateTo(finalPercent, animationSpec = animationSpec)
        }

        progress?.let { progress ->
            val separatorColor = baseLineColor.compositeOver(MaterialTheme.colorScheme.background)
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidthPx = strokeWidth.toPx()
                val radius = (size.width - strokeWidthPx) / 2
                val percent = progressAnimatable.value
                val overflowPercent = percent % 1f
                val sweepPercent = if (percent > 1f && overflowPercent > 0f) {
                    overflowPercent
                } else if (percent < 1f) {
                    percent
                } else {
                    0f
                }
                if (percent >= 1f) {
                    drawCircle(
                        progress.color,
                        radius = radius,
                        center = Offset(radius + strokeWidthPx / 2, radius + strokeWidthPx / 2),
                        style = Stroke(width = strokeWidthPx)
                    )
                }
                if (sweepPercent > 0f) {
                    drawArc(
                        progress.color,
                        startAngle = -90f,
                        sweepAngle = sweepPercent * 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2)
                    )
                }
                val markerEndPercent = when {
                    percent > 1f && overflowPercent > 0f -> overflowPercent
                    percent >= 1f -> 1f
                    else -> percent
                }

                if (markerEndPercent > 0f) {
                    drawEndMarker(
                        markerEndPercent = markerEndPercent,
                        radius = radius,
                        strokeWidthPx = strokeWidthPx,
                        separatorColor = separatorColor,
                        progressColor = progress.color
                    )
                }
            }
        }

        segment?.let { segment ->
            val animatableStart = remember { Animatable(initialValue) }
            val animatableEnd = remember { Animatable(initialValue) }

            LaunchedEffect(segment.startPercent) {
                animatableStart.animateTo(segment.startPercent.coerceAtLeast(0f), animationSpec = animationSpec)
            }
            LaunchedEffect(segment.endPercent) {
                animatableEnd.animateTo(segment.endPercent.coerceAtLeast(0f), animationSpec = animationSpec)
            }

            Canvas(Modifier.fillMaxSize()) {
                val strokeWidthPx = strokeWidth.toPx()
                val segmentPadding = 3.dp.toPx()
                val segmentStrokeWidth = strokeWidthPx - segmentPadding * 2
                val inset = strokeWidthPx / 2
                val segmentSize = size.width - inset * 2
                val startPercent = animatableStart.value
                val endPercent = animatableEnd.value.coerceAtLeast(startPercent)
                val progressEndPercent = progress
                    ?.percent
                    ?.coerceAtLeast(0f)
                    ?.markerEndPercent()
                    ?: 0f
                val roundedCapPercent = (segmentStrokeWidth / 2f).toSweepPercent(radius = segmentSize / 2f)
                val overlapsTinyProgress = progressEndPercent > 0f &&
                        progressEndPercent <= roundedCapPercent &&
                        startPercent <= progressEndPercent + roundedCapPercent
                val sweepPercent = (endPercent - startPercent).coerceAtLeast(0f).coerceAtMost(1f)

                if (sweepPercent > 0f && !overlapsTinyProgress) {
                    drawArc(
                        segment.color,
                        startAngle = -90f + startPercent * 360f,
                        sweepAngle = sweepPercent * 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        style = Stroke(width = segmentStrokeWidth, cap = StrokeCap.Round),
                        size = Size(segmentSize, segmentSize)
                    )
                    segment.label?.let { label ->
                        drawSegmentLabel(
                            label = label,
                            labelColor = segment.labelColor,
                            startPercent = startPercent,
                            sweepPercent = sweepPercent,
                            radius = segmentSize / 2f,
                            center = Offset(size.width / 2f, size.height / 2f),
                            segmentStrokeWidth = segmentStrokeWidth
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSegmentLabel(
    label: String,
    labelColor: Color,
    startPercent: Float,
    sweepPercent: Float,
    radius: Float,
    center: Offset,
    segmentStrokeWidth: Float
) {
    if (label.isBlank() || sweepPercent <= 0f || radius <= 0f) return

    val paint = Paint().apply {
        isAntiAlias = true
        textSize = 12.sp.toPx()
        color = labelColor.toArgb()
        textAlign = Paint.Align.LEFT
        textScaleX = 1.08f
        isFakeBoldText = true
    }
    val textWidth = paint.measureText(label)
    val sweepRadians = sweepPercent * 2f * PI.toFloat()
    val arcLength = radius * sweepRadians
    val capExtension = segmentStrokeWidth / 2f
    val padding = 6.dp.toPx()
    val usableStart = -capExtension + padding
    val usableEnd = arcLength + capExtension - padding
    val desiredCenterLength = usableEnd - textWidth / 2f
    val minCenterLength = usableStart + textWidth / 2f
    val maxCenterLength = usableEnd - textWidth / 2f
    val centerLength = if (minCenterLength <= maxCenterLength) {
        desiredCenterLength.coerceIn(minCenterLength, maxCenterLength)
    } else {
        (usableStart + usableEnd) / 2f
    }
    val centerPercentThroughSweep = if (arcLength > 0f) {
        (centerLength / arcLength).coerceIn(0f, 1f)
    } else {
        1f
    }
    val labelAngle = -90f + (startPercent + sweepPercent * centerPercentThroughSweep) * 360f
    val labelAngleRadians = labelAngle / 180f * PI.toFloat()
    val labelCenter = Offset(
        x = center.x + cos(labelAngleRadians) * radius,
        y = center.y + sin(labelAngleRadians) * radius
    )
    val baselineOffset = -(paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f

    drawIntoCanvas {
        it.nativeCanvas.save()
        it.nativeCanvas.rotate(labelAngle + 90f, labelCenter.x, labelCenter.y)
        it.nativeCanvas.drawText(
            label,
            labelCenter.x - textWidth / 2f,
            labelCenter.y + baselineOffset,
            paint
        )
        it.nativeCanvas.restore()
    }
}

private fun DrawScope.drawEndMarker(
    markerEndPercent: Float,
    radius: Float,
    strokeWidthPx: Float,
    separatorColor: Color,
    progressColor: Color
) {
    if (markerEndPercent <= 0f) return

    val center = Offset(size.width / 2, size.height / 2)
    val markerStroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    val ringClipPath = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(
            Rect(
                center = center,
                radius = radius + strokeWidthPx / 2
            )
        )
        addOval(
            Rect(
                center = center,
                radius = radius - strokeWidthPx / 2
            )
        )
    }
    val markerAngle = -90f + markerEndPercent * 360f
    val markerSweep = strokeWidthPx
        .toSweepDegrees(radius)
        .coerceAtMost(markerEndPercent * 360f)
    val markerAngleRadians = markerAngle / 180f * PI.toFloat()
    val markerCenter = Offset(
        x = center.x + cos(markerAngleRadians) * radius,
        y = center.y + sin(markerAngleRadians) * radius
    )
    val startClipSweep = markerEndPercent * 360f + 180f
    val startClipPath = if (startClipSweep >= 360f) {
        Path().apply {
            addRect(Rect(Offset.Zero, size))
        }
    } else {
        Path().apply {
            moveTo(center.x, center.y)
            arcTo(
                rect = Rect(
                    center = center,
                    radius = radius + strokeWidthPx / 2 + 2.dp.toPx()
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = startClipSweep,
                forceMoveTo = false
            )
            close()
        }
    }

    clipPath(ringClipPath) {
        clipPath(startClipPath) {
            drawCircle(
                separatorColor,
                radius = strokeWidthPx / 2 + 2.dp.toPx(),
                center = markerCenter
            )
        }
        drawArc(
            progressColor,
            startAngle = markerAngle - markerSweep,
            sweepAngle = markerSweep,
            useCenter = false,
            topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
            style = markerStroke,
            size = Size(radius * 2, radius * 2)
        )
    }
}

private fun Float.toSweepDegrees(radius: Float): Float {
    val circumference = 2f * PI.toFloat() * radius
    return if (circumference > 0f) this / circumference * 360f else 0f
}

private fun Float.toSweepPercent(radius: Float): Float {
    val circumference = 2f * PI.toFloat() * radius
    return if (circumference > 0f) this / circumference else 0f
}

private fun Float.markerEndPercent(): Float {
    val overflowPercent = this % 1f
    return when {
        this > 1f && overflowPercent > 0f -> overflowPercent
        this >= 1f -> 1f
        else -> this
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
                baseLineColor = Color.LightGray,
                strokeWidth = 20.dp,
                progress = ProgressKind.Progress(percent = .6f, color = ProgressNegative),
                segment = CircleProgressSegment(
                    startPercent = .4f,
                    endPercent = .6f,
                    color = ProgressPositive,
                    label = "+1"
                )
            )
        }
    }
}
