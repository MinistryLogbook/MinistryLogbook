package app.ministrylogbook.shared.layouts.progress

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.ministrylogbook.ui.shared.ComposeTestActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class CircleProgressIndicatorTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun marker_isVisibleAtSixtyPercent() {
        assertMarkerSeparatorPixels(
            name = "sixty-percent",
            progress = 0.6f,
            indicatorProgress = ProgressKind.Progress(percent = 0.6f, color = progressColor)
        )
    }

    @Test
    fun marker_isVisibleWhenProgressEndOverlapsStart() {
        assertMarkerSeparatorPixels(
            name = "ninety-nine-percent-with-inner-progress",
            progress = 0.99f,
            indicatorProgress = ProgressKind.Progress(percent = 0.99f, color = progressColor),
            segment = CircleProgressSegment(startPercent = 0.92f, endPercent = 0.99f, color = innerProgressColor)
        )
    }

    @Test
    fun marker_isVisibleForTinyProgressWithInnerProgress() {
        assertMarkerSeparatorPixels(
            name = "tiny-progress-with-inner-progress",
            progress = 0.001f,
            indicatorProgress = ProgressKind.Progress(percent = 0.001f, color = progressColor),
            segment = CircleProgressSegment(startPercent = 0.001f, endPercent = 0.02f, color = innerProgressColor),
            assertNoInnerProgressNearMarker = true
        )
    }

    @Test
    fun marker_isVisibleForOverflowProgress() {
        assertMarkerSeparatorPixels(
            name = "overflow-progress",
            progress = 1.25f,
            indicatorProgress = ProgressKind.Progress(percent = 1.25f, color = progressColor)
        )
    }

    private fun assertMarkerSeparatorPixels(
        name: String,
        progress: Float,
        indicatorProgress: ProgressKind.Progress,
        segment: CircleProgressSegment? = null,
        assertNoInnerProgressNearMarker: Boolean = false
    ) {
        ActivityScenario.launch(ComposeTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    MaterialTheme(
                        colorScheme = lightColorScheme(background = backgroundColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(INDICATOR_SIZE)
                                .background(backgroundColor)
                                .testTag(INDICATOR_TAG)
                        ) {
                            CircleProgressIndicator(
                                modifier = Modifier.size(INDICATOR_SIZE),
                                strokeWidth = STROKE_WIDTH,
                                animationSpec = snap(),
                                progress = indicatorProgress,
                                segment = segment,
                                baseLineColor = markerSeparatorColor
                            )
                        }
                    }
                }
            }

            compose.waitForIdle()
            val image = compose.onNodeWithTag(INDICATOR_TAG).captureToImage()
            writeSnapshot(name, image.asAndroidBitmap())
            val pixels = image.toPixelMap()
            val markerPercent = markerPercent(progress)
            val strokeWidthPx = image.width * STROKE_WIDTH.value / INDICATOR_SIZE.value
            val radius = (image.width - strokeWidthPx) / 2f
            val angle = -90f + markerPercent * 360f
            val angleRadians = angle / 180f * PI.toFloat()
            val markerCenterX = image.width / 2f + cos(angleRadians) * radius
            val markerCenterY = image.height / 2f + sin(angleRadians) * radius
            val searchRadius = (strokeWidthPx / 2f).roundToInt()
            var separatorPixels = 0
            var innerProgressPixels = 0

            for (x in (markerCenterX.roundToInt() - searchRadius)..(markerCenterX.roundToInt() + searchRadius)) {
                for (y in (markerCenterY.roundToInt() - searchRadius)..(markerCenterY.roundToInt() + searchRadius)) {
                    if (x in 0 until image.width && y in 0 until image.height && pixels[x, y].isMarkerSeparator()) {
                        separatorPixels++
                    }
                    if (x in 0 until image.width && y in 0 until image.height && pixels[x, y].isInnerProgress()) {
                        innerProgressPixels++
                    }
                }
            }

            assertTrue(
                "Expected marker separator pixels near progress=$progress, found $separatorPixels",
                separatorPixels > 12
            )
            if (assertNoInnerProgressNearMarker) {
                assertTrue(
                    "Expected no inner progress pixels near progress=$progress, found $innerProgressPixels",
                    innerProgressPixels == 0
                )
            }
        }
    }

    private fun markerPercent(progress: Float): Float {
        val overflowPercent = progress % 1f
        return when {
            progress > 1f && overflowPercent > 0f -> overflowPercent
            progress >= 1f -> 1f
            else -> progress
        }
    }

    private fun Color.isMarkerSeparator(): Boolean = red > 0.9f && green > 0.9f && blue > 0.9f && alpha > 0.9f

    private fun Color.isInnerProgress(): Boolean = red < 0.1f && green < 0.1f && blue > 0.9f && alpha > 0.9f

    private fun writeSnapshot(name: String, bitmap: Bitmap) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), SNAPSHOT_DIRECTORY)
        directory.mkdirs()
        FileOutputStream(File(directory, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private companion object {
        const val INDICATOR_TAG = "circle-progress-indicator"
        const val SNAPSHOT_DIRECTORY = "circle-progress-snapshots"
        val INDICATOR_SIZE = 240.dp
        val STROKE_WIDTH = 32.dp
        val backgroundColor = Color.Black
        val progressColor = Color.Red
        val innerProgressColor = Color.Blue
        val markerSeparatorColor = Color.White
    }
}
