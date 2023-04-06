package com.github.danieldaeschle.ministrynotes.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Tile(content: @Composable () -> Unit) {
    Surface(
        Modifier.padding(horizontal = 16.dp),
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        content = content,
    )
}