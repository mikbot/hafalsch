package dev.schlaubi.hafalsch.bot.util

import dev.kord.rest.builder.message.EmbedBuilder

inline fun MutableList<EmbedBuilder>.embed(block: EmbedBuilder.() -> Unit) = add(
    dev.schlaubi.mikbot.plugin.api.util.embed(
        block
    )
)
