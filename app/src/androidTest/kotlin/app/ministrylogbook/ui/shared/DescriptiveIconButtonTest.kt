package app.ministrylogbook.ui.shared

import androidx.activity.compose.setContent
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ministrylogbook.R
import app.ministrylogbook.data.Design
import app.ministrylogbook.ui.theme.MinistryLogbookTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DescriptiveIconButtonTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun longPress_showsDescriptionTooltip() {
        val description = "Share report"

        ActivityScenario.launch(ComposeTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    MinistryLogbookTheme(design = Design.Light) {
                        DescriptiveIconButton(description = description) {
                            Icon(
                                painterResource(R.drawable.ic_share),
                                contentDescription = description
                            )
                        }
                    }
                }
            }

            compose.onNodeWithContentDescription(description)
                .assertIsDisplayed()
                .performTouchInput { longClick() }

            compose.onNodeWithText(description).assertIsDisplayed()
        }
    }
}
