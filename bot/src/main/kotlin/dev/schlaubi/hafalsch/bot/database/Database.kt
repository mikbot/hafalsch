package dev.schlaubi.hafalsch.bot.database

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.schlaubi.hafalsch.bot.util.DateSerializer
import dev.schlaubi.hafalsch.marudor.entity.IrisMessage
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
    val journeyStates = database.getCollection<JourneyState>("journey_states")
    val subscriptionSettings = database.getCollection<SubscribtionSettings>("hafalsch_subscription_settings")
}

suspend fun CoroutineCollection<SubscribtionSettings>.findOneByIdSafe(user: Snowflake) =
    findOneById(user) ?: SubscribtionSettings(user)

suspend fun CoroutineCollection<CheckIn>.findForJournies(journeyIds: List<String>) =
    find(CheckIn::journeyId `in` journeyIds).toList()

suspend fun CoroutineCollection<CheckIn>.deleteNotActive(user: Snowflake, journeyId: String) =
    deleteMany(and(CheckIn::user eq user, not(CheckIn::journeyId eq journeyId)))

@Serializable
data class TraevellingUserLogin(
    @SerialName("_id")
    val id: Snowflake,
    val token: String,
    @Serializable(with = DateSerializer::class)
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
    val end: String,
    // Delay of last notification, not real time delay
    val delays: Map<String, Int> // Map<EVA, DELAY>
)

@Serializable
data class JourneyState(
    @SerialName("_id")
    val journeyId: String,
    val messages: List<IrisMessage>,
    val delays: Map<String, Int> // Map<EVA, DELAY>
) {
    override fun equals(other: Any?): Boolean {
        if (other !is JourneyState) return false

        if (other.journeyId != journeyId) return false
        if (other.delays != delays) return false

        return messages.withIndex()
            .all { (index, message) ->
                val previousMessage = other.messages.getOrNull(index)

                previousMessage?.value == message.value && message.superseded == previousMessage?.superseded
            }
    }

    override fun hashCode(): Int {
        var result = journeyId.hashCode()
        result = 31 * result + messages.hashCode()
        result = 31 * result + delays.hashCode()
        return result
    }
}

@Serializable
data class SubscribtionSettings(
    @SerialName("_id")
    val id: Snowflake,
    val currentDelayMargin: Int = 5, // change-margin for delay at current stop
    val exitDelayMargin: Int = 5, // change-margin for delay at users exit stop
    val subscribeToMessages: Boolean = true,
    val locale: String? = null
)

