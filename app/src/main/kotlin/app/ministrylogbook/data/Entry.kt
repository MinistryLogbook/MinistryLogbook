package app.ministrylogbook.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.ministrylogbook.lib.Time
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
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "datetime") val datetime: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    @ColumnInfo(name = "placements") val placements: Int = 0,
    @ColumnInfo(name = "video_showings") val videoShowings: Int = 0,
    @ColumnInfo(name = "hours") val hours: Int = 0,
    @ColumnInfo(name = "minutes") val minutes: Int = 0,
    @ColumnInfo(name = "return_visits") val returnVisits: Int = 0,
    @ColumnInfo(name = "type") val type: EntryType = EntryType.Ministry,
    @ColumnInfo(name = "transferred_from") val transferredFrom: LocalDate? = null,
) : Parcelable {

    val isCredit: Boolean
        get() = type == EntryType.TheocraticSchool || type == EntryType.TheocraticAssignment

    val time: Time
        get() = Time(hours, minutes)

    private companion object : Parceler<Entry> {
        override fun create(parcel: Parcel) = Entry(
            id = parcel.readInt(),
            datetime = Instant.fromEpochMilliseconds(parcel.readLong())
                .toLocalDateTime(TimeZone.currentSystemDefault()).date,
            placements = parcel.readInt(),
            videoShowings = parcel.readInt(),
            hours = parcel.readInt(),
            minutes = parcel.readInt(),
            returnVisits = parcel.readInt(),
            type = parcel.readString().let { EntryType.valueOf(it!!) }
        )

        override fun Entry.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeLong(
                datetime.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            )
            parcel.writeInt(placements)
            parcel.writeInt(videoShowings)
            parcel.writeInt(hours)
            parcel.writeInt(minutes)
            parcel.writeInt(returnVisits)
            parcel.writeString(type.name)
        }
    }
}
