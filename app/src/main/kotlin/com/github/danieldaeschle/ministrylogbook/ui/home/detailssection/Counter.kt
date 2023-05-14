package com.github.danieldaeschle.ministrylogbook.ui.home.detailssection

import android.graphics.Rect
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.github.danieldaeschle.ministrylogbook.R
import com.github.danieldaeschle.ministrylogbook.lib.Time
import com.github.danieldaeschle.ministrylogbook.ui.theme.MinistryLogbookTheme

@Composable
fun Counter(
    modifier: Modifier = Modifier,
    time: Time,
) {
    val context = LocalContext.current
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val onBackgroundColor80 = MaterialTheme.colorScheme.onBackground.copy(0.8f).toArgb()
    val onBackgroundColor60 = MaterialTheme.colorScheme.onBackground.copy(0.6f).toArgb()
    val jostTypeface = ResourcesCompat.getFont(context, R.font.jost_variable)
    val animatedHours by animateIntAsState(targetValue = time.hours, label = "hours")
    val animatedMinutes by animateIntAsState(targetValue = time.minutes, label = "minutes")
    val hrsText = stringResource(R.string.hours_short)
    val minText = stringResource(R.string.minutes_short)

    Canvas(modifier = modifier, onDraw = {
        val bigTextPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 60.sp.toPx()
            color = onBackgroundColor
            typeface = jostTypeface
        }
        val smallTextPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 28.sp.toPx()
            color = onBackgroundColor80
            typeface = jostTypeface
        }
        val unitTextPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 16.sp.toPx()
            color = onBackgroundColor60
            typeface = jostTypeface
        }
        val hoursBounds = Rect()
        val animatedHoursWithSign =
            if (time.isNegative) "-$animatedHours" else animatedHours.toString()
        bigTextPaint.getTextBounds(
            animatedHoursWithSign, 0, animatedHoursWithSign.length, hoursBounds
        )
        val minutesBounds = Rect()
        smallTextPaint.getTextBounds(
            animatedMinutes.toString(), 0, animatedMinutes.toString().length, minutesBounds
        )
        val hrsUnitBounds = Rect()
        unitTextPaint.getTextBounds(hrsText, 0, 3, hrsUnitBounds)
        val minUnitBounds = Rect()
        unitTextPaint.getTextBounds(minText, 0, 3, minUnitBounds)
        val gap = 8.dp.toPx()
        val drawWidth = hoursBounds.width() + gap + minUnitBounds.width()
        val drawHeight = hoursBounds.height() + gap + minutesBounds.height()
        val beginHoursY = (size.height - drawHeight) / 2 + hoursBounds.height()
        val beginHoursX = (size.width - drawWidth) / 2
        val beginMinutesX = beginHoursX + hoursBounds.width() - minutesBounds.width()
        val beginMinutesY = beginHoursY + gap + minutesBounds.height()
        val beginUnitX = beginHoursX + hoursBounds.width().toFloat() + gap
        val beginMinY = beginHoursY + gap + minUnitBounds.height()

        drawIntoCanvas {
            it.nativeCanvas.drawText(
                animatedHoursWithSign, beginHoursX, beginHoursY, bigTextPaint
            )
            it.nativeCanvas.drawText(
                animatedMinutes.toString(), beginMinutesX, beginMinutesY, smallTextPaint
            )
            it.nativeCanvas.drawText(hrsText, beginUnitX, beginHoursY, unitTextPaint)
            it.nativeCanvas.drawText(minText, beginUnitX, beginMinY, unitTextPaint)
        }
    })
}

@Preview(showBackground = true)
@Composable
fun CounterPreview() {
    MinistryLogbookTheme {
        Counter(
            modifier = Modifier
                .height(120.dp)
                .width(120.dp),
            time = Time(1, 30),
        )
    }
}
