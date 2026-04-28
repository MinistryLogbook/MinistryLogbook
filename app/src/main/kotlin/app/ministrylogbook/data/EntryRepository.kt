package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class EntryRepository(private val entryDao: EntryDao) {

    fun get(id: Int) = entryDao.get(id)

    fun getAllOfMonth(month: LocalDate) = entryDao.getAllOfMonth(month.year, month.month.ordinal + 1)

    fun getAllInRange(from: LocalDate, to: LocalDate) =
        entryDao.getAllInRange(from.year, from.month.ordinal + 1, to.year, to.month.ordinal + 1)

    fun getTransferredFrom(localDate: LocalDate) = entryDao.getTransferredFrom(
        localDate.year,
        localDate.month.ordinal + 1
    )

    val latest: Flow<Entry?>
        get() = entryDao.getLatest()

    suspend fun save(entry: Entry): Int = withContext(Dispatchers.IO) {
        entryDao.upsert(entry)
    }.first().toInt()

    suspend fun delete(entry: Entry) {
        withContext(Dispatchers.IO) {
            entryDao.delete(entry)
        }
    }
}
