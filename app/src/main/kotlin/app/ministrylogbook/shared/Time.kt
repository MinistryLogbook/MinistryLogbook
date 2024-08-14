package app.ministrylogbook.shared

import kotlin.math.abs

class Time : Comparable<Time> {

    private var internalRepresentation: Int = 0

    val isNegative
        get() = internalRepresentation < 0

    val hours
        get() = abs(internalRepresentation) / 60

    val minutes
        get() = abs(internalRepresentation) % 60

    val isEmpty
        get() = this == Empty

    val isNotEmpty
        get() = this != Empty

    constructor(hours: Int, minutes: Int, isNegative: Boolean = false) {
        if (hours < 0 || minutes < 0) {
            throw IllegalArgumentException("Hours and minutes must be positive")
        }
        if (minutes > 59) {
            throw IllegalArgumentException("Minutes must be less than 60")
        }

        if (isNegative) {
            this.internalRepresentation = -(hours * 60 + minutes)
        } else {
            this.internalRepresentation = hours * 60 + minutes
        }
    }

    private constructor(internalRepresentation: Int) {
        this.internalRepresentation = internalRepresentation
    }

    operator fun plus(other: Time) =
        Time(this.internalRepresentation + other.internalRepresentation)

    operator fun minus(other: Time) =
        Time(this.internalRepresentation - other.internalRepresentation)

    override fun compareTo(other: Time) =
        this.internalRepresentation.compareTo(other.internalRepresentation)

    override fun equals(other: Any?) =
        (other is Time) && this.internalRepresentation == other.internalRepresentation

    operator fun div(other: Int): Time {
        if (other == 0) {
            throw IllegalArgumentException("Division by zero")
        }
        return Time(this.internalRepresentation / other)
    }

    operator fun times(other: Int): Time {
        return Time(this.internalRepresentation * other)
    }

    override fun hashCode() = internalRepresentation.hashCode()

    companion object {
        val Empty = Time(0)
    }

    override fun toString(): String {
        val sign = if (isNegative) "-" else ""
        return "$sign$hours:${minutes.toString().padStart(2, '0')}"
    }

    fun toFloat(): Float {
        return this.hours + 1 / 60f * this.minutes
    }
}

fun Iterable<Time>.sum(): Time {
    var sum = Time.Empty
    this.forEach { sum += it }
    return sum
}

fun Float.toTime(): Time {
    val hours = this.toInt()
    val minutes = ((this - hours) * 60).toInt()
    return Time(hours, minutes)
}

fun Int.toTime() = Time(this, 0)
