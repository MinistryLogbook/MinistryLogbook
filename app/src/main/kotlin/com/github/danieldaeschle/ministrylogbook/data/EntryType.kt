package com.github.danieldaeschle.ministrylogbook.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.github.danieldaeschle.ministrylogbook.R
import com.github.danieldaeschle.ministrylogbook.ui.theme.ProgressPositive

enum class EntryType {
    Ministry, TheocraticAssignment, TheocraticSchool, Transfer;

    @Composable
    @ReadOnlyComposable
    fun translate(): String {
        val context = LocalContext.current
        return when (this) {
            Ministry -> context.getString(R.string.ministry)
            TheocraticAssignment -> context.getString(R.string.theocratic_assignment)
            TheocraticSchool -> context.getString(R.string.theocratic_school_or_class)
            Transfer -> context.getString(R.string.transfer)
        }
    }

    @Composable
    fun icon(): Painter = when (this) {
        Ministry -> painterResource(R.drawable.ic_work)
        TheocraticAssignment -> painterResource(R.drawable.ic_build)
        TheocraticSchool -> painterResource(R.drawable.ic_school)
        Transfer -> painterResource(R.drawable.ic_exit_to_app)
    }

    @Composable
    @ReadOnlyComposable
    fun color(): Color = when (this) {
        Ministry -> MaterialTheme.colorScheme.primary
        TheocraticAssignment -> Color(0xFFFF9800)
        TheocraticSchool -> Color(0xFFFF9800)
        Transfer -> ProgressPositive
    }
}