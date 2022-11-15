package dev.schlaubi.hafalsch.bot.util

import kotlinx.datetime.Instant
import org.litote.kmongo.serialization.TemporalExtendedJsonSerializer

object DateSerializer : TemporalExtendedJsonSerializer<Instant>() {
    override fun epochMillis(temporal: Instant): Long = temporal.toEpochMilliseconds()

    override fun instantiate(date: Long): Instant = Instant.fromEpochMilliseconds(date)
}
