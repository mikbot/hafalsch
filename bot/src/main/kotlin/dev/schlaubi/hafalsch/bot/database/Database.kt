package dev.schlaubi.hafalsch.bot.database

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

object Database : KordExKoinComponent {
    val traewellingLogins = database.getCollection<TraevellingUserLogin>("traewelling_logins")
    val checkIns = database.getCollection<CheckIn>("train_check_ins")
}

suspend fun CoroutineCollection<CheckIn>.findForJourney(user: Snowflake, journeyId: String) = findOne(
    and(CheckIn::journeyId eq journeyId, CheckIn::user eq user)
)

suspend fun CoroutineCollection<CheckIn>.findForJournies(journeyIds: List<String>) = find(
    and(CheckIn::journeyId `in` journeyIds)
).toList()

@Serializable
data class TraevellingUserLogin(
    @SerialName("_id")
    val id: Snowflake,
    val token: String,
    val expiresAt: Instant,
    val userId: Int
)

@Serializable
data class CheckIn(
    @SerialName("_id") @Contextual
    val id: Id<CheckIn> = newId(),
    val user: Snowflake,
    val journeyId: String,
    val start: String,
    val end: String
)
