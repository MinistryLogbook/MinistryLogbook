package app.ministrylogbook.shared.layouts.progress

import androidx.compose.ui.graphics.Color

sealed class ProgressKind {
    data class Progress(val percent: Float = 0f, val color: Color) : ProgressKind()

    data class Indicator(val percent: Float = 0f, val color: Color) : ProgressKind()
}
