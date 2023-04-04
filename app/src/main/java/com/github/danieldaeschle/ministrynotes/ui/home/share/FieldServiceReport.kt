package com.github.danieldaeschle.ministrynotes.ui.home.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.danieldaeschle.ministrynotes.ui.theme.Caveat

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

// TODO: rewrite without canvas; pass data
@OptIn(ExperimentalTextApi::class)
@Composable
fun FieldServiceReportImage(report: FieldServiceReport) {
    val textMeasurer = rememberTextMeasurer()
    val title = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        ) {
            append("FIELD SERVICE REPORT")
        }
    }
    val titleSize = textMeasurer.measure(title).size
    val name = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        ) {
            append("Name:")
        }
    }
    val nameSize = textMeasurer.measure(name).size
    val month = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        ) {
            append("Month:")
        }
    }
    val monthSize = textMeasurer.measure(month).size

    val createRowString: (text: String) -> AnnotatedString = { text ->
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = Color.Black,
                    fontSize = 10.sp,
                )
            ) {
                append(text)
            }
        }
    }

    val createValueString: (text: String) -> AnnotatedString = { text ->
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontFamily = Caveat,
                )
            ) {
                append(text)
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val commentsTitle = createRowString("Comments:")
        val comments = createValueString(report.comments)
        val width = constraints.minWidth - 140f
        val constraints = Constraints(minWidth = width.toInt(), maxWidth = width.toInt())

        val minBoxHeight = 120f
        val commentsBoxHeight = if (report.comments.isNotEmpty()) {
            val commentsSize = textMeasurer.measure(comments, constraints = constraints).size
            val commentsTitleSize =
                textMeasurer.measure(commentsTitle, constraints = constraints).size
            maxOf(minBoxHeight, commentsSize.height + commentsTitleSize.height + 10f)
        } else {
            minBoxHeight
        }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(LocalDensity.current.run { (650 + commentsBoxHeight).toDp() })
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            // title
            drawText(
                textMeasurer = textMeasurer,
                text = title,
                topLeft = Offset((size.width - titleSize.width) / 2, 40f)
            )

            // name
            drawText(
                textMeasurer = textMeasurer,
                text = name,
                topLeft = Offset(70f, 140f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = createValueString(report.name),
                Offset(70f + nameSize.width + 20f, 130f),
            )
            drawLine(
                Color.Gray,
                Offset(70f + nameSize.width + 10f, 140f + nameSize.height - 10f),
                Offset(size.width - 40f, 140f + nameSize.height - 10f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 3f)),
            )
            // month
            drawText(
                textMeasurer = textMeasurer,
                text = month,
                topLeft = Offset(70f, 200f),
            )
            drawText(
                textMeasurer = textMeasurer,
                text = createValueString(report.month),
                Offset(70f + monthSize.width + 20f, 190f),
            )
            drawLine(
                Color.Gray,
                Offset(70f + monthSize.width + 10f, 200f + monthSize.height - 10f),
                Offset(size.width - 40f, 200f + monthSize.height - 10f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 3f)),
            )

            // table
            drawRect(
                Color.Black,
                topLeft = Offset(40f, 300f),
                size = Size(size.width - 80f, 275f),
                style = Stroke(),
            )
            drawLine(
                Color.Black,
                Offset(size.width - 140f, 300f),
                Offset(size.width - 140f, 575f)
            )

            // placements row
            drawText(
                textMeasurer = textMeasurer,
                text = createRowString("Placements (Printed and Electronic)"),
                Offset(70f, 300f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = createValueString(report.placements.toString()),
                Offset(size.width - 140f + 20f, 300f)
            )
            drawLine(Color.Black, Offset(40f, 355f), Offset(size.width - 40f, 355f))

            // video showings row
            drawText(
                textMeasurer = textMeasurer,
                text = createRowString("Video Showings"),
                Offset(70f, 355f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = createValueString(report.videoShowings.toString()),
                Offset(size.width - 140f + 20f, 355f)
            )
            drawLine(Color.Black, Offset(40f, 410f), Offset(size.width - 40f, 410f))

            // hours row
            drawText(
                textMeasurer = textMeasurer,
                text = createRowString("Hours"),
                Offset(70f, 410f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = createValueString(report.hours.toString()),
                Offset(size.width - 140f + 20f, 410f)
            )
            drawLine(Color.Black, Offset(40f, 465f), Offset(size.width - 40f, 465f))

            // return visits row
            drawText(
                textMeasurer = textMeasurer,
                text = createRowString("Return Visits"),
                Offset(70f, 465f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = createValueString(report.returnVisits.toString()),
                Offset(size.width - 140f + 20f, 465f)
            )
            drawLine(Color.Black, Offset(40f, 520f), Offset(size.width - 40f, 520f))

            // bible studies row
            drawText(
                textMeasurer = textMeasurer,
                text = createRowString("Number of Different Bible Studies Conducted"),
                Offset(70f, 520f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = createValueString(report.bibleStudies.toString()),
                Offset(size.width - 140f + 20f, 520f)
            )

            // comments box
            drawRect(
                Color.Black,
                topLeft = Offset(40f, 610f),
                size = Size(size.width - 80f, commentsBoxHeight),
                style = Stroke(),
            )
            drawText(
                textMeasurer = textMeasurer,
                text = commentsTitle,
                Offset(70f, 610f),
            )
            drawText(
                textMeasurer = textMeasurer,
                text = comments,
                topLeft = Offset(70f, 660f),
                size = Size(height = Float.NaN, width = width),
            )
        }
    }
}
