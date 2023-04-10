package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class BibleStudyEntryRepository(private val context: Context) {

    suspend fun getOfMonth(month: LocalDate) = withContext(Dispatchers.IO) {
        val entry = context.db().bibleStudyEntryDao().getOfMonth(month.year, month.monthNumber)
        if (entry == null) {
            val lastMonth = month.minus(DatePeriod(months = 1))
            val lastMonthEntry =
                context.db().bibleStudyEntryDao().getOfMonth(lastMonth.year, lastMonth.monthNumber)
            save(BibleStudyEntry(month = month, count = lastMonthEntry?.count ?: 0))
        }
        context.db().bibleStudyEntryDao().getOfMonth(month.year, month.monthNumber)
    }

    suspend fun save(studyEntry: BibleStudyEntry): Long {
        return withContext(Dispatchers.IO) {
            context.db().bibleStudyEntryDao().upsert(studyEntry)
        }
    }
}