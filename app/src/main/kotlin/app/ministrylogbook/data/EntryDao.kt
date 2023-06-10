package app.ministrylogbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM entry WHERE id = :id")
    fun get(id: Int): Flow<Entry>

    @Query(
        "SELECT * FROM entry WHERE strftime('%Y%m', datetime) = :year || substr('00' || :month, -2, 2)"
    )
    fun getAllOfMonth(year: Int, month: Int): Flow<List<Entry>>

    @Query(
        "SELECT * FROM entry WHERE strftime('%Y%m', datetime) >= :fromYear || substr('00' || :fromMonth, -2, 2) " +
            "AND strftime('%Y%m', datetime) <= :toYear || substr('00' || :toMonth, -2, 2)"
    )
    fun getAllInRange(fromYear: Int, fromMonth: Int, toYear: Int, toMonth: Int): Flow<List<Entry>>

    @Query(
        "SELECT * FROM entry WHERE strftime('%Y%m', transferred_from) = :year || substr('00' || :month, -2, 2)"
    )
    fun getTransferredFrom(year: Int, month: Int): Flow<List<Entry>>

    @Upsert
    suspend fun upsert(vararg entries: Entry): List<Long>

    @Delete
    suspend fun delete(entry: Entry)
}
