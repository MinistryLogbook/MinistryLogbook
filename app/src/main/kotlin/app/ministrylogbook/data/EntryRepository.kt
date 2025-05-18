package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class EntryRepository(private val entryDao: EntryDao) {

    fun get(id: Int) = entryDao.get(id)

    fun getAllOfMonth(month: LocalDate) = entryDao.getAllOfMonth(month.year, month.monthNumber)

    fun getAllInRange(from: LocalDate, to: LocalDate) =
        entryDao.getAllInRange(from.year, from.monthNumber, to.year, to.monthNumber)

    fun getTransferredFrom(localDate: LocalDate) = entryDao.getTransferredFrom(localDate.year, localDate.monthNumber)

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
