package com.github.danieldaeschle.ministrylogbook.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [BibleStudyEntry::class, Entry::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun bibleStudyEntryDao(): BibleStudyEntryDao
}
