package com.github.danieldaeschle.ministrynotes.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.settings.SettingsGraph
import com.github.danieldaeschle.ministrynotes.ui.shared.ToolbarAction


@Composable
fun ProfileButton() {
    val navController = LocalAppNavController.current

    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(100f))
            .clickable(onClick = {
                navController.navigate(HomeGraph.ProfileMenu.route)
            })
            .padding(4.dp), contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(100f))
                .background(MaterialTheme.colorScheme.onSurface.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("D")
        }
    }
}

@Composable
fun ProfilePopup() {
    val navController = LocalAppNavController.current

    Row(
        Modifier
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarAction(onClick = {
            navController.popBackStack()
        }) {
            Icon(painterResource(R.drawable.ic_close), contentDescription = null)
        }
    }

    Divider()

    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(SettingsGraph.Root.route)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
        Icon(painterResource(R.drawable.ic_settings), contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text("Settings")
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
        Icon(
            painterResource(R.drawable.ic_tips_and_updates),
            contentDescription = null
        )
        Spacer(Modifier.width(16.dp))
        Text("News")
    }
}