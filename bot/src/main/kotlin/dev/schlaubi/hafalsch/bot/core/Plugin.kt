package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.schlaubi.hafalsch.bot.commands.departuresCommand
import dev.schlaubi.hafalsch.bot.commands.journeyCommand
import dev.schlaubi.hafalsch.bot.commands.stationCommand
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper

@PluginMain
class Plugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    private val marudor = Marudor()

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            afterKoinSetup {
                loadModule {
                    single { marudor }
                }
            }
        }
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::HafalschModule)
    }
}

class HafalschModule : Extension() {
    override val name: String = "Hafalsch"
    override val bundle: String = "hafalsch"

    override suspend fun setup() {
        stationCommand()
        journeyCommand()
        departuresCommand()
    }
}
