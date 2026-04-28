package app.ministrylogbook.ui.home

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.os.ConfigurationCompat
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ministrylogbook.MainActivity
import app.ministrylogbook.data.Design
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.theme.MinistryLogbookTheme
import java.time.Month as JavaMonth
import java.time.format.TextStyle
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomePageTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun toolbarMonth_usesHomeStateMonthImmediately() {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val selectedMonth = LocalDate(currentDate.year, currentDate.month, 1) - DatePeriod(months = 1)
        var expectedTitle: String? = null

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current).get(0)
                        ?: LocalLocale.current.platformLocale
                    val monthName = JavaMonth.of(selectedMonth.month.ordinal + 1)
                        .getDisplayName(TextStyle.FULL, locale)
                    expectedTitle = if (selectedMonth.year != currentDate.year) {
                        "$monthName ${selectedMonth.year}"
                    } else {
                        monthName
                    }

                    val navController = rememberNavController()
                    CompositionLocalProvider(LocalAppNavController provides navController) {
                        MinistryLogbookTheme(design = Design.Light) {
                            HomePage(state = HomeState(month = selectedMonth))
                        }
                    }
                }
            }

            compose.waitUntil { expectedTitle != null }
            compose.onNodeWithText(expectedTitle!!).assertIsDisplayed()
        }
    }
}
