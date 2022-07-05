package dev.schlaubi.hafalsch.bot.database

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object Database : KordExKoinComponent {
    val traewellingLogins = database.getCollection<TraevellingUserLogin>("traewelling_logins")
}

@Serializable
data class TraevellingUserLogin(
    @SerialName("_id")
    val id: Snowflake,
    val token: String,
    val expiresAt: Instant,
    val userId: Int
)
