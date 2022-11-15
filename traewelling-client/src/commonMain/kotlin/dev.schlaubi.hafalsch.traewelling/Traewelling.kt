package dev.schlaubi.hafalsch.traewelling

import dev.schlaubi.hafalsch.client.ClientCompanion
import dev.schlaubi.hafalsch.client.ClientResources
import dev.schlaubi.hafalsch.client.util.safeBody
import dev.schlaubi.hafalsch.traewelling.entity.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import dev.schlaubi.hafalsch.traewelling.entity.User as UserEntity
import dev.schlaubi.hafalsch.traewelling.routes.Traewelling as TraewellingRoute
import dev.schlaubi.hafalsch.traewelling.routes.Traewelling.Statuses as StatusesRoute

public class Traewelling internal constructor(private val resources: ClientResources) {

    public val auth: Auth = Auth()
    public val trains: Trains = Trains()
    public val statuses: Statuses = Statuses()
    public val user: User = User()

    public inner class User {
        /**
         * Lists all active statuses for [token].
         */
        public suspend fun listEnroute(token: String): Data<Status>? {
            return resources.client.get(TraewellingRoute.User.Statuses.Active()) {
                expectSuccess = false
                authenticate(token)
            }.safeBody()
        }
    }

    public inner class Auth {
        /**
         * Retrieves the user for [token].
         */
        public suspend fun getUser(token: String): Data<UserEntity> =
            resources.client.get(TraewellingRoute.Auth.User()) {
                authenticate(token)
            }.body()

        /**
         * Retrieves a [TokenResponse] for [email] and [password].
         */
        public suspend fun login(email: String, password: String): Data<TokenResponse>? =
            resources.client.post(TraewellingRoute.Auth.Login()) {
                expectSuccess = false
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.safeBody()

        /**
         * Destroys [token].
         */
        public suspend fun logout(token: String): Unit? = resources.client.post(TraewellingRoute.Auth.Logout()) {
            authenticate(token)
        }.safeBody()
    }

    public inner class Trains {
        /**
         * Searches for stations by [query] using [token].
         */
        public suspend fun autocomplete(query: String, token: String): List<Station> =
            resources.client.get(TraewellingRoute.Trains.AutoComplete(query)) {
                authenticate(token)
            }.body()

        /**
         * Retrieves the station board for [station].
         */
        public suspend fun stationBoard(
            station: Int,
            time: Instant? = null,
            travelType: TraewellingRoute.Trains.Stationboard.TravelType? = null,
            token: String
        ): Departures = resources.client.get(TraewellingRoute.Trains.Stationboard(station, time, travelType)) {
            authenticate(token)
        }.body()

        /**
         * Retrieves Trip information for [tripId] at [start].
         */
        public suspend fun trip(
            tripId: String,
            lineName: String? = null,
            start: String,
            token: String
        ): Trip = resources.client.get(TraewellingRoute.Trains.Trip(tripId, lineName, start)) {
            authenticate(token)
        }.body()

        /**
         * Requests a check in via [checkInRequest].
         */
        public suspend fun checkIn(checkInRequest: CheckInRequest, token: String): CheckInResponse =
            resources.client.post(TraewellingRoute.Trains.CheckIn()) {
                contentType(ContentType.Application.Json)
                authenticate(token)
                setBody(checkInRequest)
            }.body()

    }

    public inner class Statuses {
        /**
         * Lists all active statuses for [token].
         */
        public suspend fun listEnroute(token: String): StatusesList =
            resources.client.get(StatusesRoute.EnRoute.All()) {
                authenticate(token)
            }.body()

        /**
         * Deletes the status corresponding to [id].
         */
        public suspend fun delete(id: Int, token: String): Unit =
            resources.client.delete(StatusesRoute.Specific(id)) {
                authenticate(token)
            }.body()

        /**
         * Deletes the status corresponding to [id].
         */
        public suspend fun fetch(id: Int, token: String): Status? =
            resources.client.get(StatusesRoute.Specific(id)) {
                authenticate(token)
            }.safeBody()
    }

    private fun HttpRequestBuilder.authenticate(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    public companion object : ClientCompanion<Traewelling, TraewellingBuilder> {
        override fun newBuilder(): TraewellingBuilder = TraewellingBuilder()
    }
}
