package dev.schlaubi.hafalsch.marudor.util

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*

internal suspend inline fun <reified T : Any> HttpResponse.catchNotFoundBody(): T? {
    if (status == HttpStatusCode.NotFound || status == HttpStatusCode.InternalServerError) {
        return null
    }

    return body<T>()
}
