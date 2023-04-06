package com.github.danieldaeschle.ministrynotes.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.github.danieldaeschle.ministrynotes.R

enum class EntryKind {
    Ministry, TheocraticAssignment, TheocraticSchool, Transfer;

    fun translate(): String = when (this) {
        Ministry -> "Ministry"
        TheocraticAssignment -> "Theocratic Assignment"
        TheocraticSchool -> "Theocratic School"
        Transfer -> "Transfer"
    }

    @Composable
    fun icon(): Painter = when (this) {
        Ministry -> painterResource(R.drawable.ic_work)
        TheocraticAssignment -> painterResource(R.drawable.ic_build)
        TheocraticSchool -> painterResource(R.drawable.ic_school)
        Transfer -> painterResource(R.drawable.ic_tab_move)
    }

    @Composable
    fun color(): Color = when (this) {
        Ministry -> MaterialTheme.colorScheme.primary
        TheocraticAssignment -> Color(0xFFFF9800)
        TheocraticSchool -> Color(0xFFFF9800)
        Transfer -> Color(0xFF9E9E9E)
    }
}