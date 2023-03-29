package com.github.danieldaeschle.ministrynotes.ui.shared

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(start = 14.dp, end = 12.dp),
    elevation: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit = {}
) {
    val absoluteElevation = LocalAbsoluteTonalElevation.current + elevation
    val surface = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
        absoluteElevation
    )
    val isDarkThemeEnabled = isSystemInDarkTheme()
    val animatedBackground = remember { Animatable(surface) }

    LaunchedEffect(elevation) {
        val targetColor =
            if (elevation.value == 0f || !isDarkThemeEnabled) surface else backgroundColor
        animatedBackground.animateTo(targetColor, animationSpec = tween(200))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        color = animatedBackground.value,
        shadowElevation = elevation,
    ) {
        Row(
            modifier = Modifier
                .padding(padding)
                .statusBarsPadding()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
fun ToolbarAction(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}