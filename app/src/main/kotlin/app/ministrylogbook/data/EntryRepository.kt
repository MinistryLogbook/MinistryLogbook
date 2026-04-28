package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class EntryRepository(private val entryDao: EntryDao, private val databaseChangeNotifier: DatabaseChangeNotifier) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun get(id: Int) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        entryDao.get(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllOfMonth(month: LocalDate) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        entryDao.getAllOfMonth(month.year, month.month.ordinal + 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllInRange(from: LocalDate, to: LocalDate) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        entryDao.getAllInRange(from.year, from.month.ordinal + 1, to.year, to.month.ordinal + 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransferredFrom(localDate: LocalDate) = databaseChangeNotifier.databaseChanges.flatMapLatest {
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

    suspend fun save(entry: Entry): Int = withContext(Dispatchers.IO) {
        entryDao.upsert(entry)
    }.first().toInt()

    suspend fun delete(entry: Entry) {
        withContext(Dispatchers.IO) {
            entryDao.delete(entry)
        }
    }
}
