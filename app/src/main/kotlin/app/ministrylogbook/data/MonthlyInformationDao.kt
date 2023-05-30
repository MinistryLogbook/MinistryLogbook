package app.ministrylogbook.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MonthlyInformationDao {
    @Query("SELECT * from monthlyinformation WHERE strftime('%Y%m', month) = :year || substr('00' || :month, -2, 2) LIMIT 1")
    suspend fun getOfMonth(year: Int, month: Int): MonthlyInformation?

    @Upsert
    suspend fun upsert(info: MonthlyInformation): Long
}