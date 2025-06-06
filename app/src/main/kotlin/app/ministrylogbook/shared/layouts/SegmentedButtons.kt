package app.ministrylogbook.shared.layouts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SegmentedButtonsScope(initialSelectedIndex: Int) {
    private constructor(selectedIndex: Int, buttonCount: Int, segmentedButtonCount: Int) : this(selectedIndex) {
        this.selectedIndex.value = selectedIndex
        this.buttonCount.value = buttonCount
        this@SegmentedButtonsScope.segmentedButtonCount = segmentedButtonCount
    }

    private var selectedIndex = MutableStateFlow(initialSelectedIndex)
    private var buttonCount = MutableStateFlow(0)
    private var segmentedButtonCount = 0

    val selectedIndexState = selectedIndex.asStateFlow()
    val buttonCountState = buttonCount.asStateFlow()

    @Composable
    fun RowScope.SegmentedButton(onClick: () -> Unit = {}, text: @Composable () -> Unit) {
        val index = rememberSaveable { segmentedButtonCount++ }

        LaunchedEffect(Unit) {
            buttonCount.update { segmentedButtonCount }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .clickable {
                    onClick()
                    selectedIndex.value = index
                },
            contentAlignment = Alignment.Center
        ) {
            text()
        }
    }

    companion object {
        @Suppress("ktlint:standard:function-naming")
        fun Saver() = mapSaver(
            save = {
                mapOf(
                    "selectedIndex" to it.selectedIndex.value,
                    "buttonCount" to it.buttonCount.value,
                    "segmentedButtonCount" to it.segmentedButtonCount
                )
            },
            restore = {
                SegmentedButtonsScope(
                    it["selectedIndex"] as Int,
                    it["buttonCount"] as Int,
                    it["segmentedButtonCount"] as Int
                )
            }
        )
    }
}

@Composable
fun rememberSegmentedButtonsScope(initialSelectedIndex: Int) =
    rememberSaveable(saver = SegmentedButtonsScope.Saver()) {
        SegmentedButtonsScope(initialSelectedIndex)
    }

@Composable
fun SegmentedButtons(
    initialSelectedIndex: Int = 0,
    content: @Composable SegmentedButtonsScope.(scope: RowScope) -> Unit
) {
    val scope = rememberSegmentedButtonsScope(initialSelectedIndex)
    val selectedIndex by scope.selectedIndexState.collectAsStateWithLifecycle()
    val buttonCount by scope.buttonCountState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
    ) {
        val width = maxWidth
        val startPadding by animateDpAsState(
            targetValue = width / max(buttonCount, 1) * selectedIndex,
            label = ""
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = startPadding)
        ) {
            if (buttonCount > 0) {
                Box(
                    Modifier
                        .width(width / buttonCount)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }
        Row(Modifier.fillMaxSize()) {
            scope.content(this)
        }
    }
}
