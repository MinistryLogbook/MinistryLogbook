package com.github.danieldaeschle.ministrynotes.data

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
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "datetime") val datetime: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    @ColumnInfo(name = "placements") val placements: Int = 0,
    @ColumnInfo(name = "video_showings") val videoShowings: Int = 0,
    @ColumnInfo(name = "hours") val hours: Int = 0,
    @ColumnInfo(name = "minutes") val minutes: Int = 0,
    @ColumnInfo(name = "return_visits") val returnVisits: Int = 0,
    @ColumnInfo(name = "credit_hours") val creditHours: Int = 0,
    @ColumnInfo(name = "credit_minutes") val creditMinutes: Int = 0,
) : Parcelable {

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
            creditHours = parcel.readInt(),
            creditMinutes = parcel.readInt(),
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
            parcel.writeInt(creditHours)
            parcel.writeInt(creditMinutes)
        }
    }
}