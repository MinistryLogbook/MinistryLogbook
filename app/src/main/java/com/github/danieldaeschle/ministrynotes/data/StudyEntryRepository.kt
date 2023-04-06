package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class StudyEntryRepository(private val context: Context) {

    suspend fun getOfMonth(month: LocalDate) = withContext(Dispatchers.IO) {
        val entry = context.db().studyEntryDao().getOfMonth(month.year, month.monthNumber)
        if (entry == null) {
            val lastMonth = month.minus(DatePeriod(months = 1))
            val lastMonthEntry =
                context.db().studyEntryDao().getOfMonth(lastMonth.year, lastMonth.monthNumber)
            save(StudyEntry(month = month, count = lastMonthEntry?.count ?: 0))
        }
        context.db().studyEntryDao().getOfMonth(month.year, month.monthNumber)
    }

    suspend fun save(studyEntry: StudyEntry): Long {
        return withContext(Dispatchers.IO) {
            context.db().studyEntryDao().upsert(studyEntry)
        }
    }
}