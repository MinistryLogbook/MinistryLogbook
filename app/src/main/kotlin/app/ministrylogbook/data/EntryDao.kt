package app.ministrylogbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface EntryDao {

    @Query("SELECT * FROM entry WHERE id = :id")
    suspend fun get(id: Int): Entry

    @Query("SELECT * from entry WHERE strftime('%Y%m', datetime) = :year || substr('00' || :month, -2, 2)")
    suspend fun getAllOfMonth(year: Int, month: Int): List<Entry>

    @Query("SELECT * from entry WHERE strftime('%Y%m', transferred_from) = :year || substr('00' || :month, -2, 2)")
    suspend fun getTransferredFrom(year: Int, month: Int): List<Entry>

    @Upsert
    suspend fun upsert(vararg entries: Entry): List<Long>

    @Delete
    suspend fun delete(entry: Entry)
}