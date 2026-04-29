package app.ministrylogbook.ui

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.room.RoomDatabase
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ministrylogbook.MainActivity
import app.ministrylogbook.R
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.BibleStudy
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.ui.shared.ComposeTestActivity
import app.ministrylogbook.ui.theme.MinistryLogbookTheme
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
class AppFlowTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database: AppDatabase
        get() = GlobalContext.get().get()
    private val settingsService: SettingsService
        get() = GlobalContext.get().get()

    @Before
    fun resetState() = runBlocking {
        database.clearAllTablesOnIo()
        settingsService.setIntroShown()
        settingsService.setName("")
        settingsService.setRole(app.ministrylogbook.data.Role.Publisher)
        settingsService.setPioneerSince(null)
        settingsService.setDesign(Design.Light)
        settingsService.setUseSystemColors(false)
        settingsService.setPrecisionMode(false)
        settingsService.setSendReportReminders(false)
    }

    @Test
    fun onboardingFlow_fromWelcomeThroughSetupCompletion() {
        runBlocking {
            val userName = "Flow User"

            ActivityScenario.launch(ComposeTestActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.setContent {
                        MinistryLogbookTheme(design = Design.Light) {
                            AppNavHost(startDestination = AppGraph.Intro.route)
                        }
                    }
                }

                compose.waitUntilExists(context.getString(R.string.start_now))
                compose.onNodeWithText(context.getString(R.string.start_now)).performClick()
                compose.onAllNodes(hasSetTextAction())[0].performTextInput(userName)
                compose.onNodeWithContentDescription(context.getString(R.string.next)).performClick()

                compose.onNodeWithText(context.getString(R.string.intro_role_title, userName)).assertIsDisplayed()
                compose.onNodeWithContentDescription(context.getString(R.string.next)).performClick()
                compose.onNodeWithText(context.getString(R.string.intro_goal_title)).assertIsDisplayed()
                compose.onNodeWithContentDescription(context.getString(R.string.next)).performClick()
                compose.onNodeWithText(context.getString(R.string.setup_reminders_title)).assertIsDisplayed()
                compose.waitUntilExists(context.getString(R.string.ready))
                compose.onNodeWithText(context.getString(R.string.ready)).performClick()

                compose.onNodeWithText(context.getString(R.string.time)).assertIsDisplayed()
            }

            assertEquals(userName, settingsService.name.first())
            assertTrue(settingsService.introShown.first())
        }
    }

    @Test
    fun criticalFlow_onboardingEntryCreationAndReportRendering() {
        runBlocking {
            val userName = "Complete Flow User"
            settingsService.setName("")

            ActivityScenario.launch(ComposeTestActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.setContent {
                        MinistryLogbookTheme(design = Design.Light) {
                            AppNavHost(startDestination = AppGraph.Intro.route)
                        }
                    }
                }

                compose.waitUntilExists(context.getString(R.string.start_now))
                compose.onNodeWithText(context.getString(R.string.start_now)).performClick()
                compose.onAllNodes(hasSetTextAction())[0].performTextInput(userName)
                compose.onNodeWithContentDescription(context.getString(R.string.next)).performClick()
                compose.onNodeWithContentDescription(context.getString(R.string.next)).performClick()
                compose.onNodeWithContentDescription(context.getString(R.string.next)).performClick()
                compose.waitUntilExists(context.getString(R.string.ready))
                compose.onNodeWithText(context.getString(R.string.ready)).performClick()
                compose.waitUntilExists(context.getString(R.string.create_entry))

                compose.onNodeWithText(context.getString(R.string.create_entry)).performClick()
                compose.waitUntilExists(context.getString(R.string.save))
                compose.onAllNodesWithContentDescription("Plus")[0].performClick()
                compose.onNodeWithText(context.getString(R.string.save)).performClick()
                compose.waitUntil(timeoutMillis = 5_000) { runBlocking { latestEntry()?.hours == 1 } }

                compose.waitUntilExistsByContentDescription(
                    context.getString(R.string.share_field_service_report)
                )
                compose.onNodeWithContentDescription(
                    context.getString(R.string.share_field_service_report)
                ).performClick()
                compose.waitUntilExists(context.getString(R.string.text))
                compose.onNodeWithText(context.getString(R.string.text)).performClick()

                compose.waitUntilExists(context.getString(R.string.field_service_report).uppercase())
                compose.waitUntilExists("${context.getString(R.string.name_colon)} $userName")
                compose.waitUntilExists("${context.getString(R.string.hours_long_colon)} 1")
                compose.onNodeWithText(context.getString(R.string.comments)).assertIsDisplayed()
            }

            assertEquals(userName, settingsService.name.first())
            assertTrue(settingsService.introShown.first())
        }
    }

    @Test
    fun homeFlow_addEditAndDeleteMinistryEntry() {
        runBlocking {
            ActivityScenario.launch(MainActivity::class.java).use {
                compose.waitUntilExists(context.getString(R.string.create_entry))
                compose.onNodeWithText(context.getString(R.string.create_entry)).performClick()
                compose.waitUntilExists(context.getString(R.string.save))
                compose.onAllNodesWithContentDescription("Plus")[0].performClick()
                compose.onNodeWithText(context.getString(R.string.save)).performClick()

                compose.waitUntil(timeoutMillis = 5_000) { runBlocking { latestEntry()?.hours == 1 } }
                compose.waitUntilExists(context.getString(R.string.ministry))
                compose.onNodeWithText(context.getString(R.string.ministry)).performClick()
                compose.waitUntilExists(context.getString(R.string.save))
                compose.onAllNodesWithContentDescription("Plus")[0].performClick()
                compose.onNodeWithText(context.getString(R.string.save)).performClick()

                compose.waitUntil(timeoutMillis = 5_000) { runBlocking { latestEntry()?.hours == 2 } }
                compose.waitUntilExists(context.getString(R.string.ministry))
                compose.onNodeWithText(context.getString(R.string.ministry)).performClick()
                compose.onNodeWithContentDescription(context.getString(R.string.delete_entry)).performClick()
                compose.onNodeWithText(context.getString(R.string.yes)).performClick()

                compose.waitUntil(timeoutMillis = 5_000) { runBlocking { latestEntry() == null } }
            }
        }
    }

    @Test
    fun bibleStudyFlow_addCheckAndRemoveStudy() {
        runBlocking {
            val studyName = "Maria Example"

            ActivityScenario.launch(MainActivity::class.java).use {
                compose.onNodeWithText(context.getString(R.string.bible_studies_short)).performClick()
                compose.onNodeWithText(context.getString(R.string.add_bible_study)).performClick()
                compose.onAllNodes(hasSetTextAction())[0].performTextInput(studyName)
                compose.onNodeWithText(context.getString(R.string.add)).performClick()

                compose.waitUntil(timeoutMillis = 5_000) {
                    runBlocking { studies().singleOrNull()?.name == studyName }
                }
                compose.onNodeWithText(studyName).assertIsDisplayed()
                compose.onNodeWithContentDescription(studyName).performClick()
                compose.onNodeWithContentDescription(context.getString(R.string.delete_bible_study)).performClick()
                compose.onNodeWithText(context.getString(R.string.yes)).performClick()

                compose.waitUntil(timeoutMillis = 5_000) { runBlocking { studies().isEmpty() } }
            }
        }
    }

    @Test
    fun settingsFlow_updatesNameAndGoal() {
        runBlocking {
            val userName = "Settings User"
            val goal = 12

            ActivityScenario.launch(MainActivity::class.java).use {
                compose.waitUntilExistsByContentDescription(context.getString(R.string.settings))
                compose.onNodeWithContentDescription(context.getString(R.string.settings)).performClick()
                compose.waitUntilExists(context.getString(R.string.name))
                compose.onNodeWithText(context.getString(R.string.name)).performClick()
                compose.onAllNodes(hasSetTextAction())[0].performTextInput(userName)
                compose.onNodeWithContentDescription(context.getString(R.string.save)).performClick()
                compose.waitUntil(timeoutMillis = 5_000) {
                    runBlocking { settingsService.name.first() == userName }
                }

                compose.onNodeWithText(context.getString(R.string.monthly_goal)).performScrollTo().performClick()
                compose.onAllNodes(hasSetTextAction())[0].performTextClearance()
                compose.onAllNodes(hasSetTextAction())[0].performTextInput(goal.toString())
                compose.onNodeWithContentDescription(context.getString(R.string.save)).performClick()

                compose.waitUntil(timeoutMillis = 5_000) { runBlocking { currentMonthGoal() == goal } }
            }
        }
    }

    @Test
    fun shareFlow_rendersExpectedMonthTotals() {
        runBlocking {
            val userName = "Report User"
            val month = currentMonth()
            settingsService.setName(userName)
            database.entryDao().upsert(
                Entry(
                    datetime = month.atTime(10, 0),
                    hours = 2,
                    minutes = 30
                )
            )
            database.studyDao().upsert(BibleStudy(name = "Report Study", month = month, checked = true))

            ActivityScenario.launch(MainActivity::class.java).use {
                compose.waitUntilExistsByContentDescription(
                    context.getString(R.string.share_field_service_report)
                )
                compose.onNodeWithContentDescription(
                    context.getString(R.string.share_field_service_report)
                ).performClick()
                compose.waitUntilExists(context.getString(R.string.text))
                compose.onNodeWithText(context.getString(R.string.text)).performClick()

                compose.waitUntilExists(context.getString(R.string.field_service_report).uppercase())
                compose.waitUntilExists("${context.getString(R.string.name_colon)} $userName")
                compose.waitUntilExists("${context.getString(R.string.hours_long_colon)} 2")
                compose.waitUntilExists("${context.getString(R.string.bible_studies_long_colon)} 1")
                compose.onNodeWithText(context.getString(R.string.comments)).assertIsDisplayed()
            }
        }
    }

    private suspend fun latestEntry(): Entry? = database.entryDao().getLatest().first()

    private suspend fun studies(): List<BibleStudy> {
        val month = currentMonth()
        return database.studyDao().getAllOfMonth(month.year, month.month.ordinal + 1).first()
    }

    private suspend fun currentMonthGoal(): Int? {
        val month = currentMonth()
        return database.monthlyInformationDao().getOfMonth(month.year, month.month.ordinal + 1).first()?.goal
    }

    private fun currentMonth(): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return LocalDate(today.year, today.month, 1)
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilExists(text: String) {
        waitUntil(timeoutMillis = 15_000) {
            onAllNodes(
                androidx.compose.ui.test.hasText(text, substring = true),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilExistsByContentDescription(
        contentDescription: String
    ) {
        waitUntil(timeoutMillis = 15_000) {
            onAllNodes(
                androidx.compose.ui.test.hasContentDescription(contentDescription),
                useUnmergedTree = true
            )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private suspend fun RoomDatabase.clearAllTablesOnIo() {
        withContext(Dispatchers.IO) {
            clearAllTables()
        }
    }
}
