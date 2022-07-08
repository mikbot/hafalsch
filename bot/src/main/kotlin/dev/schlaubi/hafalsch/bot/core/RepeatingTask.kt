package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

abstract class RepeatingTask : CoroutineScope, KordExKoinComponent {

    protected abstract val duration: Duration

    @OptIn(ObsoleteCoroutinesApi::class)
    private val ticker by lazy { ticker(duration.inWholeMilliseconds, 0) }
    private lateinit var runner: Job
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    fun start() {
        runner = launch {
            for (unit in ticker) {
                if (isActive) {
                    run()
                }
            }
        }
    }

    protected abstract suspend fun run()
}
