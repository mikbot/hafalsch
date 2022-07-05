package dev.schlaubi.hafalsch.traewelling.routes

import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api/v0")
public class Traewelling {

    @Serializable
    @Resource("/getuser")
    public data class Getuser(val traewelling: Traewelling = Traewelling())

    @Serializable
    @Resource("auth")
    public data class Auth(val traewelling: Traewelling = Traewelling()) {
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
            @SerialName("tripID")
            val tripId: String,
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
}