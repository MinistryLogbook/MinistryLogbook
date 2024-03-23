package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyRepository(private val studyDao: StudyDao) {

    fun get(id: Int) = studyDao.get(id)

    fun getAll() = studyDao.getAll()

    suspend fun save(study: Study): Int {
        return withContext(Dispatchers.IO) {
            studyDao.upsert(study)
        }.first().toInt()
    }

    suspend fun delete(study: Study) {
        withContext(Dispatchers.IO) {
            studyDao.delete(study)
        }
    }
}
