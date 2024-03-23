package app.ministrylogbook.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Study(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
) : Parcelable {
    private companion object : Parceler<Study> {
        override fun create(parcel: Parcel) = Study(
            id = parcel.readInt(),
            name = parcel.readString()!!,
            createdAt = Instant.fromEpochMilliseconds(parcel.readLong())
                .toLocalDateTime(TimeZone.currentSystemDefault())
        )

        override fun Study.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(name)
            parcel.writeLong(createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
        }
    }
}
