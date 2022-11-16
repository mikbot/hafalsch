package dev.schlaubi.hafalsch.bot.util

import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.publicButton
import dev.schlaubi.hafalsch.bot.ui.JourneyData
import dev.schlaubi.hafalsch.bot.ui.UIContext
import dev.schlaubi.hafalsch.bot.ui.asUIContext
import dev.schlaubi.hafalsch.bot.ui.journey
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation

context (UIContext)
suspend fun ComponentContainer.showTrainInfo(details: JourneyInformation) {
    publicButton {
        bundle = dev.schlaubi.hafalsch.bot.util.bundle
        label = translate("notification.show_train_info")

        action {
            asUIContext {
                val station = details.currentStop?.station?.let { marudor.stopPlace.byEva(it.id) }
                journey(JourneyData(details.train.name, station, details.departure.time))
            }
        }
    }
}
