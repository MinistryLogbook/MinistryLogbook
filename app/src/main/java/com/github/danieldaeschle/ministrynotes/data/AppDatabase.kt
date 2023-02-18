package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [StudyEntry::class, Entry::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun studyEntryDao(): StudyEntryDao
}

fun Context.db() = Room.databaseBuilder(
    applicationContext,
    AppDatabase::class.java, "db"
).build()


