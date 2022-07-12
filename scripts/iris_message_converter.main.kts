// Converter for IRIS xlsx file converted to csv
// https://www.dbnetze.com/resource/blob/6245806/1c422761486d3bf7c4f430ad857db793/2021_24_Liste_optimierte_RIS-Kundengruende-data.xlsx
@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10")
@file:DependsOn("dev.schlaubi:stdx-core-jvm:1.2.1")

import dev.schlaubi.stdx.core.nullIfBlank
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.*

val translationKeyPrefix = "iris.message."

val sourceFile = Path("data/iris_messages.csv")
val outputFile = Path("out/iris_output.properties")

val messages = sourceFile.readLines()
    .asSequence()
    .drop(2) // drop header
    .mapNotNull {entry ->
        val (codeRaw, _, oldRaw, newRaw) = entry.split(',')
        val old = oldRaw.nullIfBlank()
        val new = newRaw.nullIfBlank()
        val code = codeRaw.toInt()
        val text = new ?: old

        text?.let { code to it }
    }

val properties = Properties()

messages.forEach { (key, value) ->
    properties["$translationKeyPrefix$key"] = value
}

if (!outputFile.parent.isDirectory()) {
    outputFile.parent.createDirectories()
}

outputFile.bufferedWriter(options = arrayOf(StandardOpenOption.CREATE)).use {
    properties.store(it, null)
}

println("Exported properties to ${outputFile.absolutePathString()}")
