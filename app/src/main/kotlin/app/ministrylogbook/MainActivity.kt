package app.ministrylogbook

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.ui.AppGraph
import app.ministrylogbook.ui.AppNavHost
import app.ministrylogbook.ui.theme.MinistryLogbookTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val settingsService = SettingsService(this)
        val design = runBlocking {
            settingsService.design.firstOrNull() ?: Design.System
        }
        val showIntro = runBlocking {
            !(settingsService.introShown.firstOrNull() ?: false)
        }
        lifecycleScope.launch {
            settingsService.design.drop(1).collectLatest { it.apply() }
        }

        setContent {
            MinistryLogbookTheme(design) {
                val startDestination = if (showIntro) AppGraph.Intro.route else AppGraph.Home.route
                AppNavHost(startDestination)
            }
        }
    }
}
