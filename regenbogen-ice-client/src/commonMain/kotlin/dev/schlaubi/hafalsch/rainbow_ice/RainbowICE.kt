package dev.schlaubi.hafalsch.rainbow_ice

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import dev.schlaubi.hafalsch.rainbow_ice.util.executeSafe

/**
 * Mapper of the [regenbogen-ice.de](regenbogen-ice.de) API.
 *
 * You might need to import [dev.schlaubi.hafalsch.client.invoke] to use [RainbowICEBuilder]
 */
public class RainbowICE {
    private val apollo = ApolloClient.Builder()
        .serverUrl("https://regenbogen-ice.de/graphql")
        .build()

    /**
     * Provides autocomplete for [query].
     */
    public suspend fun autocomplete(query: String): List<AutoCompleteQuery.Autocomplete> {
        return apollo.query(AutoCompleteQuery(query))
            .executeSafe()
            .autocomplete
    }

    /**
     * Validates whether [query] is a valid TZn.
     *
     * @see autocomplete
     */
    public suspend fun matchTrain(query: String): Boolean =
        apollo.query(MatchTrainQuery(query)).executeSafe().autocomplete.firstOrNull()?.guess == query

    /**
     * Fetches [train information][FetchTrainQuery.Train_vehicle] for [query].
     */
    public suspend fun fetchTrain(query: String, tripLimit: Int?): FetchTrainQuery.Train_vehicle? =
        apollo.query(FetchTrainQuery(query, Optional.presentIfNotNull(tripLimit))).executeSafe().train_vehicle

}
