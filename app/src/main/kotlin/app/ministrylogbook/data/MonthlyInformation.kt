package app.ministrylogbook.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
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
    @ColumnInfo(name = "bible_studies")
    @Deprecated("Is replaced by the study table.")
    val bibleStudies: Int? = null,
    @ColumnInfo(name = "goal") val goal: Int? = null,
    @ColumnInfo(name = "report_comment", defaultValue = "") val reportComment: String = "",
    @ColumnInfo(
        name = "dismissed_bible_studies_hint",
        defaultValue = "0"
    ) val dismissedBibleStudiesHint: Boolean = false
) : Parcelable {
    private companion object : Parceler<MonthlyInformation> {
        override fun create(parcel: Parcel) = MonthlyInformation(
            id = parcel.readInt(),
            month = Instant.fromEpochMilliseconds(parcel.readLong())
                .toLocalDateTime(TimeZone.currentSystemDefault()).date,
            goal = parcel.readValue(Int::class.java.classLoader) as Int?,
            dismissedBibleStudiesHint = parcel.readByte() != 0.toByte()
        )

        override fun MonthlyInformation.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeLong(
                month.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            )
            parcel.writeValue(goal)
            parcel.writeByte((if (dismissedBibleStudiesHint) 1 else 0).toByte())
        }
    }
}

@Entity(
    primaryKeys = ["monthlyInformationId", "studyId"],
    indices = [Index("studyId", unique = false)]
)
data class MonthlyInformationStudyCrossRef(
    val monthlyInformationId: Int,
    val studyId: Int
)

data class MonthlyInformationWithStudies(
    @Embedded val info: MonthlyInformation,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            MonthlyInformationStudyCrossRef::class,
            parentColumn = "monthlyInformationId",
            entityColumn = "studyId"
        )
    )
    val checkedStudies: List<Study>
)
