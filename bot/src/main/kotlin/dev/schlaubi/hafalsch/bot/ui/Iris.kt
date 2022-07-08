package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.schlaubi.hafalsch.marudor.entity.IrisMessage


fun List<IrisMessage>.filterRelevant() = filter { it.head == null }

fun List<IrisMessage>.format() = joinToString("\n") {
    buildString {
        if (it.timestamp != null) {
            @Suppress("ReplaceNotNullAssertionWithElvisReturn")
            append(it.timestamp!!.toDiscord(TimestampType.ShortTime))
            append(": ")
        }
        append(it.text)
    }.cancel(it.superseded)
}
