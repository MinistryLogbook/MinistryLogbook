package app.ministrylogbook.ui.home

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.os.ConfigurationCompat
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ministrylogbook.MainActivity
import app.ministrylogbook.R
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
import org.junit.Assert.assertEquals
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

            compose.waitUntil(timeoutMillis = 5_000) { expectedTitle != null }
            compose.onNodeWithText(expectedTitle!!).assertIsDisplayed()
        }
    }

    @Test
    fun toolbarMonthSelect_whenSelectedMonthClicked_doesNotSelectAgain() {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val selectedMonth = LocalDate(currentDate.year, currentDate.month, 1) - DatePeriod(months = 1)
        var monthTitle: String? = null
        var monthShortTitle: String? = null
        var selectCount = 0

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current).get(0)
                        ?: LocalLocale.current.platformLocale
                    val monthName = JavaMonth.of(selectedMonth.month.ordinal + 1)
                        .getDisplayName(TextStyle.FULL, locale)
                    monthTitle = if (selectedMonth.year != currentDate.year) {
                        "$monthName ${selectedMonth.year}"
                    } else {
                        monthName
                    }
                    monthShortTitle = activity.getString(selectedMonth.shortNameResourceId())

                    MinistryLogbookTheme(design = Design.Light) {
                        ToolbarMonthSelect(
                            selectedMonth = selectedMonth,
                            onSelect = {
                                selectCount += 1
                            }
                        )
                    }
                }
            }

            compose.waitUntil(timeoutMillis = 5_000) { monthTitle != null && monthShortTitle != null }
            compose.onNodeWithText(monthTitle!!).performClick()
            compose.onNodeWithText(monthShortTitle!!).performClick()

            compose.waitForIdle()
            assertEquals(0, selectCount)
        }
    }

    private fun LocalDate.shortNameResourceId() = when (month.ordinal + 1) {
        1 -> R.string.january_short
        2 -> R.string.february_short
        3 -> R.string.march_short
        4 -> R.string.april_short
        5 -> R.string.may_short
        6 -> R.string.june_short
        7 -> R.string.july_short
        8 -> R.string.august_short
        9 -> R.string.september_short
        10 -> R.string.october_short
        11 -> R.string.november_short
        else -> R.string.december_short
    }
}
