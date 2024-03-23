package app.ministrylogbook.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.ministrylogbook.shared.Time
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
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "datetime") val datetime: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    @ColumnInfo(name = "placements")
    @Deprecated("Newest report does not contain this information anymore")
    val placements: Int = 0,
    @ColumnInfo(name = "video_showings")
    @Deprecated("Newest report does not contain this information anymore")
    val videoShowings: Int = 0,
    @ColumnInfo(name = "hours") val hours: Int = 0,
    @ColumnInfo(name = "minutes") val minutes: Int = 0,
    @ColumnInfo(name = "return_visits")
    @Deprecated("Newest report does not contain this information anymore")
    val returnVisits: Int = 0,
    @ColumnInfo(name = "type") val type: EntryType = EntryType.Ministry,
    @ColumnInfo(name = "transferred_from") val transferredFrom: LocalDateTime? = null
) : Parcelable {

    val isCredit: Boolean
        get() = type == EntryType.TheocraticSchool || type == EntryType.TheocraticAssignment

    val time: Time
        get() = Time(hours, minutes)

    private companion object : Parceler<Entry> {
        override fun create(parcel: Parcel) = Entry(
            id = parcel.readInt(),
            datetime = Instant.fromEpochMilliseconds(parcel.readLong())
                .toLocalDateTime(TimeZone.currentSystemDefault()),
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
                datetime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
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
