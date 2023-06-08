package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class EntryRepository(private val entryDao: EntryDao) {

    fun get(id: Int) = entryDao.get(id)

    fun getAllOfMonth(month: LocalDate) = entryDao.getAllOfMonth(month.year, month.monthNumber)

    fun getAllFrom(month: LocalDate) = entryDao.getAllFrom(month.year, month.monthNumber)

    fun getTransferredFrom(localDate: LocalDate) = entryDao.getTransferredFrom(localDate.year, localDate.monthNumber)

    suspend fun save(entry: Entry): Int {
        return withContext(Dispatchers.IO) {
            entryDao.upsert(entry)
        }.first().toInt()
    }

    suspend fun delete(entry: Entry) {
        withContext(Dispatchers.IO) {
            entryDao.delete(entry)
        }
    }
}
