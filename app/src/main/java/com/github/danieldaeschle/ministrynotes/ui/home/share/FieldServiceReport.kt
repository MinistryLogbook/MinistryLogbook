package com.github.danieldaeschle.ministrynotes.ui.home.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.github.danieldaeschle.ministrynotes.R


data class FieldServiceReport(
    val name: String,
    val month: String,
    val placements: Int,
    val videoShowings: Int,
    val hours: Int,
    val returnVisits: Int,
    val bibleStudies: Int,
    val comments: String,
)

fun Context.createFieldServiceReportImage(report: FieldServiceReport): Bitmap {
    val width = 1000
    val padding = 40f
    // label row starting point
    val textLabelX = 70f
    val tableDividerX = 860f
    val textValueX = 880f

    val dottedLinePaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(5f, 3f), 0f)
    }
    val labelPaint = Paint().apply {
        color = Color.BLACK
        textSize = 35f
    }
    val valueTypeface = resources.getFont(R.font.caveat_variable)
    val valuePaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 38f
        typeface = Typeface.create(valueTypeface, Typeface.NORMAL)
    }

    // title
    val title = getString(R.string.field_service_report).uppercase()
    val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 45f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val titleBounds = Rect()
    titlePaint.getTextBounds(title, 0, title.length, titleBounds)

    // name and month
    val nameMonthPaint = Paint().apply {
        color = Color.BLACK
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val nameLabel = getString(R.string.name_colon)
    val monthLabel = getString(R.string.month_colon)
    val nameLabelMetrics = Rect()
    nameMonthPaint.getTextBounds(nameLabel, 0, nameLabel.length, nameLabelMetrics)
    val monthLabelMetrics = Rect()
    nameMonthPaint.getTextBounds(monthLabel, 0, monthLabel.length, monthLabelMetrics)

    // table
    val rowHeight = 50f
    val tablePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val tableTop = 260f
    val tableBottom = tableTop + 5 * rowHeight

    // placements
    val placementsLabel = getString(R.string.placements_long_colon)
    val placementsLineY = tableTop + rowHeight

    // video showings
    val videoShowingsLabel = getString(R.string.video_showings)
    val videoShowingsLineY = placementsLineY + rowHeight

    // hours
    val hoursLabel = getString(R.string.hours)
    val hoursLineY = videoShowingsLineY + rowHeight

    // return visits
    val returnVisitsLabel = getString(R.string.return_visits)
    val returnVisitsLineY = hoursLineY + rowHeight

    // bible studies
    val bibleStudiesLabel = getString(R.string.bible_studies_long)

    // comments
    val commentsTop = tableBottom + padding
    val commentsMinHeight = 120f
    val commentsLabel = getString(R.string.comments_colon)
    val commentsLabelLineHeight = 36f
    val commentStaticLayout =
        StaticLayout.Builder.obtain(
            report.comments,
            0,
            report.comments.length,
            valuePaint,
            width - 2 * textLabelX.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 0.8f)
            .setIncludePad(true)
            .build()
    val commentsBottom =
        commentsTop + maxOf(
            commentsMinHeight,
            commentsLabelLineHeight + 10 + commentStaticLayout.height
        )

    val height = commentsBottom + padding
    val bitmap = Bitmap.createBitmap(width, height.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    with(canvas) {
        // background
        drawRect(0f, 0f, width.toFloat(), height, Paint().apply {
            color = Color.WHITE
        })

        // title
        drawText(
            title,
            ((width - titleBounds.width()) / 2).toFloat(),
            padding + titleBounds.height(),
            titlePaint,
        )

        // name and month
        drawText(nameLabel, textLabelX, 160f, nameMonthPaint)
        drawText(report.name, textLabelX + nameLabelMetrics.width() + 40, 160f, valuePaint)
        drawLine(
            textLabelX + nameLabelMetrics.width() + 20,
            165f,
            width - textLabelX,
            160f,
            dottedLinePaint
        )
        drawText(monthLabel, textLabelX, 210f, nameMonthPaint)
        drawText(report.month, textLabelX + monthLabelMetrics.width() + 40, 210f, valuePaint)
        drawLine(
            textLabelX + monthLabelMetrics.width() + 20,
            215f,
            width - textLabelX,
            210f,
            dottedLinePaint
        )

        // table
        drawRect(padding, tableTop, width - padding, tableBottom, tablePaint)
        drawLine(tableDividerX, tableTop, tableDividerX, tableBottom, tablePaint)

        // placements
        drawText(
            placementsLabel,
            textLabelX,
            tableTop + 36f,
            labelPaint,
        )
        drawText(
            report.placements.toString(),
            textValueX,
            tableTop + 36f,
            valuePaint,
        )
        drawLine(
            padding,
            placementsLineY,
            width - padding,
            placementsLineY,
            tablePaint
        )

        // Video showings
        drawText(
            videoShowingsLabel,
            textLabelX,
            placementsLineY + 34f,
            labelPaint,
        )
        drawText(
            report.videoShowings.toString(),
            textValueX,
            placementsLineY + 36f,
            valuePaint,
        )
        drawLine(padding, videoShowingsLineY, width - padding, videoShowingsLineY, tablePaint)

        // hours
        drawText(
            hoursLabel,
            textLabelX,
            videoShowingsLineY + 36f,
            labelPaint,
        )
        drawText(
            report.hours.toString(),
            textValueX,
            videoShowingsLineY + 36f,
            valuePaint,
        )
        drawLine(padding, hoursLineY, width - padding, hoursLineY, tablePaint)

        // Return visits
        drawText(
            returnVisitsLabel,
            textLabelX,
            hoursLineY + 36f,
            labelPaint,
        )
        drawText(
            report.returnVisits.toString(),
            textValueX,
            hoursLineY + 36f,
            valuePaint,
        )
        drawLine(padding, returnVisitsLineY, width - padding, returnVisitsLineY, tablePaint)

        // Bible studies
        drawText(
            bibleStudiesLabel,
            textLabelX,
            returnVisitsLineY + 36f,
            labelPaint,
        )
        drawText(
            report.bibleStudies.toString(),
            textValueX,
            returnVisitsLineY + 36f,
            valuePaint,
        )

        // Comments
        drawRect(padding, commentsTop, width - padding, commentsBottom, tablePaint)
        drawText(
            commentsLabel,
            textLabelX,
            commentsTop + commentsLabelLineHeight,
            labelPaint,
        )
        canvas.translate(textLabelX, commentsTop + 5 + commentsLabelLineHeight)
        commentStaticLayout.draw(this)
    }

    return bitmap
}
