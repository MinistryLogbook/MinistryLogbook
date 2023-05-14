package com.github.danieldaeschle.ministrynotes.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class EntryRepository(private val entryDao: EntryDao) {

    suspend fun get(id: Int) = withContext(Dispatchers.IO) {
        entryDao.get(id)
    }

    suspend fun getAllOfMonth(month: LocalDate) =
        withContext(Dispatchers.IO) {
            entryDao.getAllOfMonth(month.year, month.monthNumber)
        }

    suspend fun getTransferredFrom(localDate: LocalDate) =
        withContext(Dispatchers.IO) {
            entryDao.getTransferredFrom(localDate.year, localDate.monthNumber)
        }

    suspend fun save(entry: Entry): Int {
        return withContext(Dispatchers.IO) {
            entryDao.upsert(entry)
        }.first().toInt()
    }

    suspend fun delete(entry: Entry) {
        withContext(Dispatchers.IO) {
            entryDao.delete(entry)
        }
    }
}