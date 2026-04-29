package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

interface EntryDetailsRepository {
    fun get(id: Int): Flow<Entry?>

    suspend fun save(entry: Entry): Int

    suspend fun delete(entry: Entry)
}

interface HomeEntryRepository {
    fun getAllOfMonth(month: LocalDate): Flow<List<Entry>>

    fun getAllInRange(from: LocalDate, to: LocalDate): Flow<List<Entry>>

    fun getTransferredFrom(localDate: LocalDate): Flow<List<Entry>>

    suspend fun save(entry: Entry): Int

    suspend fun delete(entry: Entry)
}

class EntryRepository(private val entryDao: EntryDao, private val databaseChangeNotifier: DatabaseChangeNotifier) :
    EntryDetailsRepository,
    HomeEntryRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun get(id: Int) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        entryDao.get(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllOfMonth(month: LocalDate) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        entryDao.getAllOfMonth(month.year, month.month.ordinal + 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllInRange(from: LocalDate, to: LocalDate) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        entryDao.getAllInRange(from.year, from.month.ordinal + 1, to.year, to.month.ordinal + 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTransferredFrom(localDate: LocalDate) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        entryDao.getTransferredFrom(
            localDate.year,
            localDate.month.ordinal + 1
        )
    }

    val latest
        @OptIn(ExperimentalCoroutinesApi::class)
        get() = databaseChangeNotifier.databaseChanges.flatMapLatest {
            entryDao.getLatest()
        }

    override suspend fun save(entry: Entry): Int = withContext(Dispatchers.IO) {
        entryDao.upsert(entry)
    }.first().toInt()

    override suspend fun delete(entry: Entry) {
        withContext(Dispatchers.IO) {
            entryDao.delete(entry)
        }
    }
}
