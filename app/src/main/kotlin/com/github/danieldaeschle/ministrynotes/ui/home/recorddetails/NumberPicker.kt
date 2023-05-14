package com.github.danieldaeschle.ministrynotes.ui.home.recorddetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NumberPicker(
    value: Int, step: Int = 1, onChange: (newValue: Int) -> Unit = {}
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(100))
            .background(MaterialTheme.colorScheme.onSurface.copy(0.05f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_remove),
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(100))
                .repeatingClickable { onChange(value - step) }
                .clickable { onChange(value - step) }
                .padding(4.dp),
            contentDescription = "Minus", // TODO: translation
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            value.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(32.dp)
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            painterResource(R.drawable.ic_add),
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(100))
                .repeatingClickable { onChange(value + step) }
                .clickable { onChange(value + step) }
                .padding(4.dp),
            contentDescription = "Plus", // TODO: translation
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

fun Modifier.repeatingClickable(
    initialDelay: Long = 700,
    delay: Long = 120,
    onClick: () -> Unit,
): Modifier = composed {
    val currentClickListener by rememberUpdatedState(onClick)

    pointerInput(Unit) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)

                val heldButtonJob = launch {
                    delay(initialDelay)

                    while (down.pressed) {
                        currentClickListener()
                        delay(delay)
                    }
                }

                waitForUpOrCancellation()
                heldButtonJob.cancel()
            }
        }
    }
}