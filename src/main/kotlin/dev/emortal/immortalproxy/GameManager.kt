package dev.emortal.immortalproxy

import dev.emortal.immortalproxy.ImmortalProxyPlugin.Companion.server
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.LoggerFactory

object GameManager {
    val logger = LoggerFactory.getLogger("dev.emortal.immortalproxy.GameManager")

    fun registerGameType(gameType: String, gameTypeSettings: GameTypeSettings) {
        RedisStorage.pool.sadd("registeredGameTypes", gameType)
        RedisStorage.pool.set("${gameType}-serverName", gameTypeSettings.serverName)
    }

    internal fun Player.sendToServer(gameType: String) {
        val registeredGameTypes = RedisStorage.pool.smembers("registeredGameTypes")
        if (!registeredGameTypes.contains(gameType)) {
            logger.error("Game type not registered")
            return
        }
        val serverName = RedisStorage.pool.get("${gameType}-serverName")
        if (serverName == null) {
            logger.error("Server name not registered")
            return
        }

        server.getServer(serverName).ifPresentOrElse({ server ->
            val future = this.createConnectionRequest(server).connectWithIndication()

            future.thenAccept { successful ->
                if (successful) {
                    logger.info("Sent player ${this.username} to server ${serverName}")
                }
            }
        }, {
            logger.error("Couldn't get server by the name of ${serverName}, did it go offline?")

            this.sendMessage(Component.text("Failed to join game", NamedTextColor.RED))
        })
    }

}