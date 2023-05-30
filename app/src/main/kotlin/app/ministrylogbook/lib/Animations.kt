package app.ministrylogbook.lib

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable

fun stayOut(durationMillis: Int) = fadeOut(stay(durationMillis))

fun <T> stay(durationMillis: Int) = tween<T>(1, delayMillis = durationMillis)


@Composable
fun ExpandAnimationVisibility(show: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = show,
        enter = expandVertically(tween(durationMillis = 200)) + fadeIn(tween(delayMillis = 250)),
        exit = shrinkVertically(
            tween(
                durationMillis = 200,
                delayMillis = AnimationConstants.DefaultDurationMillis + 50
            )
        ) + fadeOut(tween()),
    ) {
        content()
    }
}