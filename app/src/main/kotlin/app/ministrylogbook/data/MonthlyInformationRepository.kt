package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class MonthlyInformationRepository(private val monthlyInformationDao: MonthlyInformationDao) {

    suspend fun getOfMonth(month: LocalDate) = withContext(Dispatchers.IO) {
        val entry = monthlyInformationDao.getOfMonth(month.year, month.monthNumber)
        if (entry == null) {
            val lastMonth = month.minus(DatePeriod(months = 1))
            val lastMonthInfo =
                monthlyInformationDao.getOfMonth(lastMonth.year, lastMonth.monthNumber)
            save(
                MonthlyInformation(
                    month = month,
                    bibleStudies = lastMonthInfo?.bibleStudies ?: 0,
                    goal = lastMonthInfo?.goal
                )
            )
        }
        monthlyInformationDao.getOfMonth(month.year, month.monthNumber)
    }

    suspend fun save(info: MonthlyInformation): Long {
        return withContext(Dispatchers.IO) {
            monthlyInformationDao.upsert(info)
        }
    }
}
