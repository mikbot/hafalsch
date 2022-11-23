package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.schlaubi.hafalsch.bot.config.Config
import dev.schlaubi.hafalsch.bot.database.Database
import dev.schlaubi.hafalsch.bot.database.TraevellingUserLogin
import dev.schlaubi.hafalsch.bot.ui.modify
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.koin.core.component.inject
import org.pf4j.Extension
import kotlin.time.Duration.Companion.seconds

private const val AUTH_NAME = "trwl"

@Serializable
@Resource("hafalsch/oauth")
class HafalschRoute {
    @Serializable
    @Resource("login")
    data class Login(val id: String, val hafalschRoute: HafalschRoute = HafalschRoute())

    @Serializable
    @Resource("callback")
    data class Callback(val hafalschRoute: HafalschRoute = HafalschRoute())

    @Serializable
    @Resource("done")
    data class Done(val hafalschRoute: HafalschRoute = HafalschRoute())

    @Serializable
    @Resource("authorize")
    data class Authorize(val hafalschRoute: HafalschRoute = HafalschRoute())
}

@Serializable
data class Session(val id: String)

private val tokens = mutableMapOf<String, OAuthAccessTokenResponse.OAuth2>()
private val requests = mutableMapOf<String, Snowflake>()

fun registerUser(snowflake: Snowflake): String {
    val id = generateNonce()
    requests[id] = snowflake
    return id
}

@Extension
class OAuthServer : KtorExtensionPoint, KordExKoinComponent {
    private val traewelling by inject<Traewelling>()
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    override fun Application.apply() {
        install(Sessions) {
            cookie<Session>("SESSION") {
                cookie.path = "/"
            }
        }
        install(Authentication) {
            oauth(AUTH_NAME) {
                urlProvider = { this@apply.buildBotUrl(HafalschRoute.Callback()).toString() }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = AUTH_NAME,
                        authorizeUrl = Config.TRAEWELLING_API.modify { path("oauth", "authorize") },
                        accessTokenUrl = Config.TRAEWELLING_API.modify { path("oauth", "token") },
                        requestMethod = HttpMethod.Post,
                        clientId = Config.TRAEWELLING_CLIENT_ID,
                        clientSecret = Config.TRAEWELLING_CLIENT_SECRET
                    )
                }

                client = this@OAuthServer.client
            }
        }

        routing {
            get<HafalschRoute.Login> { (id) ->
                val session = call.sessions.get<Session>()?.id
                if (session != null && requests.containsKey(id) && tokens.containsKey(id)) {
                    call.respondRedirect(application.href(HafalschRoute.Done()))
                } else {
                    call.sessions.set(Session(id))
                    call.respondRedirect(application.href(HafalschRoute.Authorize()))
                }
            }
            authenticate(AUTH_NAME) {
                get<HafalschRoute.Authorize> {
                    call.respondRedirect(application.href(HafalschRoute.Done()))
                }
                get<HafalschRoute.Callback> {
                    val session = call.sessions.get<Session>() ?: throw BadRequestException("No session")
                    val principal: OAuthAccessTokenResponse.OAuth2 =
                        call.principal() ?: throw BadRequestException("No token found")
                    tokens[session.id] = principal
                    call.respondRedirect(application.href(HafalschRoute.Done()))
                }
            }
            get<HafalschRoute.Done> {
                val session = call.sessions.get<Session>() ?: throw BadRequestException("No session")
                val userId = requests[session.id] ?: throw BadRequestException("Invalid session")
                val response = tokens[session.id] ?: throw BadRequestException("Invalid session")
                val user = traewelling.auth.getUser(response.accessToken)

                @Suppress("ReplaceNotNullAssertionWithElvisReturn")
                Database.traewellingLogins.save(
                    TraevellingUserLogin(
                        userId,
                        response.accessToken,
                        response.refreshToken!!,
                        (Clock.System.now() + response.expiresIn.seconds),
                        user.data.id
                    )
                )

                call.sessions.clear(call.sessions.findName(Session::class))
                tokens.remove(session.id)
                requests.remove(session.id)

                call.respond("Thanks for connecting, as usual, our team will gladly advise you to not buy any Apple products and have a nice day")
            }
        }
    }
}

private inline fun <reified T : Any> Application.buildBotUrl(resource: T): Url {
    val resourceUrl = href(resource)
    return dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl {
        encodedPath = resourceUrl
    }
}
