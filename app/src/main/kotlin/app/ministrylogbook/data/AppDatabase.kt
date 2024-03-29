package app.ministrylogbook.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MonthlyInformation::class, Entry::class, BibleStudy::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(LocalDateConverters::class, LocalDateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun monthlyInformationDao(): MonthlyInformationDao
    abstract fun studyDao(): BibleStudyDao
}
