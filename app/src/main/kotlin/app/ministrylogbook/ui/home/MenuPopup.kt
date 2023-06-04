package app.ministrylogbook.ui.home

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.settings.navigateToSettings
import app.ministrylogbook.ui.shared.ToolbarAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileButton(viewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val name by viewModel.name.collectAsStateWithLifecycle()

    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(100f))
            .clickable(onClick = {
                navController.navigateToMenu()
            })
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(100f))
                .background(MaterialTheme.colorScheme.onSurface.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            val char = if (name.isNotEmpty()) name.substring(0..0) else "?"
            Text(char)
        }
    }
}

@Composable
fun MenuPopup() {
    val navController = LocalAppNavController.current

    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        ToolbarAction(Modifier.align(Alignment.CenterStart), onClick = {
            navController.popBackStack()
        }) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = null
            ) // TODO: contentDescription
        }

        Text(
            stringResource(R.string.app_name),
            modifier = Modifier.align(Alignment.Center),
            fontSize = 20.sp
        )
    }

    Divider()

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { navController.navigateToSettings() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            painterResource(R.drawable.ic_settings),
            contentDescription = null
        ) // TODO: contentDescription
        Spacer(Modifier.width(16.dp))
        Text(stringResource(R.string.settings))
    }
}
