package app.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class MonthlyInformationRepository(
    private val monthlyInformationDao: MonthlyInformationDao,
    private val databaseChangeNotifier: DatabaseChangeNotifier
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getOfMonth(month: LocalDate): Flow<MonthlyInformation> {
        val lastMonth = month.minus(DatePeriod(months = 1))

        return databaseChangeNotifier.databaseChanges.flatMapLatest {
            combine(
                monthlyInformationDao.getOfMonth(month.year, month.month.ordinal + 1),
                monthlyInformationDao.getOfMonth(lastMonth.year, lastMonth.month.ordinal + 1)
            ) { current, last ->
                if (current == null) {
                    val monthlyInformation = MonthlyInformation(
                        month = month,
                        goal = last?.goal
                    )
                    val id = save(monthlyInformation)

                    return@combine monthlyInformation.copy(id = id.toInt())
                }

                current
            }
        }
    }

    suspend fun save(info: MonthlyInformation): Long = withContext(Dispatchers.IO) {
        monthlyInformationDao.upsert(info)
    }

    suspend fun update(month: LocalDate, modify: (monthlyInfo: MonthlyInformation) -> MonthlyInformation) {
        withContext(Dispatchers.IO) {
            val info = monthlyInformationDao
                .getOfMonth(month.year, month.month.ordinal + 1)
                .firstOrNull()
                ?: return@withContext
            val modifiedInfo = modify(info)
            monthlyInformationDao.upsert(modifiedInfo)
        }
    }
}
