package app.ministrylogbook.shared.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

fun stayOut(durationMillis: Int) = fadeOut(stay(durationMillis))

fun <T> stay(durationMillis: Int) = tween<T>(1, delayMillis = durationMillis)

fun expandVerticallyWithFade(durationMillis: Int = 200) =
    expandVertically(tween(durationMillis = durationMillis)) + fadeIn(tween(delayMillis = durationMillis + 50))

@Composable
fun ExpandAnimatedVisibility(show: Boolean, content: @Composable AnimatedVisibilityScope.() -> Unit) {
    AnimatedVisibility(
        visible = show,
        enter = expandVertically(tween(durationMillis = 200)) + fadeIn(tween(delayMillis = 250)),
        exit = shrinkVertically(
            tween(
                durationMillis = 200,
                delayMillis = AnimationConstants.DefaultDurationMillis + 50
            )
        ) + fadeOut(tween()),
        content = content
    )
}

@Composable
fun DeferredAnimatedVisibility(
    delayMillis: Long,
    transition: EnterTransition,
    animate: Boolean = true,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis)
        isVisible = true
    }

    if (animate) {
        AnimatedVisibility(
            visible = isVisible,
            enter = transition,
            content = { content() }
        )
    } else {
        content()
    }
}
