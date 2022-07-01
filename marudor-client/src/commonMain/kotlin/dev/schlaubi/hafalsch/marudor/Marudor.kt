package dev.schlaubi.hafalsch.marudor

import dev.schlaubi.hafalsch.marudor.entity.*
import dev.schlaubi.hafalsch.marudor.routes.HafasProfile
import dev.schlaubi.hafalsch.marudor.util.catchNotFoundBody
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import dev.schlaubi.hafalsch.marudor.entity.CoachSequence as CoachSequenceEntity
import dev.schlaubi.hafalsch.marudor.entity.Map as StopPlaceMap
import dev.schlaubi.hafalsch.marudor.routes.CoachSequence as CoachSequenceRoute
import dev.schlaubi.hafalsch.marudor.routes.Hafas as HafasRoute
import dev.schlaubi.hafalsch.marudor.routes.StopPlace as StopPlaceRoute

public class Marudor(public val resoures: MarudorResources) {

    public val hafas: Hafas = Hafas()
    public val stopPlace: StopPlace = StopPlace()
    public val coachSequence: CoachSequence = CoachSequence()

    public inner class Hafas {
        /**
         * Retrieves [JourneyInformation] for specific [trainName].
         */
        public suspend fun details(
            trainName: String, station: String? = null, date: Instant? = null, profile: HafasProfile? = null
        ): JourneyInformation? =
            resoures.client.get(HafasRoute.V2.Details(trainName, station, date, profile)).catchNotFoundBody()

        /**
         * Returns the details redirect for [journeyId].
         */
        public fun detailsRedirect(journeyId: String, profile: HafasProfile? = null): String =
            buildUrl(HafasRoute.V1.DetailsRedirect(journeyId, profile))

        /**
         * Searches for journeys matching [name] (Useful for autocomplete).
         */
        public suspend fun journeyMatch(
            name: String,
            initialDepartureDate: Instant = Clock.System.now(),
            filters: List<HafasJourneyMatchRequest.Filter> = emptyList(),
            onlyRT: Boolean = false,
            profile: HafasProfile? = null
        ): List<HafasJourneyMatchJourney> {
            return resoures.client.post(HafasRoute.V2.JourneyMatch(profile)) {
                contentType(ContentType.Application.Json)
                setBody(HafasJourneyMatchRequest(name, initialDepartureDate, filters, onlyRT))
            }.body()
        }

        /**
         * Searches for journeys matching [name] (Useful for autocomplete) but different.
         */
        public suspend fun enrichedJourneyMatch(
            name: String,
            limit: Int = 5,
            initialDepartureDate: Instant = Clock.System.now(),
            filtered: Boolean = true
        ): List<HafasJourneyMatchJourney> {
            return resoures.client.post(HafasRoute.V1.EnrichedJourneyMatch()) {
                contentType(ContentType.Application.Json)
                setBody(HafasEnrichedJourneyMatchRequest(name, limit, initialDepartureDate, filtered))
            }.body()
        }
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
            resoures.client.get(StopPlaceRoute.StopPlaceByEva(eva)).catchNotFoundBody()

        /**
         * Retrieves a [StopPlaceMap] by an [eva] and a [name].
         */
        public suspend fun map(eva: String, name: String): StopPlaceMap? =
            resoures.client.get(StopPlaceRoute.Map(name, eva)).catchNotFoundBody()

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
        ): CoachSequenceEntity =
            resoures.client.get(CoachSequenceRoute.V4.CoachSequence(trainNumber, departure, eva, initialDeparture))
                .body()
    }

    private inline fun <reified T> buildUrl(resource: T): String {
        val format = resoures.client.plugin(Resources).resourcesFormat

        val builder = URLBuilder(resoures.url)
        href(format, resource, builder)

        builder.pathSegments = resoures.url.pathSegments + builder.pathSegments

        return builder.buildString()
    }

    public inline fun buildUrl(builder: URLBuilder.() -> Unit = {}): String =
        URLBuilder(resoures.url).apply { encodedPathSegments = emptyList() }.apply(builder).buildString()
}
