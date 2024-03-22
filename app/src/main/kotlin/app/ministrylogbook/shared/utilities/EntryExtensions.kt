package app.ministrylogbook.shared.utilities

import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.Time

fun List<Entry>.timeSum(): Time {
    var hours = this.sumOf { it.hours }
    var minutes = this.sumOf { it.minutes }
    hours += minutes / 60
    minutes %= 60
    return Time(hours, minutes)
}

fun List<Entry>.ministryTimeSum() = this.ministries().timeSum()

fun List<Entry>.credits() = this.filter { it.isCredit }

fun List<Entry>.theocraticAssignments() = this.filter { it.type == EntryType.TheocraticAssignment }

fun List<Entry>.theocraticAssignmentTimeSum() = this.theocraticAssignments().timeSum()

fun List<Entry>.theocraticSchools() = this.filter { it.type == EntryType.TheocraticSchool }

fun List<Entry>.theocraticSchoolTimeSum() = this.theocraticSchools().timeSum()

fun List<Entry>.ministries() =
    this.filter { it.type in arrayOf(EntryType.Ministry, EntryType.Transfer) }

fun List<Entry>.transfers() = this.filter { it.type == EntryType.Transfer }

fun List<Entry>.splitIntoMonths(): List<List<Entry>> {
    if (this.isEmpty()) {
        return listOf()
    }

    val sorted = this.sortedBy { it.datetime }
    val months = mutableListOf<MutableList<Entry>>(mutableListOf())

    var currentYear = sorted.first().datetime.year
    var currentMonth = sorted.first().datetime.month
    sorted.forEach {
        if (it.datetime.year != currentYear || it.datetime.month != currentMonth) {
            currentYear = it.datetime.year
            currentMonth = it.datetime.month
            months.add(mutableListOf())
        }
        months.last().add(it)
    }

    return months
}
