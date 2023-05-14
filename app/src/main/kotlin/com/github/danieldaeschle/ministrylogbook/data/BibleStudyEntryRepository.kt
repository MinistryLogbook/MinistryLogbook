package com.github.danieldaeschle.ministrylogbook.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class BibleStudyEntryRepository(private val bibleStudyEntryDao: BibleStudyEntryDao) {

    suspend fun getOfMonth(month: LocalDate) = withContext(Dispatchers.IO) {
        val entry = bibleStudyEntryDao.getOfMonth(month.year, month.monthNumber)
        if (entry == null) {
            val lastMonth = month.minus(DatePeriod(months = 1))
            val lastMonthEntry =
                bibleStudyEntryDao.getOfMonth(lastMonth.year, lastMonth.monthNumber)
            save(BibleStudyEntry(month = month, count = lastMonthEntry?.count ?: 0))
        }
        bibleStudyEntryDao.getOfMonth(month.year, month.monthNumber)
    }

    suspend fun save(studyEntry: BibleStudyEntry): Long {
        return withContext(Dispatchers.IO) {
            bibleStudyEntryDao.upsert(studyEntry)
        }
    }
}