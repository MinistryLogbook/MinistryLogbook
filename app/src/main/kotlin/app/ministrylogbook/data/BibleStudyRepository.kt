package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

interface HomeBibleStudyRepository {
    fun getAllOfMonth(month: LocalDate): Flow<List<BibleStudy>>

    suspend fun transfer(fromMonth: LocalDate, toMonth: LocalDate)

    suspend fun save(bibleStudy: BibleStudy): Long

    suspend fun delete(bibleStudy: BibleStudy)
}

class BibleStudyRepository(
    private val bibleStudyDao: BibleStudyDao,
    private val databaseChangeNotifier: DatabaseChangeNotifier
) : HomeBibleStudyRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun get(id: Int) = databaseChangeNotifier.databaseChanges.flatMapLatest {
        bibleStudyDao.get(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllOfMonth(month: LocalDate): Flow<List<BibleStudy>> =
        databaseChangeNotifier.databaseChanges.flatMapLatest {
            bibleStudyDao.getAllOfMonth(month.year, month.month.ordinal + 1)
        }

    override suspend fun transfer(fromMonth: LocalDate, toMonth: LocalDate) {
        withContext(Dispatchers.IO) {
            val bibleStudies = bibleStudyDao.getAllOfMonth(fromMonth.year, fromMonth.month.ordinal + 1).first()
            bibleStudies.forEach {
                save(BibleStudy(month = toMonth, name = it.name, checked = false))
            }
        }
    }

    override suspend fun save(bibleStudy: BibleStudy): Long = withContext(Dispatchers.IO) {
        bibleStudyDao.upsert(bibleStudy)
    }.first()

    override suspend fun delete(bibleStudy: BibleStudy) {
        withContext(Dispatchers.IO) {
            bibleStudyDao.delete(bibleStudy)
        }
    }
}
