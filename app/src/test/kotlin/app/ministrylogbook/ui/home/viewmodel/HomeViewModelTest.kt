package app.ministrylogbook.ui.home.viewmodel

import android.app.Application
import android.net.TestUri
import android.net.Uri
import app.ministrylogbook.MainDispatcherRule
import app.ministrylogbook.data.BibleStudy
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.HomeBibleStudyRepository
import app.ministrylogbook.data.HomeEntryRepository
import app.ministrylogbook.data.HomeMonthlyInformationRepository
import app.ministrylogbook.data.HomeSettings
import app.ministrylogbook.data.MonthlyInformation
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.services.HomeBackupService
import app.ministrylogbook.shared.services.Metadata
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_combinesEntriesSettingsAndMonthlyInformation() = runTest {
        val month = LocalDate(2026, 4, 1)
        val entries = FakeHomeEntryRepository(
            entry(LocalDateTime(2026, 4, 3, 9, 0), hours = 2),
            entry(LocalDateTime(2026, 3, 10, 9, 0), hours = 1),
            entry(
                datetime = LocalDateTime(2026, 5, 1, 0, 0),
                minutes = 30,
                type = EntryType.Transfer,
                transferredFrom = LocalDateTime(2026, 4, 30, 0, 0)
            )
        )
        val monthlyInfo = FakeHomeMonthlyInformationRepository(
            MonthlyInformation(month = month, goal = 5),
            MonthlyInformation(month = month.minus(DatePeriod(months = 1)), reportSent = false)
        )
        val settings = FakeHomeSettings(
            role = Role.RegularPioneer,
            pioneerSince = LocalDate(2026, 1, 1)
        )
        val viewModel = viewModel(
            month = month,
            entries = entries,
            monthlyInformation = monthlyInfo,
            settings = settings
        )
        val collectJob = collectState(viewModel)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isLoaded)
        assertEquals(5, state.goal)
        assertTrue(state.hasGoal == true)
        assertEquals(50, state.roleGoal)
        assertEquals(Role.RegularPioneer, state.role)
        assertEquals(1, state.entries.size)
        assertEquals(1, state.entriesLastMonth.size)
        assertEquals(2, state.entriesInServiceYear.size)
        assertEquals(30, state.transferred.single().minutes)
        assertEquals(1, state.rest.hours)
        assertEquals(30, state.rest.minutes)
        assertFalse(state.lastMonthReportSent!!)
        assertEquals(LocalDate(2026, 1, 1), state.beginOfPioneeringInServiceYear)
        collectJob.cancel()
    }

    @Test
    fun transferIntents_saveTransferEntriesAndUndoDeletesTransfer() = runTest {
        val entries = FakeHomeEntryRepository()
        val viewModel = viewModel(entries = entries)
        val collectJob = collectState(viewModel)

        viewModel.dispatch(HomeIntent.TransferToTextMonth(minutes = 45))
        viewModel.dispatch(HomeIntent.TransferFromLastMonth(minutes = 20))
        advanceUntilIdle()

        val transferToNextMonth = entries.saved[0]
        assertEquals(LocalDateTime(2026, 5, 1, 0, 0), transferToNextMonth.datetime)
        assertEquals(LocalDateTime(2026, 4, 30, 0, 0), transferToNextMonth.transferredFrom)
        assertEquals(EntryType.Transfer, transferToNextMonth.type)
        assertEquals(45, transferToNextMonth.minutes)

        val transferFromLastMonth = entries.saved[1]
        assertEquals(LocalDateTime(2026, 4, 1, 0, 0), transferFromLastMonth.datetime)
        assertEquals(LocalDateTime(2026, 3, 31, 0, 0), transferFromLastMonth.transferredFrom)
        assertEquals(20, transferFromLastMonth.minutes)

        viewModel.dispatch(HomeIntent.UndoTransfer(transferToNextMonth))
        advanceUntilIdle()

        assertEquals(listOf(transferToNextMonth), entries.deleted)
        collectJob.cancel()
    }

    @Test
    fun bibleStudyIntents_createUpdateDeleteAndInitialTransfer() = runTest {
        val month = LocalDate(2026, 4, 1)
        val bibleStudies = FakeHomeBibleStudyRepository(
            BibleStudy(id = 1, name = "Existing", month = month.minus(DatePeriod(months = 1)))
        )
        val monthlyInfo = FakeHomeMonthlyInformationRepository(MonthlyInformation(month = month))
        val viewModel = viewModel(
            month = month,
            bibleStudies = bibleStudies,
            monthlyInformation = monthlyInfo
        )
        val collectJob = collectState(viewModel)

        advanceUntilIdle()

        assertEquals(month.minus(DatePeriod(months = 1)) to month, bibleStudies.transfers.single())
        assertTrue(monthlyInfo.get(month).bibleStudiesTransferred)
        assertEquals(BibleStudy(name = "Existing", month = month, checked = false), bibleStudies.saved.single())

        viewModel.dispatch(HomeIntent.CreateBibleStudy("New"))
        advanceUntilIdle()
        assertEquals(BibleStudy(name = "New", month = month), bibleStudies.saved.last())

        val study = BibleStudy(id = 7, name = "New", month = month, checked = false)
        viewModel.dispatch(HomeIntent.CheckBibleStudy(study))
        viewModel.dispatch(HomeIntent.UncheckBibleStudy(study.copy(checked = true)))
        viewModel.dispatch(HomeIntent.DeleteBibleStudy(study))
        advanceUntilIdle()

        assertEquals(study.copy(checked = true), bibleStudies.saved[bibleStudies.saved.lastIndex - 1])
        assertEquals(study, bibleStudies.saved.last())
        assertEquals(listOf(study), bibleStudies.deleted)
        collectJob.cancel()
    }

    @Test
    fun dismissHints_updateMonthlyInformation() = runTest {
        val month = LocalDate(2026, 4, 1)
        val lastMonth = month.minus(DatePeriod(months = 1))
        val monthlyInfo = FakeHomeMonthlyInformationRepository(
            MonthlyInformation(month = month),
            MonthlyInformation(month = lastMonth)
        )
        val viewModel = viewModel(month = month, monthlyInformation = monthlyInfo)
        val collectJob = collectState(viewModel)

        viewModel.dispatch(HomeIntent.DismissBibleStudyHint)
        viewModel.dispatch(HomeIntent.DismissSendReportHint)
        advanceUntilIdle()

        assertTrue(monthlyInfo.get(month).dismissedBibleStudiesHint)
        assertTrue(monthlyInfo.get(lastMonth).reportSent)
        collectJob.cancel()
    }

    @Test
    fun goalParties_emitWhenProgressCrossesGoalAndCanBeDismissed() = runTest {
        val month = LocalDate(2026, 4, 1)
        val entries = FakeHomeEntryRepository(entry(LocalDateTime(2026, 4, 2, 9, 0), hours = 1))
        val monthlyInfo = FakeHomeMonthlyInformationRepository(MonthlyInformation(month = month, goal = 2))
        val viewModel = viewModel(month = month, entries = entries, monthlyInformation = monthlyInfo)
        val collectJob = collectState(viewModel)

        advanceUntilIdle()
        assertTrue(viewModel.state.value.monthlyParties.isEmpty())

        entries.save(entry(LocalDateTime(2026, 4, 3, 9, 0), hours = 1))
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.monthlyParties.size)

        viewModel.dispatch(HomeIntent.MonthlyPartyFinished)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.monthlyParties.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun yearlyParties_emitWhenProgressCrossesYearlyGoalAndSuppressMonthlyParties() = runTest {
        val month = LocalDate(2023, 8, 1)
        val entries = FakeHomeEntryRepository(entry(LocalDateTime(2023, 8, 2, 9, 0), hours = 29))
        val monthlyInfo = FakeHomeMonthlyInformationRepository(
            MonthlyInformation(month = month, goal = 30),
            MonthlyInformation(month = month.minus(DatePeriod(months = 1)))
        )
        val settings = FakeHomeSettings(
            role = Role.AuxiliaryPioneer,
            pioneerSince = month
        )
        val viewModel = viewModel(
            month = month,
            entries = entries,
            monthlyInformation = monthlyInfo,
            settings = settings
        )
        val collectJob = collectState(viewModel)

        advanceUntilIdle()
        assertTrue(viewModel.state.value.yearlyParties.isEmpty())

        entries.save(entry(LocalDateTime(2023, 8, 3, 9, 0), hours = 1))
        advanceUntilIdle()

        assertEquals(30, viewModel.state.value.yearlyGoal)
        assertEquals(2, viewModel.state.value.yearlyParties.size)
        assertTrue(viewModel.state.value.monthlyParties.isEmpty())

        viewModel.dispatch(HomeIntent.YearlyPartyFinished)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.yearlyParties.isEmpty())
        assertTrue(viewModel.state.value.monthlyParties.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun importBackup_withSelectedFile_setsImportFinishedWhenImportSucceeds() = runTest {
        val uri = TestUri("content://backup")
        val backup = FakeHomeBackupService(metadata = metadata(), isValid = true, importResult = true)
        val viewModel = viewModel(uri = uri, backup = backup)
        val collectJob = collectState(viewModel)

        advanceUntilIdle()

        assertEquals(uri, viewModel.state.value.selectedBackupFile?.uri)
        assertTrue(viewModel.state.value.isBackupValid)

        viewModel.dispatch(HomeIntent.ImportBackup)
        advanceUntilIdle()

        assertEquals(listOf(uri), backup.imported)
        assertTrue(viewModel.state.value.importFinished)
        collectJob.cancel()
    }

    @Test
    fun dismissImportBackup_clearsSelectedBackupFile() = runTest {
        val uri = TestUri("content://backup")
        val backup = FakeHomeBackupService(metadata = metadata(), isValid = true)
        val viewModel = viewModel(uri = uri, backup = backup)
        val collectJob = collectState(viewModel)

        advanceUntilIdle()
        assertEquals(uri, viewModel.state.value.selectedBackupFile?.uri)

        viewModel.dispatch(HomeIntent.DismissImportBackup)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.selectedBackupFile)
        assertFalse(viewModel.state.value.isBackupValid)
        collectJob.cancel()
    }

    @Test
    fun importBackup_whenImportFails_doesNotFinishAndShowsInvalidBackupMessage() = runTest {
        val uri = TestUri("content://backup")
        val backup = FakeHomeBackupService(metadata = metadata(), isValid = true, importResult = false)
        val messageNotifier = FakeHomeMessageNotifier()
        val viewModel = viewModel(
            uri = uri,
            backup = backup,
            messageNotifier = messageNotifier
        )
        val collectJob = collectState(viewModel)

        advanceUntilIdle()
        viewModel.dispatch(HomeIntent.ImportBackup)
        advanceUntilIdle()

        assertEquals(listOf(uri), backup.imported)
        assertFalse(viewModel.state.value.importFinished)
        assertEquals(1, messageNotifier.invalidBackupMessages)
        collectJob.cancel()
    }

    private fun TestScope.collectState(viewModel: HomeViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

    private fun viewModel(
        month: LocalDate = LocalDate(2026, 4, 1),
        uri: Uri? = null,
        entries: FakeHomeEntryRepository = FakeHomeEntryRepository(),
        backup: FakeHomeBackupService = FakeHomeBackupService(),
        bibleStudies: FakeHomeBibleStudyRepository = FakeHomeBibleStudyRepository(),
        monthlyInformation: FakeHomeMonthlyInformationRepository = FakeHomeMonthlyInformationRepository(
            MonthlyInformation(month = month),
            MonthlyInformation(month = month.minus(DatePeriod(months = 1)))
        ),
        settings: FakeHomeSettings = FakeHomeSettings(),
        messageNotifier: FakeHomeMessageNotifier = FakeHomeMessageNotifier()
    ) = HomeViewModel(
        month = month,
        _uri = uri,
        _application = Application(),
        _entryRepository = entries,
        _backupService = backup,
        _bibleStudyRepository = bibleStudies,
        _monthlyInformationRepository = monthlyInformation,
        settingsService = settings,
        _messageNotifier = messageNotifier
    )

    private fun entry(
        datetime: LocalDateTime,
        hours: Int = 0,
        minutes: Int = 0,
        type: EntryType = EntryType.Ministry,
        transferredFrom: LocalDateTime? = null
    ) = Entry(
        datetime = datetime,
        hours = hours,
        minutes = minutes,
        type = type,
        transferredFrom = transferredFrom
    )

    private fun metadata() = Metadata(
        version = 1,
        datetime = LocalDateTime(2026, 4, 1, 12, 0),
        role = Role.Publisher,
        startOfPioneering = null,
        name = "Publisher",
        design = Design.System,
        precisionMode = false,
        sendReportReminder = true
    )

    private class FakeHomeEntryRepository(vararg initialEntries: Entry) : HomeEntryRepository {
        private val entries = MutableStateFlow(initialEntries.toList())
        val saved = mutableListOf<Entry>()
        val deleted = mutableListOf<Entry>()

        override fun getAllOfMonth(month: LocalDate): Flow<List<Entry>> = entries.map { entries ->
            entries.filter { it.datetime.date.year == month.year && it.datetime.date.month == month.month }
        }

        override fun getAllInRange(from: LocalDate, to: LocalDate): Flow<List<Entry>> = entries.map { entries ->
            entries.filter { it.datetime.date >= from && it.datetime.date <= to }
        }

        override fun getTransferredFrom(localDate: LocalDate): Flow<List<Entry>> = entries.map { entries ->
            entries.filter {
                it.transferredFrom?.date?.let { date ->
                    date.year == localDate.year && date.month == localDate.month
                } == true
            }
        }

        override suspend fun save(entry: Entry): Int {
            saved += entry
            entries.value += entry
            return entry.id
        }

        override suspend fun delete(entry: Entry) {
            deleted += entry
            entries.value -= entry
        }
    }

    private class FakeHomeBibleStudyRepository(vararg initialStudies: BibleStudy) : HomeBibleStudyRepository {
        private val bibleStudies = MutableStateFlow(initialStudies.toList())
        val saved = mutableListOf<BibleStudy>()
        val deleted = mutableListOf<BibleStudy>()
        val transfers = mutableListOf<Pair<LocalDate, LocalDate>>()

        override fun getAllOfMonth(month: LocalDate): Flow<List<BibleStudy>> = bibleStudies.map { studies ->
            studies.filter { it.month.year == month.year && it.month.month == month.month }
        }

        override suspend fun transfer(fromMonth: LocalDate, toMonth: LocalDate) {
            transfers += fromMonth to toMonth
            bibleStudies.value
                .filter { it.month.year == fromMonth.year && it.month.month == fromMonth.month }
                .forEach { save(BibleStudy(name = it.name, month = toMonth, checked = false)) }
        }

        override suspend fun save(bibleStudy: BibleStudy): Long {
            saved += bibleStudy
            bibleStudies.value += bibleStudy
            return bibleStudy.id.toLong()
        }

        override suspend fun delete(bibleStudy: BibleStudy) {
            deleted += bibleStudy
            bibleStudies.value -= bibleStudy
        }
    }

    private class FakeHomeMonthlyInformationRepository(vararg initialInformation: MonthlyInformation) :
        HomeMonthlyInformationRepository {
        private val information = initialInformation.associate { it.month to MutableStateFlow(it) }.toMutableMap()
        val saved = mutableListOf<MonthlyInformation>()

        override fun getOfMonth(month: LocalDate): Flow<MonthlyInformation> = flow(month)

        override suspend fun save(info: MonthlyInformation): Long {
            saved += info
            flow(info.month).value = info
            return info.id.toLong()
        }

        fun get(month: LocalDate): MonthlyInformation = flow(month).value

        private fun flow(month: LocalDate) = information.getOrPut(month) {
            MutableStateFlow(MonthlyInformation(month = month))
        }
    }

    private class FakeHomeSettings(role: Role = Role.Publisher, pioneerSince: LocalDate? = null) : HomeSettings {
        override val role = MutableStateFlow(role)
        override val pioneerSince = MutableStateFlow(pioneerSince)
        override val roleGoal = MutableStateFlow(role.goal)
    }

    private class FakeHomeBackupService(
        private val metadata: Metadata? = null,
        private val isValid: Boolean = false,
        private val importResult: Boolean = true
    ) : HomeBackupService {
        val imported = mutableListOf<Uri>()

        override suspend fun importBackup(uri: Uri): Boolean {
            imported += uri
            return importResult
        }

        override fun getBackupMetadata(uri: Uri): Metadata? = metadata

        override fun validateBackup(uri: Uri): Boolean = isValid
    }

    private class FakeHomeMessageNotifier : HomeMessageNotifier {
        var invalidBackupMessages = 0

        override fun showInvalidBackupMessage() {
            invalidBackupMessages += 1
        }
    }
}
