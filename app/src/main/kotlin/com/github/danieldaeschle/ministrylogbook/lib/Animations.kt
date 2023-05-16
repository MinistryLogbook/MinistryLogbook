package com.github.danieldaeschle.ministrylogbook.lib

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut

fun stayOut(durationMillis: Int) = fadeOut(stay(durationMillis))

fun <T> stay(durationMillis: Int) = tween<T>(1, delayMillis = durationMillis)
