package app.ministrylogbook.ui.home

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.shared.utilities.getLocale
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.backup.navigateToBackup
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.settings.navigateToSettings
import app.ministrylogbook.ui.shared.ToolbarAction
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileButton(viewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val name by viewModel.name.collectAsStateWithLifecycle()

    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = {
                navController.navigateToHomeMenu()
            })
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(100f))
                .background(MaterialTheme.colorScheme.secondary.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            val char = if (name.isNotEmpty()) name.substring(0..0) else ""
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
            navController.navigateUp()
        }) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = null // TODO: contentDescription
            )
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
            contentDescription = null // TODO: contentDescription
        )
        Spacer(Modifier.width(16.dp))
        Text(stringResource(R.string.settings))
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { navController.navigateToBackup() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            painterResource(R.drawable.ic_settings_backup_restore),
            contentDescription = null // TODO: contentDescription
        )
        Spacer(Modifier.width(16.dp))
        Text(stringResource(R.string.backup))
    }

    Divider()

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        val context = LocalContext.current
        val builder = remember { CustomTabsIntent.Builder() }
        val colorScheme = MaterialTheme.colorScheme
        val design = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> CustomTabsIntent.COLOR_SCHEME_DARK
            AppCompatDelegate.MODE_NIGHT_NO -> CustomTabsIntent.COLOR_SCHEME_LIGHT
            else -> CustomTabsIntent.COLOR_SCHEME_SYSTEM
        }
        val locale = getLocale()

        val handleOpenPrivacyPolicy = {
            val arrowBackDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_arrow_back)
            DrawableCompat.setTint(arrowBackDrawable!!, colorScheme.onSurface.toArgb())
            val customTabsIntent = builder
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

            customTabsIntent.launchUrl(context, Uri.parse(getPrivacyPolicyUrl(locale)))
        }

        TextButton(onClick = handleOpenPrivacyPolicy) {
            Text(
                stringResource(R.string.privacy_policy),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun getPrivacyPolicyUrl(locale: Locale) = "https://ministrylogbook.app/${locale.language}/privacy-policy"
