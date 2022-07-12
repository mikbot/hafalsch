package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.schlaubi.hafalsch.marudor.entity.IrisMessage

private const val irisMessagePrefix = "iris.message."

context(UIContext)
        suspend fun IrisMessage.safeTranslate(): String {
    return if (value != null) {
        translate("$irisMessagePrefix$value").takeIf { !it.startsWith(irisMessagePrefix) } ?: text
    } else {
        return text
    }
}


fun List<IrisMessage>.filterRelevant() = filter { it.head == null }

context (UIContext) suspend fun List<IrisMessage>.format() =
    map { it to it.safeTranslate() }
        .joinToString("\n") { (it, translated) ->
            buildString {
                if (it.timestamp != null) {
                    @Suppress("ReplaceNotNullAssertionWithElvisReturn") append(it.timestamp!!.toDiscord(TimestampType.ShortTime))
                    append(": ")
                }
                append(translated)
            }.cancel(it.superseded)
        }
