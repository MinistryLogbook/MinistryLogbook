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

    override fun hashCode() = internalRepresentation.hashCode()

    companion object {
        val Empty = Time(0)
    }
}
