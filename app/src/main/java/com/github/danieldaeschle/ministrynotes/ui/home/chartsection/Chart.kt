package com.github.danieldaeschle.ministrynotes.ui.home.chartsection

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.geometry.Rect
import com.github.danieldaeschle.ministrynotes.R


data class Bar(val value: Float, val name: String, val color: Color, val width: Dp = 10.dp)

enum class VerticalMarkerStyle {
    Line, Dashed,
}

data class VerticalMarker(
    val value: Float,
    val color: Color,
    val style: VerticalMarkerStyle = VerticalMarkerStyle.Line,
    val width: Dp = 1.dp,
)

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    bars: Array<Bar>,
    horizontalPadding: Dp = 0.dp,
    markers: Array<VerticalMarker> = arrayOf(),
    animationSpec: AnimationSpec<Float> = tween(400),
) {
    val progress = remember { Animatable(0f) }
    val context = LocalContext.current
    val jostTypeface = ResourcesCompat.getFont(context, R.font.jost_variable)
    val cornerRadius = CornerRadius(100f, 100f)
    val lineColor = MaterialTheme.colorScheme.onBackground.copy(0.4f)

    LaunchedEffect(bars) {
        progress.animateTo(1f, animationSpec = animationSpec)
    }

    Canvas(modifier = modifier, onDraw = {
        val maxValue = bars.maxOf { it.value }
        val width = if (markers.isEmpty()) size.width
        else size.width - 2 * horizontalPadding.toPx() - 16.dp.toPx()

        markers.forEach { marker ->
            val markerHeight = size.height - size.height / maxValue * marker.value
            val pathEffect = when (marker.style) {
                VerticalMarkerStyle.Dashed -> PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                VerticalMarkerStyle.Line -> null
            }
            drawLine(
                color = lineColor,
                strokeWidth = marker.width.toPx(),
                pathEffect = pathEffect,
                start = Offset(x = 0f, y = markerHeight),
                end = Offset(x = size.width, y = markerHeight)
            )
            val textPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = 9.sp.toPx()
                color = marker.color.toArgb()
                typeface = jostTypeface
            }
            val bounds = android.graphics.Rect()
            val text = if (marker.value % 1.0 == 0.0) marker.value.toInt().toString()
            else "%.1f".format(marker.value)
            textPaint.getTextBounds(
                text, 0, text.length, bounds
            )
            val beginX = size.width - bounds.width() - 16.dp.toPx()
            val beginY = markerHeight - 4.dp.toPx()
            drawIntoCanvas {
                it.nativeCanvas.drawText(text, beginX, beginY, textPaint)
            }
        }
        bars.forEachIndexed { i, bar ->
            val barHeight = size.height / maxValue * bar.value * progress.value
            val columnWidth = width / bars.size
            val offsetX =
                horizontalPadding.toPx() + i * columnWidth + columnWidth / 2 - bar.width.toPx() / 2
            val offsetY = size.height - barHeight
            drawPath(
                path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                offset = Offset(offsetX, offsetY),
                                size = Size(bar.width.toPx(), barHeight),
                            ),
                            cornerRadius = cornerRadius,
                        )
                    )
                },
                color = bar.color,
            )
        }
    })
    Column(
        modifier = Modifier.padding(
            top = 4.dp,
            start = horizontalPadding,
            end = horizontalPadding + 16.dp
        )
    ) {
        Row {
            bars.forEach {
                Text(
                    modifier = Modifier.weight(1f),
                    text = it.name,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}