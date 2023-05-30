package app.ministrylogbook

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.SettingsDataStore
import app.ministrylogbook.ui.AppNavHost
import app.ministrylogbook.ui.theme.MinistryLogbookTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val settingsDataStore = SettingsDataStore(this)
        val design = runBlocking {
            settingsDataStore.design.firstOrNull() ?: Design.System
        }

        setContent {
            MinistryLogbookTheme(design) {
                AppNavHost()
            }
        }
    }
}
