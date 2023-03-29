package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyEntryRepository(private val context: Context) {

    suspend fun getOfMonth(year: Int, month: Int) = withContext(Dispatchers.IO) {
        val entry = context.db().studyEntryDao().getOfMonth(year, month)
        if (entry == null) {
            val lastYear = if (month <= 1) year - 1 else year
            val lastMonth = if (month <= 1) 12 else month - 1
            val lastMonthEntry = context.db().studyEntryDao().getOfMonth(lastYear, lastMonth)
            save(StudyEntry(count = lastMonthEntry?.count ?: 0))
        }
        context.db().studyEntryDao().getOfMonth(year, month)
    }

    suspend fun save(studyEntry: StudyEntry): Long {
        return withContext(Dispatchers.IO) {
            context.db().studyEntryDao().upsert(studyEntry)
        }
    }
}