package com.github.danieldaeschle.ministrylogbook.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface BibleStudyEntryDao {
    @Query("SELECT * from biblestudyentry WHERE strftime('%Y%m', month) = :year || substr('00' || :month, -2, 2) LIMIT 1")
    suspend fun getOfMonth(year: Int, month: Int): BibleStudyEntry?

    @Upsert
    suspend fun upsert(studyEntry: BibleStudyEntry): Long
}