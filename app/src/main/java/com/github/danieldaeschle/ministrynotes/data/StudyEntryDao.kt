package com.github.danieldaeschle.ministrynotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface StudyEntryDao {
    @Query("SELECT * from studyentry WHERE strftime('%Y%m', month) = :year || substr('00' || :month, -2, 2) LIMIT 1")
    suspend fun getOfMonth(year: Int, month: Int): StudyEntry?

    @Upsert
    suspend fun upsert(studyEntry: StudyEntry): Long
}