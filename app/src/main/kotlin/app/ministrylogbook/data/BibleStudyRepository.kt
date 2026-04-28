package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class BibleStudyRepository(private val bibleStudyDao: BibleStudyDao) {
    fun get(id: Int) = bibleStudyDao.get(id)

    fun getAllOfMonth(month: LocalDate): Flow<List<BibleStudy>> =
        bibleStudyDao.getAllOfMonth(month.year, month.month.ordinal + 1)

    suspend fun transfer(fromMonth: LocalDate, toMonth: LocalDate) {
        withContext(Dispatchers.IO) {
            val bibleStudies = bibleStudyDao.getAllOfMonth(fromMonth.year, fromMonth.month.ordinal + 1).first()
            bibleStudies.forEach {
                save(BibleStudy(month = toMonth, name = it.name, checked = false))
            }
        }
    }

    suspend fun save(bibleStudy: BibleStudy): Long = withContext(Dispatchers.IO) {
        bibleStudyDao.upsert(bibleStudy)
    }.first()

    suspend fun delete(bibleStudy: BibleStudy) {
        withContext(Dispatchers.IO) {
            bibleStudyDao.delete(bibleStudy)
        }
    }
}
