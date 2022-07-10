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
            append(currentUic[4])
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

private fun fullUrl(type: String) = "https://lib.finalrewind.org/dbdb/db_wagen/$type.png"
