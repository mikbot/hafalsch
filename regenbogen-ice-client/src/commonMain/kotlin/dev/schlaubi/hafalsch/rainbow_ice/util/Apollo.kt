package dev.schlaubi.hafalsch.rainbow_ice.util

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.Operation

internal suspend fun <D : Operation.Data> ApolloCall<D>.executeSafe(): D = execute().dataAssertNoErrors
