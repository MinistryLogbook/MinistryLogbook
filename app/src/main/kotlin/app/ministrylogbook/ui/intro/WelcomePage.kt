package app.ministrylogbook.ui.intro

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.DeferredAnimatedVisibility
import app.ministrylogbook.shared.layouts.expandVerticallyWithFade
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.settings.LanguagePicker
import app.ministrylogbook.ui.shared.Toolbar
import app.ministrylogbook.ui.shared.ToolbarAction

@Composable
fun WelcomePage() {
    val navController = LocalAppNavController.current
    var animated by rememberSaveable { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }

    LanguagePicker(isDialogOpen, onClose = { isDialogOpen = false })

    Surface(Modifier.fillMaxSize()) {
        Column {
            Toolbar {
                Spacer(modifier = Modifier.weight(1f))
                ToolbarAction(onClick = {
                    isDialogOpen = true
                }) {
                    Icon(painterResource(R.drawable.ic_language), contentDescription = null)
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(160.dp)) {
                    DeferredAnimatedVisibility(
                        300,
                        animate = !animated,
                        transition = slideInVertically(tween(500)) { it / 3 } + fadeIn(tween(500))
                    ) {
                        Image(
                            painterResource(R.drawable.logo),
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                DeferredAnimatedVisibility(1200, animate = !animated, transition = expandVerticallyWithFade(500)) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.app_welcome),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            stringResource(R.string.app_description),
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                DeferredAnimatedVisibility(
                    3000,
                    animate = !animated,
                    transition = slideInVertically(tween(800)) { it / 3 } + fadeIn(tween(800))
                ) {
                    Button(onClick = {
                        animated = true
                        navController.navigateToSetup()
                    }) {
                        Text(stringResource(R.string.start_now))
                    }
                }
            }
        }
    }
}
