package app.ministrylogbook.ui.share

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
import app.ministrylogbook.R

data class FieldServiceReport(
    val name: String = "",
    val month: String = "",
    val hours: Int = 0,
    val bibleStudies: Int = 0,
    val comments: String = ""
) {
    fun toText(context: Context): String {
        val nameLine = if (name.isNotBlank()) "|${context.getString(R.string.name_colon)} ${name}\n" else ""
        val checkbox = if (hours > 0) {
            "☑"
        } else {
            "☐"
        }

        val text = """${context.getString(R.string.field_service_report).uppercase()}
        |
        $nameLine|${context.getString(R.string.month_colon)} $month
        |
        |${context.getString(R.string.check_box_form_of_ministry_colon)} $checkbox
        |${context.getString(R.string.bible_studies_long_colon)} $bibleStudies
        |${context.getString(R.string.hours_with_description_colon)} $hours"""

        val commentsSection = """
        |
        |${context.getString(R.string.comments_colon)}
        |$comments"""

        return if (comments.isNotBlank()) {
            text + commentsSection
        } else {
            text
        }.trimMargin()
    }
}

fun Context.createFieldServiceReportImage(report: FieldServiceReport): Bitmap {
    val width = 1000
    val padding = 40f
    // label row starting point
    val textLabelX = 55f
    val tableDividerX = 840f
    val textValueX = tableDividerX + 20

    val dottedLinePaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(5f, 3f), 0f)
    }
    val labelPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 35f
    }
    val valueTypeface = resources.getFont(R.font.caveat_variable)
    val bigValuePaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 55f
        typeface = Typeface.create(valueTypeface, Typeface.NORMAL)
    }
    val smallValuePaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 45f
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
    val tablePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val tableTop = 260f

    // checkbox
    val checkboxLabel = getString(R.string.check_box_form_of_ministry)
    val checkboxLabelStaticLayout =
        StaticLayout.Builder
            .obtain(
                checkboxLabel,
                0,
                checkboxLabel.length,
                labelPaint,
                tableDividerX.toInt() - textLabelX.toInt() - 150,
            )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(true)
            .build()
    val checkboxLineY = tableTop + checkboxLabelStaticLayout.height + 10

    // bible studies
    val bibleStudiesLabel = getString(R.string.bible_studies_long)
    val bibleStudiesLabelStaticLayout =
        StaticLayout.Builder
            .obtain(
                bibleStudiesLabel,
                0,
                bibleStudiesLabel.length,
                labelPaint,
                tableDividerX.toInt() - textLabelX.toInt() - 80,
            )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(true)
            .build()
    val bibleStudiesLineY = checkboxLineY + bibleStudiesLabelStaticLayout.height + 10

    // hours
    val hoursLabel = getString(R.string.hours_long)
    val hoursLabelStaticLayout =
        StaticLayout.Builder
            .obtain(
                hoursLabel,
                0,
                hoursLabel.length,
                labelPaint,
                tableDividerX.toInt() - textLabelX.toInt() - 80,
            )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(true)
            .build()

    val tableBottom = bibleStudiesLineY + hoursLabelStaticLayout.height + 15

    // comments
    val commentsTop = tableBottom + padding
    val commentsMinHeight = 120f
    val commentsLabel = getString(R.string.comments_colon)
    val commentsLabelLineHeight = 36f
    val commentsStaticLayout =
        StaticLayout.Builder
            .obtain(
                report.comments,
                0,
                report.comments.length,
                smallValuePaint,
                width - 2 * textLabelX.toInt()
            )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 0.8f)
            .setIncludePad(true)
            .build()
    val commentsBottom =
        commentsTop + maxOf(
            commentsMinHeight,
            commentsLabelLineHeight + 10 + commentsStaticLayout.height
        )

    val height = commentsBottom + padding
    val bitmap = Bitmap.createBitmap(width, height.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    with(canvas) {
        // background
        drawRect(
            0f,
            0f,
            width.toFloat(),
            height,
            Paint().apply {
                color = Color.WHITE
            }
        )

        // title
        drawText(
            title,
            ((width - titleBounds.width()) / 2).toFloat(),
            padding + titleBounds.height(),
            titlePaint
        )

        // name and month
        drawText(nameLabel, textLabelX, 160f, nameMonthPaint)
        drawText(report.name, textLabelX + nameLabelMetrics.width() + 40, 160f, smallValuePaint)
        drawLine(
            textLabelX + nameLabelMetrics.width() + 20,
            165f,
            width - padding,
            160f,
            dottedLinePaint
        )
        drawText(monthLabel, textLabelX, 210f, nameMonthPaint)
        drawText(report.month, textLabelX + monthLabelMetrics.width() + 40, 210f, smallValuePaint)
        drawLine(
            textLabelX + monthLabelMetrics.width() + 20,
            215f,
            width - padding,
            210f,
            dottedLinePaint
        )

        // table
        drawRect(padding, tableTop, width - padding, tableBottom, tablePaint)
        drawLine(tableDividerX, checkboxLineY, tableDividerX, tableBottom, tablePaint)

        // checkbox
        canvas.save()
        canvas.translate(textLabelX, tableTop + 5)
        checkboxLabelStaticLayout.draw(this)
        canvas.restore()
        val checkboxSize = 45f
        val top = tableTop + (checkboxLineY - tableTop - checkboxSize) / 2
        val left = width - padding * 2 - checkboxSize
        val right = width - padding * 2
        val bottom = top + checkboxSize
        drawRect(left, top, right, bottom, tablePaint)
        if (report.hours > 0) {
            drawText("✓", left + 5, bottom - 7, smallValuePaint)
        }
        drawLine(
            padding,
            checkboxLineY,
            width - padding,
            checkboxLineY,
            tablePaint
        )

        // Bible studies
        canvas.save()
        canvas.translate(textLabelX, checkboxLineY + 5)
        bibleStudiesLabelStaticLayout.draw(this)
        canvas.restore()
        drawText(
            report.bibleStudies.toString(),
            textValueX,
            checkboxLineY + 45,
            bigValuePaint
        )
        drawLine(padding, bibleStudiesLineY, width - padding, bibleStudiesLineY, tablePaint)

        // hours
        canvas.save()
        canvas.translate(textLabelX, bibleStudiesLineY + 5)
        hoursLabelStaticLayout.draw(this)
        canvas.restore()
        drawText(
            report.hours.toString(),
            textValueX,
            bibleStudiesLineY + 60,
            bigValuePaint
        )

        // Comments
        drawRect(padding, commentsTop, width - padding, commentsBottom, tablePaint)
        drawText(
            commentsLabel,
            textLabelX,
            commentsTop + commentsLabelLineHeight,
            labelPaint
        )
        canvas.save()
        canvas.translate(textLabelX, commentsTop + 5 + commentsLabelLineHeight)
        commentsStaticLayout.draw(this)
        canvas.restore()
    }

    return bitmap
}
