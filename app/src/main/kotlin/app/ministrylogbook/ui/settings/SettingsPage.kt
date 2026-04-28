@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "ASSIGNED_VALUE_IS_NEVER_READ", "UNUSED_VALUE")

package app.ministrylogbook.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.BuildConfig
import app.ministrylogbook.R
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.utilities.getLocale
import app.ministrylogbook.shared.layouts.AlertDialog
import app.ministrylogbook.shared.layouts.MonthPickerDialog
import app.ministrylogbook.shared.layouts.OptionList
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.settings.viewmodel.SettingsViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsPage(viewModel: SettingsViewModel = koinViewModel()) {
    val scrollState = rememberScrollState()
    val role by viewModel.role.collectAsStateWithLifecycle()
    val sendReportReminder by viewModel.sendReportReminder.collectAsStateWithLifecycle()

    BaseSettingsPage(stringResource(R.string.settings), toolbarElevation = scrollState.canScrollBackward) {
        Column(
            Modifier
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Title(stringResource(R.string.personal_information))
                NameSetting()
                RoleSetting(role, onChange = {
                    viewModel.setRole(it)
                })
                PioneerSinceSetting()
                GoalSetting()
            }
            Column {
                Title(stringResource(R.string.appearance))
                LanguageSetting()
                DesignSetting()
                SystemColorsSetting()
            }
            Column {
                Title(stringResource(R.string.behaviour))
                SendReportReminderSetting(sendReportReminder, onChange = {
                    viewModel.setSendReportReminders(it)
                })
                PrecisionModeSetting()
            }
            Column {
                Title(stringResource(R.string.data))
                BackupSetting()
            }
            Column {
                Title(stringResource(R.string.legal))
                PrivacyPolicySetting()
                OpenSourceLicenses()
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(
                        R.string.version,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun BackupSetting(viewModel: SettingsViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val lastBackup by viewModel.lastBackup.collectAsStateWithLifecycle()
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val lastBackupDescription = if (lastBackup != null) {
        stringResource(
            R.string.last_backup_value,
            dateTimeFormatter.format(lastBackup?.toJavaLocalDateTime())
        )
    } else {
        stringResource(R.string.no_backup_yet)
    }

    Setting(
        title = stringResource(R.string.backup),
        description = lastBackupDescription,
        onClick = {
            navController.navigateToBackup()
        }
    )
}

@Composable
fun SystemColorsSetting(viewModel: SettingsViewModel = koinViewModel()) {
    val useSystemColors by viewModel.useSystemColors.collectAsStateWithLifecycle()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Setting(title = stringResource(R.string.use_system_colors), paddingValues = PaddingValues(20.dp, 4.dp)) {
            Switch(checked = useSystemColors, onCheckedChange = {
                viewModel.setUseSystemColors(it)
            })
        }
    }
}

@Composable
fun DesignSetting(viewModel: SettingsViewModel = koinViewModel()) {
    var isDialogOpen by remember { mutableStateOf(false) }
    val design by viewModel.design.collectAsStateWithLifecycle()

    val handleClose = {
        isDialogOpen = false
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        isOpen = isDialogOpen,
        onDismissRequest = handleClose,
        title = {
            Text(stringResource(R.string.design))
        },
        dismissButton = {
            TextButton(onClick = handleClose) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        OptionList {
            Design.entries.map { d ->
                Option(text = d.translate(), selected = d == design, onClick = {
                    viewModel.setDesign(d)
                    handleClose()
                })
            }
        }
    }

    Setting(
        title = stringResource(R.string.design),
        onClick = { isDialogOpen = true }
    ) {
        Text(
            design.translate(),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
        )
    }
}

@Composable
fun NameSetting(viewModel: SettingsViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val name by viewModel.name.collectAsStateWithLifecycle()
    val noNameSetText = stringResource(R.string.no_name_set)
    val nameOrDefault by remember { derivedStateOf { name.ifEmpty { noNameSetText } } }

    Setting(title = stringResource(R.string.name), onClick = {
        navController.navigateToSettingsName()
    }) {
        Text(
            nameOrDefault,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
        )
    }
}

@Composable
fun RoleSetting(role: Role, onChange: (Role) -> Unit) {
    var isRoleDialogOpen by remember { mutableStateOf(false) }

    val handleClose = {
        isRoleDialogOpen = false
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        isOpen = isRoleDialogOpen,
        onDismissRequest = handleClose,
        title = {
            Text(stringResource(R.string.role))
        },
        dismissButton = {
            TextButton(onClick = handleClose) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        OptionList {
            Role.entries.map { r ->
                Option(text = r.translate(), selected = r == role, onClick = {
                    onChange(r)
                    handleClose()
                })
            }
        }
    }

    Setting(
        title = stringResource(R.string.role),
        onClick = { isRoleDialogOpen = true }
    ) {
        Text(
            role.translate(),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
        )
    }
}

@Composable
fun PrecisionModeSetting(viewModel: SettingsViewModel = koinViewModel()) {
    val precisionMode by viewModel.precisionMode.collectAsStateWithLifecycle()

    Setting(
        title = stringResource(R.string.precision_mode),
        description = stringResource(R.string.precision_mode_description)
    ) {
        Switch(checked = precisionMode, onCheckedChange = {
            viewModel.setPrecisionMode(it)
        })
    }
}

@Composable
fun SendReportReminderSetting(
    sendReportReminder: Boolean,
    onChange: (Boolean) -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
) {
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager }
    var hasPermission by remember {
        val isGranted = if (Build.VERSION.SDK_INT >= 33) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        mutableStateOf(isGranted)
    }

    val requestExactAlarmPermission = {
        if (Build.VERSION.SDK_INT >= 31 && alarmManager?.canScheduleExactAlarms() == false) {
            Intent().also { intent ->
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                context.startActivity(intent)
            }
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
            hasPermission = isGranted
            if (isGranted) {
                onChange(true)
            }

            requestExactAlarmPermission()
        })

    Setting(
        title = stringResource(R.string.send_report_reminder_title),
        description = stringResource(R.string.send_report_reminder_setting_description),
        paddingValues = paddingValues
    ) {
        Switch(checked = sendReportReminder && hasPermission, onCheckedChange = { checked ->
            if (checked) {
                if (Build.VERSION.SDK_INT >= 33 && !hasPermission) {
                    val permission = android.Manifest.permission.POST_NOTIFICATIONS
                    launcher.launch(permission)
                } else {
                    onChange(true)
                    requestExactAlarmPermission()
                }
            } else {
                onChange(false)
            }
        })
    }
}

@Composable
fun PioneerSinceSetting(viewModel: SettingsViewModel = koinViewModel()) {
    val role by viewModel.role.collectAsStateWithLifecycle()

    if (role == Role.RegularPioneer || role == Role.SpecialPioneer) {
        var isDialogOpen by remember { mutableStateOf(false) }
        val pioneerSince by viewModel.pioneerSince.collectAsStateWithLifecycle()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        MonthPickerDialog(
            isOpen = isDialogOpen,
            initialMonth = pioneerSince ?: today,
            onDismissRequest = { isDialogOpen = false },
            onSelect = {
                viewModel.setPioneerSince(it)
                isDialogOpen = false
            }
        )

        Setting(title = stringResource(R.string.start_of_pioneering), onClick = { isDialogOpen = true }) {
            val pattern = stringResource(R.string.start_of_pioneering_month_pattern)
            val formatter = DateTimeFormatter.ofPattern(pattern)
            val monthText = if (pioneerSince != null) {
                formatter.format(pioneerSince?.toJavaLocalDate())
            } else {
                stringResource(R.string.no_date_set)
            }

            Text(
                monthText,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
            )
        }
    }
}

@Composable
fun GoalSetting(viewModel: SettingsViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val role by viewModel.role.collectAsStateWithLifecycle()
    val goal by viewModel.goal.collectAsStateWithLifecycle()
    val roleGoal by viewModel.roleGoal.collectAsStateWithLifecycle()
    val manuallySetGoal by viewModel.manuallySetGoal.collectAsStateWithLifecycle()
    val noGoalSetText = stringResource(R.string.no_goal_set)
    val manuallySetText = stringResource(R.string.manually_set_colon)
    val goalUnitText = pluralStringResource(R.plurals.hours_unit, goal ?: 0, goal ?: 0)

    val goalText by remember(role, goal, roleGoal, manuallySetGoal) {
        derivedStateOf {
            if (role == Role.Publisher && goal == roleGoal && manuallySetGoal == null) {
                noGoalSetText
            } else {
                val showManuallySet =
                    manuallySetGoal != null && manuallySetGoal != roleGoal && role != Role.Publisher
                val prefix = if (showManuallySet) "$manuallySetText " else ""
                goal?.let { prefix + goalUnitText } ?: ""
            }
        }
    }

    Setting(title = stringResource(R.string.monthly_goal), onClick = {
        navController.navigateToSettingsGoal()
    }) {
        Text(
            goalText,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
        )
    }
}

@Composable
fun LanguageSetting() {
    var isDialogOpen by remember { mutableStateOf(false) }
    val locale = AppCompatDelegate.getApplicationLocales().get(0)
    val localeDisplayName = if (locale != null) {
        "${locale.getDisplayLanguage(locale)} (${
            locale.getDisplayLanguage(
                Locale.ENGLISH
            )
        })"
    } else {
        stringResource(R.string.system_default)
    }

    LanguagePicker(open = isDialogOpen, onClose = {
        isDialogOpen = false
    })

    Setting(title = stringResource(R.string.language), onClick = { isDialogOpen = true }) {
        Text(
            localeDisplayName,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
        )
    }
}

@Composable
fun LanguagePicker(open: Boolean = false, onClose: () -> Unit = {}) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        isOpen = open,
        onDismissRequest = onClose,
        title = {
            Text(stringResource(R.string.language))
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        OptionList {
            supportedLocales.map { supportedLocale ->
                val supportedLocaleDisplayName = if (supportedLocale != null) {
                    "${supportedLocale.getDisplayLanguage(supportedLocale)} (${
                        supportedLocale.getDisplayLanguage(Locale.ENGLISH)
                    })"
                } else {
                    stringResource(R.string.system_default)
                }

                Option(supportedLocaleDisplayName, onClick = {
                    val localeList =
                        if (supportedLocale != null) {
                            LocaleListCompat.create(supportedLocale)
                        } else {
                            LocaleListCompat.getEmptyLocaleList()
                        }
                    AppCompatDelegate.setApplicationLocales(localeList)
                    onClose()
                })
            }
        }
    }
}

