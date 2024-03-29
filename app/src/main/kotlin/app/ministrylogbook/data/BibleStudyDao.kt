package app.ministrylogbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleStudyDao {
    @Query("SELECT * FROM biblestudy WHERE id = :id")
    fun get(id: Int): Flow<BibleStudy?>

    @Query("SELECT * FROM biblestudy WHERE strftime('%Y%m', month) = :year || substr('00' || :month, -2, 2)")
    fun getAllOfMonth(year: Int, month: Int): Flow<List<BibleStudy>>

    @Upsert
    suspend fun upsert(vararg studies: BibleStudy): List<Long>

    @Delete
    suspend fun delete(bibleStudy: BibleStudy)
}
