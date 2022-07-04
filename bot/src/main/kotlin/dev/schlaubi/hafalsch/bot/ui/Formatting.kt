package dev.schlaubi.hafalsch.bot.ui


fun String.cancel(cancel: Boolean) = if (cancel) "~~$this~~" else this

fun String.bold(bold: Boolean) = if (bold) "**$this**" else this
