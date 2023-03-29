package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EntryRepository(private val context: Context) {

    suspend fun get(id: Int) = withContext(Dispatchers.IO) {
        context.db().entryDao().get(id)
    }

    suspend fun getAllOfMonth(year: Int, month: Int) =
        withContext(Dispatchers.IO) {
            context.db().entryDao().getAllOfMonth(year, month)
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