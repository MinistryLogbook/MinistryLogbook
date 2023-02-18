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
data class StudyEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "month") val month: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    @ColumnInfo(name = "count") val count: Int = 0,
) : Parcelable {
    private companion object : Parceler<StudyEntry> {
        override fun create(parcel: Parcel) = StudyEntry(
            id = parcel.readInt(),
            month = Instant.fromEpochMilliseconds(parcel.readLong())
                .toLocalDateTime(TimeZone.currentSystemDefault()).date,
            count = parcel.readInt(),
        )

        override fun StudyEntry.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeLong(
                month.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            )
            parcel.writeInt(count)
        }
    }
}