package app.ministrylogbook.ui.intro

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ministrylogbook.MainActivity
import app.ministrylogbook.R
import app.ministrylogbook.data.Design
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.theme.MinistryLogbookTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomePageTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun welcomePage_showsLanguageAndRestoreBackupActions() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    val navController = rememberNavController()
                    CompositionLocalProvider(LocalAppNavController provides navController) {
                        MinistryLogbookTheme(design = Design.Light) {
                            WelcomePage()
                        }
                    }
                }
            }

            val context = androidx.test.core.app.ApplicationProvider
                .getApplicationContext<android.content.Context>()
            compose.onNodeWithContentDescription(context.getString(R.string.language)).assertIsDisplayed()
            compose.onNodeWithContentDescription(context.getString(R.string.restore_backup)).assertIsDisplayed()
        }
    }
}
