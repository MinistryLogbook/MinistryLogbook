package app.ministrylogbook.shared.utilities

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.condition(condition: Boolean, then: @Composable Modifier.() -> Modifier): Modifier = if (condition) {
    composed(factory = then)
} else {
    this
}
