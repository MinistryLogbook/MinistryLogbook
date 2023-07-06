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
import androidx.compose.runtime.remember
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
    private var _selectedIndex = MutableStateFlow(initialSelectedIndex)
    private var _buttonCount = MutableStateFlow(0)
    private var _segmentedButtonCount = 0

    val selectedIndexState = _selectedIndex.asStateFlow()
    val buttonCountState = _buttonCount.asStateFlow()

    @Composable
    fun RowScope.SegmentedButton(onClick: () -> Unit = {}, text: @Composable () -> Unit) {
        val index = rememberSaveable { _segmentedButtonCount++ }

        LaunchedEffect(Unit) {
            _buttonCount.update { _segmentedButtonCount }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .clickable {
                    onClick()
                    _selectedIndex.value = index
                },
            contentAlignment = Alignment.Center
        ) {
            text()
        }
    }
}

@Composable
fun rememberSegmentedButtonsScope(initialSelectedIndex: Int) = remember { SegmentedButtonsScope(initialSelectedIndex) }

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
