package dev.emortal.divine

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import java.util.logging.Logger

@Plugin(
    id = "divine",
    name = "Divine",
    version = "1.0.0",
    description = "Handles proxy business, such as DN or YM"
)
class DivinePlugin @Inject constructor(private val server: ProxyServer, private val logger: Logger) {

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        Companion.server = server
        server.eventManager.register(this, EventListener(this))

        //server.commandManager.register(server.commandManager.metaBuilder("play").build(), PlayCommand)

        logger.info("[Divine] has been enabled!")

    }

    companion object {
        lateinit var server: ProxyServer
    }

}