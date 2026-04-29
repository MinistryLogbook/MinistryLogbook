package app.ministrylogbook.ui.settings.viewmodel

import app.ministrylogbook.MainDispatcherRule
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.MonthlyInformation
import app.ministrylogbook.data.Role
import app.ministrylogbook.ui.intro.viewmodel.FakeMonthlyInformationRepository
import app.ministrylogbook.ui.intro.viewmodel.FakeReminderScheduler
import app.ministrylogbook.ui.intro.viewmodel.FakeUserSettings
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_exposesSettingsAndFallsBackToRoleGoal() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val settings = FakeUserSettings(
            name = "Ada",
            role = Role.RegularPioneer,
            pioneerSince = LocalDate(2026, 1, 1),
            design = Design.Dark,
            useSystemColors = true,
            precisionMode = true,
            sendReportReminder = true
        )
        val monthlyInfo = FakeMonthlyInformationRepository(MonthlyInformation(month = today))
        val viewModel = SettingsViewModel(settings, monthlyInfo, FakeReminderScheduler())
        val collectJob = collect(viewModel)

        advanceUntilIdle()

        assertEquals("Ada", viewModel.name.value)
        assertEquals(Design.Dark, viewModel.design.value)
        assertTrue(viewModel.useSystemColors.value)
        assertEquals(Role.RegularPioneer, viewModel.role.value)
        assertEquals(LocalDate(2026, 1, 1), viewModel.pioneerSince.value)
        assertEquals(Role.RegularPioneer.goal, viewModel.roleGoal.value)
        assertEquals(Role.RegularPioneer.goal, viewModel.goal.value)
        assertTrue(viewModel.precisionMode.value)
        assertTrue(viewModel.sendReportReminder.value)
        collectJob.cancel()
    }

    @Test
    fun setters_updatePreferencesAndReminderScheduling() = runTest {
        val settings = FakeUserSettings()
        val reminders = FakeReminderScheduler()
        val viewModel = SettingsViewModel(
            settings,
            FakeMonthlyInformationRepository(MonthlyInformation(month = currentDate())),
            reminders
        )
        val collectJob = collect(viewModel)

        viewModel.setName("Publisher")
        viewModel.setDesign(Design.Light)
        viewModel.setUseSystemColors(true)
        viewModel.setPrecisionMode(true)
        viewModel.setSendReportReminders(true)
        advanceUntilIdle()

        assertEquals("Publisher", settings.name.value)
        assertEquals(Design.Light, settings.design.value)
        assertTrue(settings.useSystemColors.value)
        assertTrue(settings.precisionMode.value)
        assertTrue(settings.sendReportReminder.value)
        assertEquals(1, reminders.scheduleCount)

        viewModel.setSendReportReminders(false)
        advanceUntilIdle()

        assertFalse(settings.sendReportReminder.value)
        assertEquals(1, reminders.cancelCount)
        collectJob.cancel()
    }

    @Test
    fun setGoal_savesManualGoalAndResetsRoleDefaultForPioneers() = runTest {
        val today = currentDate()
        val settings = FakeUserSettings(role = Role.RegularPioneer)
        val monthlyInfo = FakeMonthlyInformationRepository(MonthlyInformation(month = today))
        val viewModel = SettingsViewModel(settings, monthlyInfo, FakeReminderScheduler())
        val collectJob = collect(viewModel)

        viewModel.setGoal(42)
        advanceUntilIdle()

        assertEquals(42, monthlyInfo.get(today).goal)

        viewModel.setGoal(Role.RegularPioneer.goal)
        advanceUntilIdle()

        assertEquals(null, monthlyInfo.get(today).goal)
        collectJob.cancel()
    }

    @Test
    fun setGoal_withTargetMonth_updatesOnlyThatMonth() = runTest {
        val today = currentDate()
        val oldMonth = LocalDate(2026, 1, 1)
        val settings = FakeUserSettings()
        val monthlyInfo = FakeMonthlyInformationRepository(
            MonthlyInformation(month = today, goal = 7),
            MonthlyInformation(month = oldMonth, goal = 5)
        )
        val viewModel = SettingsViewModel(settings, monthlyInfo, FakeReminderScheduler(), oldMonth)
        val collectJob = collect(viewModel)

        viewModel.setGoal(12)
        advanceUntilIdle()

        assertEquals(12, monthlyInfo.get(oldMonth).goal)
        assertEquals(7, monthlyInfo.get(today).goal)
        collectJob.cancel()
    }

    @Test
    fun setRole_setsAndClearsPioneerStartDate() = runTest {
        val settings = FakeUserSettings(role = Role.Publisher)
        val viewModel = SettingsViewModel(
            settings,
            FakeMonthlyInformationRepository(MonthlyInformation(month = currentDate())),
            FakeReminderScheduler()
        )
        val collectJob = collect(viewModel)

        viewModel.setRole(Role.RegularPioneer)
        advanceUntilIdle()

        assertEquals(Role.RegularPioneer, settings.role.value)
        assertEquals(currentDate(), settings.pioneerSince.value)

        viewModel.setRole(Role.Publisher)
        advanceUntilIdle()

        assertEquals(Role.Publisher, settings.role.value)
        assertEquals(null, settings.pioneerSince.value)
        collectJob.cancel()
    }

    private fun currentDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun kotlinx.coroutines.test.TestScope.collect(viewModel: SettingsViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.name.collect {} }
            launch { viewModel.design.collect {} }
            launch { viewModel.useSystemColors.collect {} }
            launch { viewModel.role.collect {} }
            launch { viewModel.pioneerSince.collect {} }
            launch { viewModel.roleGoal.collect {} }
            launch { viewModel.manuallySetGoal.collect {} }
            launch { viewModel.goal.collect {} }
            launch { viewModel.precisionMode.collect {} }
            launch { viewModel.sendReportReminder.collect {} }
            launch { viewModel.lastBackup.collect {} }
        }
}
