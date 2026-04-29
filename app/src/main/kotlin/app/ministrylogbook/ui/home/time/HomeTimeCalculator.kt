package app.ministrylogbook.ui.home.time

import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.sum
import app.ministrylogbook.shared.toTime
import app.ministrylogbook.shared.utilities.isInFirstWeekOfMonth
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.splitIntoMonths
import app.ministrylogbook.shared.utilities.theocraticAssignmentTimeSum
import app.ministrylogbook.shared.utilities.theocraticSchoolTimeSum
import app.ministrylogbook.shared.utilities.timeSum
import app.ministrylogbook.shared.utilities.weekNumber
import kotlinx.datetime.LocalDate

data class HomeTimeSummary(
    val accumulatedTime: Time,
    val accumulatedBeforeToday: Time,
    val todayAdded: Time,
    val showTodaySegment: Boolean,
    val remainingHours: Int?,
    val credit: Time,
    val weeklyProgress: Float,
    val weeklyTime: Time,
    val fieldServiceTime: Time
)

object HomeTimeCalculator {
    fun calculateSummary(
        month: LocalDate,
        today: LocalDate,
        entries: List<Entry>,
        entriesLastMonth: List<Entry>,
        transferred: List<Entry>,
        role: Role,
        goal: Int?,
        roleGoal: Int?
    ): HomeTimeSummary {
        maxHoursWithCredit(roleGoal)
        val transferredTime = transferred.timeSum()
        val fieldServiceTime = calculateFieldServiceTime(entries, transferredTime)
        val credit = entries.theocraticAssignmentTimeSum() + entries.theocraticSchoolTimeSum()
        val accumulatedTime = calculateAccumulatedTime(
            entries = entries,
            transferredTime = transferredTime,
            role = role,
            roleGoal = roleGoal
        )
        val entriesBeforeToday = entries.filter { it.datetime.date != today }
        val accumulatedBeforeToday = calculateAccumulatedTime(
            entries = entriesBeforeToday,
            transferredTime = entriesBeforeToday.filter { it.type == EntryType.Transfer }.timeSum(),
            role = role,
            roleGoal = roleGoal
        )
        val todayAdded = (accumulatedTime - accumulatedBeforeToday).takeIf { it > Time.Empty } ?: Time.Empty
        val currentWeek = today.weekNumber
        val entriesCurrentWeekLastMonth = if (today.isInFirstWeekOfMonth) {
            entriesLastMonth.filter { it.datetime.date.weekNumber == currentWeek }
        } else {
            emptyList()
        }
        val entriesCurrentWeek = entries.filter {
            it.type != EntryType.Transfer && it.datetime.date.weekNumber == currentWeek
        }
        val weekGoal = ((goal ?: 0) * 12f / 52f).toTime()
        val weeklyTime = entriesCurrentWeekLastMonth.timeSum() + entriesCurrentWeek.timeSum()
        val weeklyProgress = if (weekGoal.isEmpty) {
            0f
        } else {
            (weeklyTime.toFloat() / weekGoal.toFloat()).coerceIn(0f, 1f)
        }

        return HomeTimeSummary(
            accumulatedTime = accumulatedTime,
            accumulatedBeforeToday = accumulatedBeforeToday,
            todayAdded = todayAdded,
            showTodaySegment = month.year == today.year &&
                    month.month == today.month &&
                    todayAdded.isNotEmpty,
            remainingHours = calculateRemainingHours(goal, role, accumulatedTime, fieldServiceTime),
            credit = credit,
            weeklyProgress = weeklyProgress,
            weeklyTime = weeklyTime,
            fieldServiceTime = fieldServiceTime
        )
    }

    fun calculateAccumulatedTime(
        entries: List<Entry>,
        transferredTime: Time = Time.Empty,
        role: Role,
        roleGoal: Int?
    ): Time {
        val maxHoursWithCredit = maxHoursWithCredit(roleGoal)
        val ministryTime = calculateFieldServiceTime(entries, transferredTime)
        val assignmentTime = entries.theocraticAssignmentTimeSum()
        val schoolTime = entries.theocraticSchoolTimeSum()

        return if (role.canHaveCredit && ministryTime.hours < maxHoursWithCredit.hours) {
            minOf(ministryTime + assignmentTime, maxHoursWithCredit) + schoolTime
        } else {
            ministryTime
        }
    }

    fun calculateYearlyProgress(entriesInServiceYear: List<Entry>, role: Role, roleGoal: Int?): Time =
        entriesInServiceYear
            .splitIntoMonths()
            .map { entries ->
                calculateAccumulatedTime(
                    entries = entries,
                    role = role,
                    roleGoal = roleGoal
                )
            }
            .sum()

    private fun maxHoursWithCredit(roleGoal: Int?): Time = Time((roleGoal ?: 0) + 5, 0)

    private fun calculateFieldServiceTime(entries: List<Entry>, transferredTime: Time): Time =
        entries.ministryTimeSum() - transferredTime

    private fun calculateRemainingHours(goal: Int?, role: Role, accumulatedTime: Time, fieldServiceTime: Time): Int? {
        val goalValue = goal ?: return null
        val hours = if (role.canHaveCredit) {
            accumulatedTime.hours
        } else {
            fieldServiceTime.hours
        }
        return goalValue - kotlin.math.min(hours, goalValue)
    }
}
