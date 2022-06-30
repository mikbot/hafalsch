package dev.schlaubi.hafalsch.bot.ui

import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.marudor.entity.Load

const val BUS_EMOJI = "<:ecitaro:941801084173832213>"

const val LOAD_LOW = "<:capacity_1:990937144241557545>"
const val LOAD_MEDMIUM = "<:capacity_2:990937143486599179>"
const val LOAD_HIGH = "<:capacity_3:990937142333177886>"
const val LOAD_VERY_HIGH = "<:capacity_4:990937141284577360>"

private val loadEmotes = listOf(LOAD_LOW, LOAD_MEDMIUM, LOAD_HIGH, LOAD_VERY_HIGH)

fun getLoadForValue(value: Load.Load?) = value?.let { loadEmotes[it.value - 1] } ?: Emojis.question.toString()
