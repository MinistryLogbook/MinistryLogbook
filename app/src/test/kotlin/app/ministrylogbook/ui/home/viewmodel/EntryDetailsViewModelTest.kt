package app.ministrylogbook.ui.home.viewmodel

import app.ministrylogbook.MainDispatcherRule
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryDetailsRepository
import app.ministrylogbook.data.EntryDetailsSettings
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.Role
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun newEntry_initializesAtSelectedMonthAndSavesUpdatedValues() = runTest {
        val repository = FakeEntryDetailsRepository()
        val viewModel = viewModel(repository = repository)

        viewModel.update(
            datetime = LocalDateTime(2026, 5, 20, 0, 0),
            hours = 2,
            minutes = 30,
            type = EntryType.Ministry
        )
        viewModel.save()
        advanceUntilIdle()

        val saved = repository.saved.single()
        assertEquals(LocalDate(2026, 5, 20), saved.datetime.date)
        assertEquals(2, saved.hours)
        assertEquals(30, saved.minutes)
        assertEquals(EntryType.Ministry, saved.type)
    }

    @Test
    fun existingEntry_loadsFromRepositoryAndTracksChanges() = runTest {
        val existing = entry(id = 42, hours = 1, minutes = 15)
        val repository = FakeEntryDetailsRepository(existing)
        val viewModel = viewModel(id = 42, repository = repository)
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.entry.collect {} }
            launch { viewModel.hasChanges.collect {} }
        }

        advanceUntilIdle()
        assertEquals(existing, viewModel.entry.value)
        assertFalse(viewModel.hasChanges.value)

        viewModel.update(hours = 3)
        advanceUntilIdle()

        assertEquals(3, viewModel.entry.value.hours)
        assertTrue(viewModel.hasChanges.value)
        collectJob.cancel()
    }

    @Test
    fun save_existingCreditEntryClearsDeprecatedReportFields() = runTest {
        val existing = entry(
            id = 7,
            placements = 4,
            videoShowings = 5,
            returnVisits = 6
        )
        val repository = FakeEntryDetailsRepository(existing)
        val viewModel = viewModel(id = 7, repository = repository)

        advanceUntilIdle()
        viewModel.update(type = EntryType.TheocraticAssignment)
        viewModel.save()
        advanceUntilIdle()

        val saved = repository.saved.single()
        assertEquals(EntryType.TheocraticAssignment, saved.type)
        assertEquals(0, saved.placements)
        assertEquals(0, saved.videoShowings)
        assertEquals(0, saved.returnVisits)
        assertEquals(existing.datetime, saved.datetime)
    }

    @Test
    fun delete_removesCurrentEntry() = runTest {
        val existing = entry(id = 11)
        val repository = FakeEntryDetailsRepository(existing)
        val viewModel = viewModel(id = 11, repository = repository)

        advanceUntilIdle()
        viewModel.delete()
        advanceUntilIdle()

        assertEquals(listOf(existing), repository.deleted)
    }

    @Test
    fun settingsFlowsExposeRoleAndPrecisionMode() = runTest {
        val settings = FakeEntryDetailsSettings(
            initialRole = Role.RegularPioneer,
            initialPrecisionMode = true
        )
        val viewModel = viewModel(settings = settings)
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.role.collect {} }
            launch { viewModel.precisionMode.collect {} }
        }

        advanceUntilIdle()

        assertEquals(Role.RegularPioneer, viewModel.role.value)
        assertTrue(viewModel.precisionMode.value)
        collectJob.cancel()
    }

    private fun viewModel(
        month: LocalDate = LocalDate(2026, 4, 1),
        id: Int? = null,
        settings: FakeEntryDetailsSettings = FakeEntryDetailsSettings(),
        repository: FakeEntryDetailsRepository = FakeEntryDetailsRepository()
    ) = EntryDetailsViewModel(
        month = month,
        id = id,
        settingsDataStore = settings,
        _entryRepository = repository
    )

    private fun entry(
        id: Int = 0,
        hours: Int = 0,
        minutes: Int = 0,
        type: EntryType = EntryType.Ministry,
        placements: Int = 0,
        videoShowings: Int = 0,
        returnVisits: Int = 0,
        datetime: LocalDateTime = LocalDateTime(2026, 4, 10, 8, 15)
    ) = Entry(
        id = id,
        datetime = datetime,
        placements = placements,
        videoShowings = videoShowings,
        hours = hours,
        minutes = minutes,
        returnVisits = returnVisits,
        type = type
    )

    private class FakeEntryDetailsRepository(initialEntry: Entry? = null) : EntryDetailsRepository {
        private val entry = MutableStateFlow(initialEntry)
        val saved = mutableListOf<Entry>()
        val deleted = mutableListOf<Entry>()

        override fun get(id: Int): Flow<Entry?> = entry

        override suspend fun save(entry: Entry): Int {
            saved += entry
            this.entry.value = entry
            return entry.id
        }

        override suspend fun delete(entry: Entry) {
            deleted += entry
            this.entry.value = null
        }
    }

    private class FakeEntryDetailsSettings(initialRole: Role = Role.Publisher, initialPrecisionMode: Boolean = false) :
        EntryDetailsSettings {
        override val role = MutableStateFlow(initialRole)
        override val precisionMode = MutableStateFlow(initialPrecisionMode)
    }
}
