package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class EntryRepository(private val context: Context) {

    suspend fun get(id: Int) = withContext(Dispatchers.IO) {
        context.db().entryDao().get(id)
    }

    suspend fun getAllOfMonth(month: LocalDate) =
        withContext(Dispatchers.IO) {
            context.db().entryDao().getAllOfMonth(month.year, month.monthNumber)
        }

    suspend fun getTransferredFrom(localDate: LocalDate) =
        withContext(Dispatchers.IO) {
            context.db().entryDao().getTransferredFrom(localDate.year, localDate.monthNumber)
        }


    suspend fun save(entry: Entry): Long {
        return withContext(Dispatchers.IO) {
            context.db().entryDao().upsert(entry)
        }.first()
    }

    suspend fun delete(entry: Entry) {
        withContext(Dispatchers.IO) {
            context.db().entryDao().delete(entry)
        }
    }
}