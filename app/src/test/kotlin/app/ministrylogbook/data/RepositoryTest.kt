package app.ministrylogbook.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryTest {
    @Test
    fun entryRepository_savesDeletesAndQueriesByMonthRangeAndTransferSource() = runTest {
        val dao = FakeEntryDao()
        val repository = EntryRepository(dao, DatabaseChangeNotifier())
        val april = entry(
            id = 1,
            datetime = LocalDateTime(2026, 4, 10, 9, 0),
            hours = 2
        )
        val mayTransfer = entry(
            id = 2,
            datetime = LocalDateTime(2026, 5, 1, 0, 0),
            minutes = 30,
            type = EntryType.Transfer,
            transferredFrom = LocalDateTime(2026, 4, 30, 0, 0)
        )
        val june = entry(id = 3, datetime = LocalDateTime(2026, 6, 1, 9, 0))

        assertEquals(1, repository.save(april))
        repository.save(mayTransfer)
        repository.save(june)

        assertEquals(april, repository.get(1).first())
        assertEquals(listOf(april), repository.getAllOfMonth(LocalDate(2026, 4, 1)).first())
        assertEquals(
            listOf(april, mayTransfer),
            repository.getAllInRange(LocalDate(2026, 4, 1), LocalDate(2026, 5, 31)).first()
        )
        assertEquals(listOf(mayTransfer), repository.getTransferredFrom(LocalDate(2026, 4, 1)).first())
        assertEquals(june, repository.latest.first())

        repository.delete(april)

        assertEquals(null, repository.get(1).first())
    }

    @Test
    fun bibleStudyRepository_savesDeletesAndTransfersUncheckedCopies() = runTest {
        val dao = FakeBibleStudyDao()
        val repository = BibleStudyRepository(dao, DatabaseChangeNotifier())
        val march = BibleStudy(id = 1, name = "Study", month = LocalDate(2026, 3, 1), checked = true)

        assertEquals(1L, repository.save(march))
        assertEquals(listOf(march), repository.getAllOfMonth(LocalDate(2026, 3, 1)).first())

        repository.transfer(LocalDate(2026, 3, 1), LocalDate(2026, 4, 1))

        assertEquals(
            listOf(BibleStudy(id = 2, name = "Study", month = LocalDate(2026, 4, 1), checked = false)),
            repository.getAllOfMonth(LocalDate(2026, 4, 1)).first()
        )

        repository.delete(march)

        assertTrue(repository.getAllOfMonth(LocalDate(2026, 3, 1)).first().isEmpty())
    }

    @Test
    fun monthlyInformationRepository_createsCurrentMonthFromLastGoalAndUpdatesExistingMonth() = runTest {
        val dao = FakeMonthlyInformationDao(
            MonthlyInformation(id = 7, month = LocalDate(2026, 3, 1), goal = 12)
        )
        val repository = MonthlyInformationRepository(dao, DatabaseChangeNotifier())

        val created = repository.getOfMonth(LocalDate(2026, 4, 1)).first()

        assertEquals(LocalDate(2026, 4, 1), created.month)
        assertEquals(12, created.goal)
        assertEquals(created, dao.getOfMonth(2026, 4).first())

        repository.update(LocalDate(2026, 4, 1)) {
            it.copy(reportSent = true)
        }

        assertEquals(true, repository.getOfMonth(LocalDate(2026, 4, 1)).first().reportSent)
    }

    private fun entry(
        id: Int,
        datetime: LocalDateTime,
        hours: Int = 0,
        minutes: Int = 0,
        type: EntryType = EntryType.Ministry,
        transferredFrom: LocalDateTime? = null
    ) = Entry(
        id = id,
        datetime = datetime,
        hours = hours,
        minutes = minutes,
        type = type,
        transferredFrom = transferredFrom
    )
}

