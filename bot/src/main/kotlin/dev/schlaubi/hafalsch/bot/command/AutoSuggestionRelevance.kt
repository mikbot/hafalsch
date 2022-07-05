package dev.schlaubi.hafalsch.bot.command

import info.debatty.java.stringsimilarity.Levenshtein

private val levenshtein = Levenshtein()

fun <T> List<T>.sortByRelevance(input: String, mapper: (T) -> String) = if (input.isBlank()) this else sortedBy {
    levenshtein.distance(input, mapper(it))
}
