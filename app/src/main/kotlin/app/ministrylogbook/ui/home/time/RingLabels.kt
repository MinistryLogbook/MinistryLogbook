package app.ministrylogbook.ui.home.time

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import app.ministrylogbook.R

@Composable
fun CurvedRingLabel(
    modifier: Modifier = Modifier,
    text: String,
    strokeWidth: Dp,
    gap: Dp,
    verticalOffset: Dp,
    letterSpacing: Float = 0.12f,
    color: Int,
    arcStartAngle: Float,
    arcSweepAngle: Float
) {
    val context = LocalContext.current
    val typeface = ResourcesCompat.getFont(context, R.font.google_sans_flex_rounded_medium)

    Canvas(modifier) {
        val strokeWidthPx = strokeWidth.toPx()
        val gapPx = gap.toPx()
        val radius = size.minDimension / 2f - strokeWidthPx - gapPx
        val center = size.minDimension / 2f
        val oval = RectF(
            center - radius,
            center - radius,
            center + radius,
            center + radius
        )
        val path = Path().apply {
            addArc(oval, arcStartAngle, arcSweepAngle)
        }
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 13.sp.toPx()
            this.color = color
            this.typeface = typeface
            textAlign = Paint.Align.LEFT
            this.letterSpacing = letterSpacing
            textScaleX = 1f
            isFakeBoldText = true
        }
        val pathLength = PathMeasure(path, false).length
        val textWidth = paint.measureText(text)
        val hOffset = ((pathLength - textWidth) / 2f).coerceAtLeast(0f)

        drawIntoCanvas {
            it.nativeCanvas.drawTextOnPath(text, path, hOffset, verticalOffset.toPx(), paint)
        }
    }
}
