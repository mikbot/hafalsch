package dev.schlaubi.hafalsch.bot.ui

import io.ktor.http.*

inline fun Url.modify(builder: URLBuilder.() -> Unit) = URLBuilder(this).apply(builder).buildString()