private class FakeEntryDao(vararg initialEntries: Entry) : EntryDao {
    private val entries = MutableStateFlow(initialEntries.toList())
    private var nextId = (initialEntries.maxOfOrNull { it.id } ?: 0) + 1

    override fun get(id: Int): Flow<Entry?> = entries.map { list -> list.singleOrNull { it.id == id } }

    override fun getAllOfMonth(year: Int, month: Int): Flow<List<Entry>> = entries.map { list ->
        list.filter { it.datetime.year == year && it.datetime.month.ordinal + 1 == month }
    }

    override fun getAllInRange(fromYear: Int, fromMonth: Int, toYear: Int, toMonth: Int): Flow<List<Entry>> =
        entries.map { list ->
            list.filter {
                val value = it.datetime.year * 100 + it.datetime.month.ordinal + 1
                value >= fromYear * 100 + fromMonth && value <= toYear * 100 + toMonth
            }
        }

    override fun getTransferredFrom(year: Int, month: Int): Flow<List<Entry>> = entries.map { list ->
        list.filter {
            it.transferredFrom?.let { date ->
                date.year == year && date.month.ordinal + 1 == month
            } == true
        }
    }

    override fun getLatest(): Flow<Entry?> = entries.map { list -> list.maxByOrNull { it.id } }

    override suspend fun upsert(vararg entries: Entry): List<Long> = entries.map { entry ->
        val saved = if (entry.id == 0) entry.copy(id = nextId++) else entry
        nextId = maxOf(nextId, saved.id + 1)
        this.entries.value = this.entries.value.filterNot { it.id == saved.id } + saved
        saved.id.toLong()
    }

    override suspend fun delete(entry: Entry) {
        entries.value = entries.value.filterNot { it.id == entry.id }
    }
}

private class FakeBibleStudyDao(vararg initialStudies: BibleStudy) : BibleStudyDao {
    private val studies = MutableStateFlow(initialStudies.toList())
    private var nextId = (initialStudies.maxOfOrNull { it.id } ?: 0) + 1

    override fun get(id: Int): Flow<BibleStudy?> = studies.map { list -> list.singleOrNull { it.id == id } }

    override fun getAllOfMonth(year: Int, month: Int): Flow<List<BibleStudy>> = studies.map { list ->
        list.filter { it.month.year == year && it.month.month.ordinal + 1 == month }
    }

    override suspend fun upsert(vararg studies: BibleStudy): List<Long> = studies.map { study ->
        val saved = if (study.id == 0) study.copy(id = nextId++) else study
        nextId = maxOf(nextId, saved.id + 1)
        this.studies.value = this.studies.value.filterNot { it.id == saved.id } + saved
        saved.id.toLong()
    }

    override suspend fun delete(bibleStudy: BibleStudy) {
        studies.value = studies.value.filterNot { it.id == bibleStudy.id }
    }
}

private class FakeMonthlyInformationDao(vararg initialInformation: MonthlyInformation) : MonthlyInformationDao {
    private val information = MutableStateFlow(initialInformation.toList())
    private var nextId = (initialInformation.maxOfOrNull { it.id } ?: 0) + 1

    override fun getOfMonth(year: Int, month: Int): Flow<MonthlyInformation?> = information.map { list ->
        list.singleOrNull { it.month.year == year && it.month.month.ordinal + 1 == month }
    }

    override suspend fun upsert(info: MonthlyInformation): Long {
        val existing = information.value.singleOrNull {
            (it.id == info.id && info.id != 0) ||
                (it.month.year == info.month.year && it.month.month == info.month.month)
        }
        val saved = info.copy(id = existing?.id ?: if (info.id == 0) nextId++ else info.id)
        nextId = maxOf(nextId, saved.id + 1)
        information.value = information.value.filterNot { it.id == saved.id } + saved
        return saved.id.toLong()
    }

    override suspend fun delete(info: MonthlyInformation) {
        information.value = information.value.filterNot { it.id == info.id }
    }
}
