package dev.schlaubi.hafalsch.traewelling.routes

import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api/v1")
public class Traewelling {

    @Serializable
    @Resource("user")
    public data class User(val traewelling: Traewelling = Traewelling()) {
        @Serializable
        @Resource("statuses")
        public data class Statuses(val user: User = User()) {
            @Serializable
            @Resource("active")
            public data class Active(val statuses: Statuses = Statuses())
        }
    }

    @Serializable
    @Resource("auth")
    public data class Auth(val traewelling: Traewelling = Traewelling()) {
        /**
         * Retrieves the current user.
         */
        @Serializable
        @Resource("/user")
        public data class User(val auth: Auth = Auth())

        /**
         * Log in with your user account.
         */
        @Serializable
        @Resource("login")
        public data class Login(val auth: Auth = Auth())

        /**
         * Register a account and log in.
         */
        @Serializable
        @Resource("signup")
        public data class Signup(val auth: Auth = Auth())

        /**
         * Logs the user out and destroys the access token.
         */
        @Serializable
        @Resource("logout")
        public data class Logout(val auth: Auth = Auth())
    }

    @Serializable
    @Resource("trains")
    public data class Trains(val traewelling: Traewelling = Traewelling()) {

        @Serializable
        @Resource("autocomplete/{query}")
        public data class AutoComplete(val query: String, val trains: Trains = Trains())

        @Serializable
        @Resource("stationboard")
        public data class Stationboard(
            val station: Int,
            val time: Instant? = null,
            val travelType: TravelType? = null,
            val trains: Trains = Trains()
        ) {
            @Serializable
            public enum class TravelType {
                @SerialName("nationalExpress")
                NATIONAL_EXPRESS,

                @SerialName("express")
                LONG_DISTANCE_TRAVEL,

                @SerialName("national")
                NATIONAL,

                @SerialName("regionalExp")
                REGIONAL_EXPRESS,

                @SerialName("regional")
                REGIONAL,

                @SerialName("suburban")
                SUBURBAN,

                @SerialName("bus")
                BUS,

                @SerialName("ferry")
                FERRY,

                @SerialName("subway")
                SUBWAY,

                @SerialName("tram")
                TRAM,

                @SerialName("taxi")
                TAXI
            }
        }

        @Serializable
        @Resource("trip")
        public data class Trip(
            @SerialName("tripID") val tripId: String,
            val lineName: String? = null,
            val start: String,
            val trains: Trains = Trains()
        )

        @Serializable
        @Resource("checkin")
        public data class CheckIn(val trains: Trains = Trains())

        @Serializable
        @Resource("latest")
        public data class Latest(val trains: Trains = Trains())

        @Serializable
        @Resource("home")
        public data class Home(val trains: Trains = Trains())
    }

    @Serializable
    @Resource("statuses")
    public data class Statuses(val traewelling: Traewelling = Traewelling()) {
        @Serializable
        @Resource("{id}")
        public data class Specific(val id: Int, val statuses: Statuses = Statuses())

        @Serializable
        @Resource("enroute")
        public data class EnRoute(val statuses: Statuses = Statuses()) {
            @Serializable
            @Resource("all")
            public data class All(val enroute: EnRoute = EnRoute())
        }

        @Serializable
        @Resource("event/{id}")
        public data class Event(val id: Int, val statuses: Statuses = Statuses())

        @Serializable
        @Resource("{id}")
        public data class Status(val id: Int) {
            @Serializable
            @Resource("like")
            public data class Like(val status: Status) {
                public constructor(id: Int) : this(Status(id))
            }
        }
    }
}