private val supportedLocales = listOf(null, Locale.ENGLISH, Locale.GERMAN)

@Composable
fun OpenSourceLicenses() {
    val navController = LocalAppNavController.current

    Setting(title = stringResource(R.string.open_source_licenses), onClick = {
        navController.navigateToOpenSourceLicenses()
    })
}

@Composable
fun PrivacyPolicySetting() {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val design = when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_YES -> CustomTabsIntent.COLOR_SCHEME_DARK
        AppCompatDelegate.MODE_NIGHT_NO -> CustomTabsIntent.COLOR_SCHEME_LIGHT
        else -> CustomTabsIntent.COLOR_SCHEME_SYSTEM
    }
    val locale = getLocale()

    Setting(title = stringResource(R.string.privacy_policy), onClick = {
        val arrowBackDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_arrow_back)
        DrawableCompat.setTint(arrowBackDrawable!!, colorScheme.onSurface.toArgb())
        val customTabsIntent = CustomTabsIntent.Builder()
            .setCloseButtonIcon(arrowBackDrawable.toBitmap())
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(colorScheme.surface.toArgb())
                    .build()
            )
            .setColorSchemeParams(
                CustomTabsIntent.COLOR_SCHEME_DARK,
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(colorScheme.surface.toArgb())
                    .build()
            )
            .setColorScheme(design)
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(context, getPrivacyPolicyUrl(locale).toUri())
    })
}

fun getPrivacyPolicyUrl(locale: Locale) = "https://ministrylogbook.app/${locale.language}/privacy-policy"
