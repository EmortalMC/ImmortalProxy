package dev.emortal.immortalproxy

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import java.util.logging.Logger

@Plugin(
    id = "immortalproxy",
    name = "Immortal Proxy",
    version = "1.0.0",
    description = "Handles games for EmortalMC"
)
class ImmortalProxyPlugin @Inject constructor(private val server: ProxyServer, private val logger: Logger) {

    @Subscribe
    fun onProxyInitialization(@SuppressWarnings event: ProxyInitializeEvent) {
        Companion.server = server
        server.eventManager.register(this, EventListener)

        server.commandManager.register(server.commandManager.metaBuilder("play").build(), PlayCommand)

        logger.info("[Immortal] has been enabled!")
    }

    companion object {
        lateinit var server: ProxyServer
    }

}