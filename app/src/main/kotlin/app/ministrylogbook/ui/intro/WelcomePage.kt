package app.ministrylogbook.ui.intro

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.MainActivity
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.DeferredAnimatedVisibility
import app.ministrylogbook.shared.layouts.expandVerticallyWithFade
import app.ministrylogbook.shared.utilities.restartApp
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.backup.BackupImportDialog
import app.ministrylogbook.ui.home.backup.viewmodel.BackupIntent
import app.ministrylogbook.ui.home.backup.viewmodel.BackupViewModel
import app.ministrylogbook.ui.home.backup.viewmodel.BackupViewModelOptions
import app.ministrylogbook.ui.settings.LanguagePicker
import app.ministrylogbook.ui.shared.Toolbar
import app.ministrylogbook.ui.shared.ToolbarAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WelcomePage(
    backupViewModel: BackupViewModel = koinViewModel(
        parameters = {
            parametersOf(
                BackupViewModelOptions(
                    markIntroShownAfterImport = true,
                    observeLatestEntry = false
                )
            )
        }
    )
) {
    val navController = LocalAppNavController.current
    val backupState by backupViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val invalidBackupMessage = stringResource(R.string.backup_is_invalid)
    var animated by rememberSaveable { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }
    val openDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }
            backupViewModel.dispatch(BackupIntent.SelectBackupFile(uri))
        }

    LanguagePicker(isDialogOpen, onClose = { isDialogOpen = false })

    LaunchedEffect(backupState.selectedBackupFile, backupState.isBackupValid) {
        if (backupState.selectedBackupFile != null && !backupState.isBackupValid) {
            Toast.makeText(
                context,
                invalidBackupMessage,
                Toast.LENGTH_LONG
            ).show()
            backupViewModel.dispatch(BackupIntent.UnselectBackupFile)
        }
    }

    LaunchedEffect(backupState.importFinished) {
        if (backupState.importFinished) {
            context.restartApp(Intent(context, MainActivity::class.java))
        }
    }

    if (backupState.selectedBackupFile != null && backupState.isBackupValid) {
        BackupImportDialog(
            backupState.selectedBackupFile!!,
            backupState.latestEntry,
            onImport = {
                backupViewModel.dispatch(BackupIntent.ImportBackup)
            },
            onDismiss = {
                backupViewModel.dispatch(BackupIntent.UnselectBackupFile)
            }
        )
    }

    Surface(Modifier.fillMaxSize().navigationBarsPadding()) {
        Column {
            Toolbar {
                Spacer(modifier = Modifier.weight(1f))
                val restoreBackupDescription = stringResource(R.string.restore_backup)
                ToolbarAction(
                    description = restoreBackupDescription,
                    onClick = {
                        openDocumentLauncher.launch(arrayOf("application/*"))
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_settings_backup_restore),
                        contentDescription = restoreBackupDescription
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                val languageDescription = stringResource(R.string.language)
                ToolbarAction(
                    description = languageDescription,
                    onClick = {
                        isDialogOpen = true
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_language),
                        contentDescription = languageDescription
                    )
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
