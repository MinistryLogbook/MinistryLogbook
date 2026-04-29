package app.ministrylogbook.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceInstrumentedTest {
    private lateinit var database: AppDatabase

    private val entryDao: EntryDao
        get() = database.entryDao()

    private val bibleStudyDao: BibleStudyDao
        get() = database.studyDao()

    private val monthlyInformationDao: MonthlyInformationDao
        get() = database.monthlyInformationDao()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun entryDao_insertsUpdatesDeletesQueriesRangesAndKeepsLatestOrdering() = runTest {
        val march = entry(datetime = LocalDateTime(2026, 3, 31, 23, 45), minutes = 15)
        val april = entry(datetime = LocalDateTime(2026, 4, 10, 9, 0), hours = 1)
        val mayTransfer = entry(
            datetime = LocalDateTime(2026, 5, 1, 0, 0),
            minutes = 30,
            type = EntryType.Transfer,
            transferredFrom = LocalDateTime(2026, 4, 30, 0, 0)
        )

        val marchId = entryDao.upsert(march).single().toInt()
        val aprilId = entryDao.upsert(april).single().toInt()
        val mayTransferId = entryDao.upsert(mayTransfer).single().toInt()
        val savedApril = april.copy(id = aprilId)
        val savedMayTransfer = mayTransfer.copy(id = mayTransferId)

        assertEquals(savedApril, entryDao.get(aprilId).first())
        assertEquals(listOf(savedApril), entryDao.getAllOfMonth(2026, 4).first())
        assertEquals(
            listOf(savedApril, savedMayTransfer),
            entryDao.getAllInRange(2026, 4, 2026, 5).first()
        )
        assertEquals(listOf(savedMayTransfer), entryDao.getTransferredFrom(2026, 4).first())
        assertEquals(savedMayTransfer, entryDao.getLatest().first())

        val updatedApril = savedApril.copy(hours = 2, minutes = 45)
        entryDao.upsert(updatedApril)

        assertEquals(updatedApril, entryDao.get(aprilId).first())

        entryDao.delete(updatedApril)

        assertNull(entryDao.get(aprilId).first())
        assertEquals(listOf(mayTransfer.copy(id = mayTransferId)), entryDao.getAllInRange(2026, 4, 2026, 5).first())
        assertEquals(march.copy(id = marchId), entryDao.get(marchId).first())
    }

    @Test
    fun bibleStudyDao_insertsUpdatesDeletesAndKeepsCheckedStateInMonthQueries() = runTest {
        val active = BibleStudy(name = "Active", month = LocalDate(2026, 4, 1), checked = true)
        val inactive = BibleStudy(name = "Inactive", month = LocalDate(2026, 4, 1), checked = false)
        val otherMonth = BibleStudy(name = "Other", month = LocalDate(2026, 5, 1), checked = true)

        val activeId = bibleStudyDao.upsert(active).single().toInt()
        val inactiveId = bibleStudyDao.upsert(inactive).single().toInt()
        bibleStudyDao.upsert(otherMonth)
        val savedActive = active.copy(id = activeId)
        val savedInactive = inactive.copy(id = inactiveId)

        assertEquals(savedActive, bibleStudyDao.get(activeId).first())
        assertEquals(listOf(savedActive, savedInactive), bibleStudyDao.getAllOfMonth(2026, 4).first())
        assertEquals(listOf(savedActive), bibleStudyDao.getAllOfMonth(2026, 4).first().filter { it.checked })
        assertEquals(listOf(savedInactive), bibleStudyDao.getAllOfMonth(2026, 4).first().filterNot { it.checked })

        val updatedInactive = savedInactive.copy(name = "Updated", checked = true)
        bibleStudyDao.upsert(updatedInactive)

        assertEquals(listOf(savedActive, updatedInactive), bibleStudyDao.getAllOfMonth(2026, 4).first())

        bibleStudyDao.delete(savedActive)

        assertEquals(listOf(updatedInactive), bibleStudyDao.getAllOfMonth(2026, 4).first())
    }

    @Test
    fun monthlyInformationDao_insertsUpdatesDeletesAndQueriesSpecificMonths() = runTest {
        val april = MonthlyInformation(
            month = LocalDate(2026, 4, 1),
            goal = 15,
            reportComment = "Sent by phone"
        )
        val may = MonthlyInformation(month = LocalDate(2026, 5, 1), goal = 20)

        val aprilId = monthlyInformationDao.upsert(april).toInt()
        monthlyInformationDao.upsert(may)
        val savedApril = april.copy(id = aprilId)

        assertEquals(savedApril, monthlyInformationDao.getOfMonth(2026, 4).first())
        assertEquals(20, monthlyInformationDao.getOfMonth(2026, 5).first()?.goal)

        val updatedApril = savedApril.copy(reportSent = true, goal = 18)
        monthlyInformationDao.upsert(updatedApril)

        assertEquals(updatedApril, monthlyInformationDao.getOfMonth(2026, 4).first())

        monthlyInformationDao.delete(updatedApril)

        assertNull(monthlyInformationDao.getOfMonth(2026, 4).first())
        assertEquals(20, monthlyInformationDao.getOfMonth(2026, 5).first()?.goal)
    }

    @Test
    fun entryRepository_usesRealRoomDatabaseForSaveDeleteAndQueries() = runTest {
        val repository = EntryRepository(entryDao, DatabaseChangeNotifier())
        val april = entry(datetime = LocalDateTime(2026, 4, 3, 8, 0), hours = 2)
        val may = entry(datetime = LocalDateTime(2026, 5, 4, 9, 30), minutes = 45)

        val aprilId = repository.save(april)
        val mayId = repository.save(may)
        val savedApril = april.copy(id = aprilId)
        val savedMay = may.copy(id = mayId)

        assertEquals(savedApril, repository.get(aprilId).first())
        assertEquals(listOf(savedApril), repository.getAllOfMonth(LocalDate(2026, 4, 1)).first())
        assertEquals(
            listOf(savedApril, savedMay),
            repository.getAllInRange(LocalDate(2026, 4, 1), LocalDate(2026, 5, 1)).first()
        )
        assertEquals(savedMay, repository.latest.first())

        repository.delete(savedApril)

        assertNull(repository.get(aprilId).first())
    }

    @Test
    fun bibleStudyRepository_usesRealRoomDatabaseForSaveTransferAndDelete() = runTest {
        val repository = BibleStudyRepository(bibleStudyDao, DatabaseChangeNotifier())
        val april = BibleStudy(name = "Study", month = LocalDate(2026, 4, 1), checked = true)

        val aprilId = repository.save(april).toInt()
        val savedApril = april.copy(id = aprilId)

        assertEquals(savedApril, repository.get(aprilId).first())
        assertEquals(listOf(savedApril), repository.getAllOfMonth(LocalDate(2026, 4, 1)).first())

        repository.transfer(LocalDate(2026, 4, 1), LocalDate(2026, 5, 1))

        assertEquals(
            listOf(BibleStudy(id = aprilId + 1, name = "Study", month = LocalDate(2026, 5, 1), checked = false)),
            repository.getAllOfMonth(LocalDate(2026, 5, 1)).first()
        )

        repository.delete(savedApril)

        assertTrue(repository.getAllOfMonth(LocalDate(2026, 4, 1)).first().isEmpty())
    }

    @Test
    fun monthlyInformationRepository_usesRealRoomDatabaseForCreationUpdateAndSave() = runTest {
        val repository = MonthlyInformationRepository(monthlyInformationDao, DatabaseChangeNotifier())
        val march = MonthlyInformation(month = LocalDate(2026, 3, 1), goal = 12)
        val marchId = repository.save(march)

        assertEquals(march.copy(id = marchId.toInt()), repository.getOfMonth(LocalDate(2026, 3, 1)).first())

        val createdApril = repository.getOfMonth(LocalDate(2026, 4, 1)).first()

        assertEquals(LocalDate(2026, 4, 1), createdApril.month)
        assertEquals(12, createdApril.goal)

        repository.update(LocalDate(2026, 4, 1)) {
            it.copy(reportSent = true, reportComment = "Done")
        }

        assertEquals(
            createdApril.copy(reportSent = true, reportComment = "Done"),
            repository.getOfMonth(LocalDate(2026, 4, 1)).first()
        )
    }

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
}
