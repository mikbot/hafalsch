package dev.schlaubi.hafalsch.marudor

import dev.schlaubi.hafalsch.client.ClientCompanion
import dev.schlaubi.hafalsch.client.ClientResources
import dev.schlaubi.hafalsch.client.util.safeBody
import dev.schlaubi.hafalsch.marudor.entity.*
import dev.schlaubi.hafalsch.marudor.routes.HafasProfile
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.datetime.Instant
import dev.schlaubi.hafalsch.marudor.entity.CoachSequence as CoachSequenceEntity
import dev.schlaubi.hafalsch.marudor.entity.Map as StopPlaceMap
import dev.schlaubi.hafalsch.marudor.routes.CoachSequence as CoachSequenceRoute
import dev.schlaubi.hafalsch.marudor.routes.Hafas as HafasRoute
import dev.schlaubi.hafalsch.marudor.routes.Iris as IrisRoute
import dev.schlaubi.hafalsch.marudor.routes.Journeys as JourneysRoute
import dev.schlaubi.hafalsch.marudor.routes.StopPlace as StopPlaceRoute

public class Marudor(public val resoures: ClientResources) {

    public val hafas: Hafas = Hafas()
    public val stopPlace: StopPlace = StopPlace()
    public val coachSequence: CoachSequence = CoachSequence()
    public val iris: Iris = Iris()
    public val journeys: Journeys = Journeys()

    public inner class Hafas {
        /**
         * Retrieves the departures for a specific [station][eva] (including non DB regional services).
         */
        public suspend fun departures(
            eva: String,
            lookahead: Int? = null,
            lookbehind: Int? = null
        ): IrisDepartures? =
            resoures.client.get(HafasRoute.V1.IrisCompatibleAbfahrten(eva, lookahead, lookbehind)).safeBody()

        /**
         * Returns the details redirect for [journeyId].
         */
        public inline fun detailsRedirect(
            journeyId: String,
            profile: HafasProfile? = null,
            block: URLBuilder.() -> Unit = {}
        ): String =
            buildUrl(HafasRoute.V1.DetailsRedirect(journeyId, profile), block)

        /**
         * Provides additional information not provided by iris.
         *
         * @see Journeys.details
         */
        public suspend fun additionalInformation(
            trainName: String,
            evaNumberAlongRoute: String? = null,
            initialDepartureDate: Instant? = null
        ): HafasAdditionalJourneyInformation? =
            resoures.client.get(
                HafasRoute.V3.AdditionalInformation(
                    trainName,
                    evaNumberAlongRoute,
                    initialDepartureDate
                )
            ).safeBody()
    }

    public inner class StopPlace {
        /**
         * Searches for [Stations][Station] by [query] (useful for autocomplete).
         */
        public suspend fun search(
            query: String, max: Int? = null, filterForIris: Boolean? = null, groupedBySales: Boolean? = null
        ): List<Station> =
            resoures.client.get(StopPlaceRoute.StopPlaceSearch(query, max, filterForIris, groupedBySales)).body()

        /**
         * Retrieves the [Station] for [eva].
         */
        public suspend fun byEva(eva: String): Station? =
            resoures.client.get(StopPlaceRoute.StopPlaceByEva(eva)).safeBody()

        /**
         * Retrieves a [StopPlaceMap] by an [eva] and a [name].
         */
        public suspend fun map(eva: String, name: String): StopPlaceMap? =
            resoures.client.get(StopPlaceRoute.Map(name, eva)).safeBody()

        /**
         * Retrieves the [StopPlaceMap] for a [Station].
         */
        public suspend fun map(station: Station): StopPlaceMap? =
            map(station.eva, station.name)
    }

    public inner class CoachSequence {
        /**
         * Retrieves [coach sequence information][CoachSequenceEntity] for [trainNumber].
         */
        public suspend fun coachSequence(
            trainNumber: Int,
            departure: Instant,
            eva: String? = null,
            initialDeparture: Instant? = null
        ): CoachSequenceEntity? =
            resoures.client.get(CoachSequenceRoute.V4.CoachSequence(trainNumber, departure, eva, initialDeparture))
                .safeBody()
    }

    public inner class Iris {
        /**
         * Retrieves the departures for a specific [station][eva].
         */
        public suspend fun departures(
            eva: String,
            lookahead: Int? = null,
            lookbehind: Int? = null
        ): IrisDepartures? =
            resoures.client.get(IrisRoute.Departures(eva, lookahead, lookbehind)).safeBody()
    }

    public inner class Journeys {
        /**
         * Searches for journeys by [trainName] (useful for autocomplete)
         *
         * @param initialDepartureDate the initial departure date
         * @param filtered idk marudor requests with true
         * @param limit result limit
         */
        public suspend fun find(
            trainName: String,
            initialDepartureDate: Instant? = null,
            filtered: Boolean = true,
            limit: Int? = null
        ): List<IrisJourney> =
            resoures.client.get(JourneysRoute.Find(trainName, initialDepartureDate, filtered, limit)).safeBody()
                ?: emptyList()

        public suspend fun details(
            trainName: String,
            evaNumberAlongRoute: String? = null,
            initialDepartureDate: Instant? = null
        ): JourneyInformation? =
            resoures.client.get(JourneysRoute.Details(trainName, evaNumberAlongRoute, initialDepartureDate)).safeBody()
    }

    @PublishedApi
    internal inline fun <reified T> buildUrl(resource: T, block: URLBuilder.() -> Unit = {}): String {
        val format = resoures.client.plugin(Resources).resourcesFormat

        val builder = URLBuilder(resoures.url)
        href(format, resource, builder)

        builder.pathSegments = resoures.url.pathSegments + builder.pathSegments
        builder.apply(block)

        return builder.buildString()
    }

    public inline fun buildUrl(builder: URLBuilder.() -> Unit = {}): String =
        URLBuilder(resoures.url).apply { encodedPathSegments = emptyList() }.apply(builder).buildString()

    public companion object : ClientCompanion<Marudor, MarudorBuilder> {
        override fun newBuilder(): MarudorBuilder = MarudorBuilder()
    }
}
