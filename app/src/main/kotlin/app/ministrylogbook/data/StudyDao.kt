package app.ministrylogbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Query("SELECT * FROM study WHERE id = :id")
    fun get(id: Int): Flow<Study?>

    @Query("SELECT * FROM study")
    fun getAll(): Flow<List<Study>>

    @Upsert
    suspend fun upsert(vararg studies: Study): List<Long>

    @Delete
    suspend fun delete(study: Study)
}
