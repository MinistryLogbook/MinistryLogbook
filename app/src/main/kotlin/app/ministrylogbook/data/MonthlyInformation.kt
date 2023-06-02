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
data class MonthlyInformation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "month") val month: LocalDate = Clock.System.todayIn(
        TimeZone.currentSystemDefault()
    ),
    @ColumnInfo(name = "bible_studies") val bibleStudies: Int? = null,
    @ColumnInfo(name = "goal") val goal: Int? = null
) : Parcelable {
    private companion object : Parceler<MonthlyInformation> {
        override fun create(parcel: Parcel) = MonthlyInformation(
            id = parcel.readInt(),
            month = Instant.fromEpochMilliseconds(parcel.readLong())
                .toLocalDateTime(TimeZone.currentSystemDefault()).date,
            bibleStudies = parcel.readValue(Int::class.java.classLoader) as Int?,
            goal = parcel.readValue(Int::class.java.classLoader) as Int?
        )

        override fun MonthlyInformation.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeLong(
                month.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            )
            parcel.writeValue(bibleStudies)
            parcel.writeValue(goal)
        }
    }
}
