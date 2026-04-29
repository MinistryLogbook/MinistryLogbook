package app.ministrylogbook.ui.intro.viewmodel

import app.ministrylogbook.MainDispatcherRule
import app.ministrylogbook.data.AppMonthlyInformationRepository
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.MonthlyInformation
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.UserSettings
import app.ministrylogbook.shared.services.ReminderScheduler
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntroViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_combinesSettingsAndCurrentMonthGoal() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val settings = FakeUserSettings(
            name = "Daniel",
            role = Role.RegularPioneer,
            pioneerSince = LocalDate(2026, 1, 1),
            sendReportReminder = true
        )
        val monthlyInfo = FakeMonthlyInformationRepository(MonthlyInformation(month = today))
        val viewModel = IntroViewModel(settings, monthlyInfo, FakeReminderScheduler())
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        advanceUntilIdle()

        assertEquals(
            IntroState(
                name = "Daniel",
                role = Role.RegularPioneer,
                pioneerSince = LocalDate(2026, 1, 1),
                reminders = true,
                goal = Role.RegularPioneer.goal
            ),
            viewModel.state.value
        )
        collectJob.cancel()
    }

    @Test
    fun dispatch_updatesOnboardingSettingsAndGoal() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val settings = FakeUserSettings()
        val monthlyInfo = FakeMonthlyInformationRepository(MonthlyInformation(month = today))
        val reminders = FakeReminderScheduler()
        val viewModel = IntroViewModel(settings, monthlyInfo, reminders)
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.dispatch(IntroIntent.NameChange("Ada"))
        viewModel.dispatch(IntroIntent.RoleChange(Role.AuxiliaryPioneer))
        viewModel.dispatch(IntroIntent.PioneerSinceSet(LocalDate(2026, 4, 1)))
        viewModel.dispatch(IntroIntent.RemindersToggle(true))
        viewModel.dispatch(IntroIntent.GoalChange(12))
        viewModel.dispatch(IntroIntent.Ready)
        advanceUntilIdle()

        assertEquals("Ada", settings.name.value)
        assertEquals(Role.AuxiliaryPioneer, settings.role.value)
        assertEquals(LocalDate(2026, 4, 1), settings.pioneerSince.value)
        assertTrue(settings.sendReportReminder.value)
        assertEquals(1, reminders.scheduleCount)
        assertEquals(12, monthlyInfo.get(today).goal)
        assertTrue(settings.introShown.value)

        viewModel.dispatch(IntroIntent.RemindersToggle(false))
        advanceUntilIdle()

        assertEquals(1, reminders.cancelCount)
        assertEquals(false, settings.sendReportReminder.value)
        collectJob.cancel()
    }
}

internal class FakeUserSettings(
    name: String = "",
    role: Role = Role.Publisher,
    pioneerSince: LocalDate? = null,
    design: Design = Design.System,
    useSystemColors: Boolean = false,
    precisionMode: Boolean = false,
    sendReportReminder: Boolean = false,
    lastBackup: LocalDateTime? = null,
    introShown: Boolean = false
) : UserSettings {
    override val name = MutableStateFlow(name)
    override val role = MutableStateFlow(role)
    override val pioneerSince = MutableStateFlow(pioneerSince)
    override val roleGoal = MutableStateFlow(role.goal)
    override val design = MutableStateFlow(design)
    override val useSystemColors = MutableStateFlow(useSystemColors)
    override val precisionMode = MutableStateFlow(precisionMode)
    override val sendReportReminder = MutableStateFlow(sendReportReminder)
    override val lastBackup = MutableStateFlow(lastBackup)
    override val introShown = MutableStateFlow(introShown)

    override suspend fun setIntroShown() {
        introShown.value = true
    }

    override suspend fun setPioneerSince(date: LocalDate?) {
        pioneerSince.value = date
    }

    override suspend fun setLastBackup(dateTime: LocalDateTime?) {
        lastBackup.value = dateTime
    }

    override suspend fun setRole(role: Role) {
        this.role.value = role
        roleGoal.value = role.goal
    }

    override suspend fun setName(name: String) {
        this.name.value = name
    }

    override suspend fun setDesign(design: Design) {
        this.design.value = design
    }

    override suspend fun setUseSystemColors(value: Boolean) {
        useSystemColors.value = value
    }

    override suspend fun setPrecisionMode(precisionMode: Boolean) {
        this.precisionMode.value = precisionMode
    }

    override suspend fun setSendReportReminders(value: Boolean) {
        sendReportReminder.value = value
    }
}

internal class FakeMonthlyInformationRepository(vararg initialInformation: MonthlyInformation) :
    AppMonthlyInformationRepository {
    private val information = initialInformation.associate { it.month to MutableStateFlow(it) }.toMutableMap()
    val saved = mutableListOf<MonthlyInformation>()

    override fun getOfMonth(month: LocalDate): Flow<MonthlyInformation> = flow(month)

    override suspend fun save(info: MonthlyInformation): Long {
        saved += info
        flow(info.month).value = info
        return info.id.toLong()
    }

    override suspend fun update(month: LocalDate, modify: (monthlyInfo: MonthlyInformation) -> MonthlyInformation) {
        val modified = modify(flow(month).value)
        save(modified)
    }

    fun get(month: LocalDate): MonthlyInformation = flow(month).value

    private fun flow(month: LocalDate) = information.getOrPut(month) {
        MutableStateFlow(MonthlyInformation(month = month))
    }
}

internal class FakeReminderScheduler : ReminderScheduler {
    var scheduleCount = 0
    var cancelCount = 0

    override fun scheduleReminder() {
        scheduleCount += 1
    }

    override fun cancelReminder() {
        cancelCount += 1
    }
}
