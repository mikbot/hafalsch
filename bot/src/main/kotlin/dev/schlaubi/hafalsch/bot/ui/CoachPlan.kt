package dev.schlaubi.hafalsch.bot.ui

import dev.schlaubi.hafalsch.marudor.entity.Coach
import dev.schlaubi.hafalsch.marudor.entity.CoachGroup

private val withImage = listOf(
    "ARkimbz",
    "ARkimmbz",
    "Apmmz",
    "Avmmz",
    "Avmz",
    "Bimmdzf",
    "Bpmbz",
    "Bpmmbdz",
    "Bpmmbdzf",
    "Bpmmbz",
    "Bpmmdz",
    "Bpmmz",
    "Bvmmsz",
    "Bvmmz",
    "Bvmsz",
    "DApza",
    "DBpza",
    "DBpbzfa"
)

private val modelWithPdf = listOf(
    "401",
    "402",
    "403.R",
    "403.S1",
    "403.S2",
    "406",
    "406.R",
    "407",
    "411.S1",
    "411.S2",
    "412",
    "415",
    "IC2.KISS",
    "IC2.TWIN",
    "MET"
)

private val allowedTypes = listOf("IC", "ICE")

private val seriesRegex = """\.S(\d)""".toRegex()

// Taken from: https://github.com/marudor/BahnhofsAbfahrten/blob/3461399ce44f5ad29dd601134fe7c5116a06dbf3/packages/client/Common/Components/Reihung/WagenLink.tsx#L23-L80
fun Coach.findPlan(trainType: String, parent: CoachGroup): String? {
    val identifier = parent.model?.identifier
    if (trainType !in allowedTypes ||
        category == "TRIEBKOPF" ||
        category == "LOK"
    ) {
        return null
    }
    if ((identifier == null || identifier == "IC2.TWIN") && type in withImage) {
        // can't be null because of null not being in withImage
        @Suppress("ReplaceNotNullAssertionWithElvisReturn")
        return fullUrl(type!!)
    }

    val currentUic = uic
    if (identifier != null &&
        identifier != "TGV" &&
        identifier != "MET" &&
        currentUic != null
    ) {
        val type = buildString {
            append(currentUic.substr(4, 5))
            if (identifier.endsWith('R')) {
                append(".r")
            } else if (".S" in identifier) {
                append(seriesRegex.find(identifier)?.groupValues?.get(1))
            }
        }
        return fullUrl(type)
    }

    return null
}

// Taken from: https://cs.github.com/marudor/BahnhofsAbfahrten/blob/31113ff2d669f208847dfa103ea6ed5e61bf2e12/packages/client/Common/Components/Reihung/BRInfo.tsx?q=WRSheets
fun CoachGroup.Model.findPlan(): String? {
    return if (identifier in modelWithPdf) {
        return "https://marudor.de//WRSheets/${identifier}.pdf"
    } else {
        null
    }
}

fun CoachGroup.Model.formatNameWithPlan(): String {
    val plan = findPlan()
    return if (plan == null) {
        name
    } else {
        "[$name]($plan)"
    }
}

private fun fullUrl(type: String) = "https://lib.finalrewind.org/dbdb/db_wagen/$type.png"

private fun CharSequence.substr(start: Int, length: Int) = substring(start, start + length)
