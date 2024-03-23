package app.ministrylogbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyInformationDao {
    @Transaction
    @Query(
        "SELECT * from monthlyinformation WHERE " +
            "strftime('%Y%m', month) = :year || substr('00' || :month, -2, 2) LIMIT 1"
    )
    fun getOfMonth(year: Int, month: Int): Flow<MonthlyInformationWithStudies?>

    @Upsert
    suspend fun upsert(info: MonthlyInformation): Long

    @Insert
    suspend fun insertMonthlyInformationStudyCrossRef(ref: MonthlyInformationStudyCrossRef): Long

    @Delete
    suspend fun deleteMonthlyInformationStudyCrossRef(ref: MonthlyInformationStudyCrossRef)
}
