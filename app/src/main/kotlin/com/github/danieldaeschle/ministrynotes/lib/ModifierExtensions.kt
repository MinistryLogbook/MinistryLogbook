package com.github.danieldaeschle.ministrynotes.lib

import androidx.compose.ui.Modifier

fun Modifier.condition(
    condition: Boolean, then: Modifier.() -> Modifier
): Modifier = if (condition) {
    then()
} else {
    this
}