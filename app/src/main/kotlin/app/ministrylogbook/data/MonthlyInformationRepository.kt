package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class MonthlyInformationRepository(private val monthlyInformationDao: MonthlyInformationDao) {

    fun getOfMonth(month: LocalDate): Flow<MonthlyInformation> {
        val lastMonth = month.minus(DatePeriod(months = 1))

        return combine(
            monthlyInformationDao.getOfMonth(month.year, month.monthNumber),
            monthlyInformationDao.getOfMonth(lastMonth.year, lastMonth.monthNumber)
        ) { current, last ->
            if (current == null) {
                val monthlyInformation = MonthlyInformation(
                    month = month,
                    bibleStudies = last?.bibleStudies ?: 0,
                    goal = last?.goal
                )
                val id = save(monthlyInformation)

                return@combine monthlyInformation.copy(id = id.toInt())
            }

            current
        }
    }

    suspend fun save(info: MonthlyInformation): Long {
        return withContext(Dispatchers.IO) {
            monthlyInformationDao.upsert(info)
        }
    }

    suspend fun update(month: LocalDate, modify: (monthlyInfo: MonthlyInformation) -> MonthlyInformation) {
        withContext(Dispatchers.IO) {
            val info = monthlyInformationDao.getOfMonth(month.year, month.monthNumber).firstOrNull() ?: return@withContext
            val modifiedInfo = modify(info)
            monthlyInformationDao.upsert(modifiedInfo)
        }
    }
}
