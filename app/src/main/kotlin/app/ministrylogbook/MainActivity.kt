package app.ministrylogbook

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI

class MainActivity : AppCompatActivity() {
    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsService = SettingsService(this)
        val design = runBlocking {
            settingsService.design.firstOrNull() ?: Design.System
        }
        val showIntro = runBlocking {
            !(settingsService.introShown.firstOrNull() ?: false)
        }
        val useSystemColors = runBlocking {
            settingsService.useSystemColors.firstOrNull() ?: false
        }
        lifecycleScope.launch {
            settingsService.design.drop(1).collectLatest {
                it.apply()
            }
        }
        lifecycleScope.launch {
            settingsService.useSystemColors.drop(1).collectLatest {
                recreate()
            }
        }

        setContent {
            KoinAndroidContext {
                MinistryLogbookTheme(design, useSystemColors) {
                    val startDestination = if (showIntro) AppGraph.Intro.route else AppGraph.Home.route
                    AppNavHost(startDestination)
                }
            }
        }
    }
}
