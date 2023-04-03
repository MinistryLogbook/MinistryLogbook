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
    @ColumnInfo(name = "kind") val kind: EntryKind = EntryKind.Ministry
) : Parcelable {

    val isCredit: Boolean
        get() = kind == EntryKind.TheocraticSchool || kind == EntryKind.TheocraticAssignment

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
            kind = parcel.readString().let { EntryKind.valueOf(it!!) }
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
            parcel.writeString(kind.name)
        }
    }
}

data class Time(val hours: Int, val minutes: Int) : Comparable<Time> {
    operator fun plus(other: Time): Time {
        var accumulatedHours = this.hours + other.hours
        var accumulatedMinutes = this.minutes + other.minutes
        accumulatedHours += accumulatedMinutes / 60
        accumulatedMinutes %= 60
        return Time(accumulatedHours, accumulatedMinutes)
    }

    operator fun minus(other: Time): Time {
        var accumulatedHours = this.hours - other.hours
        var accumulatedMinutes = this.minutes + other.minutes
        if (accumulatedMinutes < 0) {
            accumulatedHours -= 1
            accumulatedMinutes += 60
        }
        return Time(accumulatedHours, accumulatedMinutes)
    }

    override fun compareTo(other: Time): Int {
        val res = this.hours.compareTo(other.hours)
        if (res == 0) {
            return this.minutes.compareTo(other.minutes)
        }
        return res
    }
}

fun List<Entry>.timeSum(): Time {
    var hours = this.sumOf { it.hours }
    var minutes = this.sumOf { it.minutes }
    hours += minutes / 60
    minutes %= 60
    return Time(hours, minutes)
}

fun List<Entry>.ministryTimeSum() = this.filter { it.kind == EntryKind.Ministry }.timeSum()

fun List<Entry>.theocraticAssignmentTimeSum() =
    this.filter { it.kind == EntryKind.TheocraticAssignment }.timeSum()

fun List<Entry>.theocraticSchoolTimeSum() =
    this.filter { it.kind == EntryKind.TheocraticSchool }.timeSum()