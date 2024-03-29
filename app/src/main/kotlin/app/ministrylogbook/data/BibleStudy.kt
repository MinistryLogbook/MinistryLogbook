package app.ministrylogbook.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class BibleStudy(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "month") val month: LocalDate = Clock.System.todayIn(
        TimeZone.currentSystemDefault()
    ),
    @ColumnInfo(name = "checked") val checked: Boolean = true
) : Parcelable {
    private companion object : Parceler<BibleStudy> {
        override fun create(parcel: Parcel) = BibleStudy(
            id = parcel.readInt(),
            name = parcel.readString()!!,
            month = Instant.fromEpochMilliseconds(parcel.readLong())
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        )

        override fun BibleStudy.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(name)
            parcel.writeLong(month.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
        }
    }
}
