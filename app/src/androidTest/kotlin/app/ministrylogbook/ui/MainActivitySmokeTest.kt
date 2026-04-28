package app.ministrylogbook.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import app.ministrylogbook.MainActivity
import app.ministrylogbook.R
import app.ministrylogbook.data.SettingsService
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(maxSdkVersion = 36)
class MainActivitySmokeTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun mainActivity_rendersHomeNavigationAfterIntroCompleted() {
        val context = androidx.test.core.app.ApplicationProvider
            .getApplicationContext<android.content.Context>()
        runBlocking {
            SettingsService(context).setIntroShown()
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithText(context.getString(R.string.time)).assertIsDisplayed()
            compose.onNodeWithText(context.getString(R.string.bible_studies_short)).assertIsDisplayed()
            compose.onNodeWithText(context.getString(R.string.create_entry)).assertIsDisplayed()
            compose.onNodeWithText(context.getString(R.string.create_entry)).performClick()
            compose.onNodeWithText(context.getString(R.string.save)).assertIsDisplayed()
        }
    }
}
