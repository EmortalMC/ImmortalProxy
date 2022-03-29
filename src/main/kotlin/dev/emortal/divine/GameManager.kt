package dev.emortal.divine

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.ServerInfo
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.GameManager.sendToServer
import dev.emortal.divine.utils.RedisStorage.redisson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object GameManager {
    private val logger = LoggerFactory.getLogger("GameManager")

    private val localhostName = server.allServers.first().serverInfo.address.hostName

    val serverGameMap = ConcurrentHashMap<String, String>()

    fun initListener() {


        // Subscribe to registergame channel
        redisson.getTopic("registergame").addListenerAsync(String::class.java) { channel, msg ->
            val args = msg.split(" ")
            val gameName = args[0].lowercase()
            val serverName = args[1].lowercase()
            val serverPort = args[2].toInt()

            logger.info("Registering new server")

            if (!server.getServer(serverName).isPresent && server.allServers.any { it.serverInfo.address.port == serverPort }) {
                logger.error("Port already in use")
                return@addListenerAsync
            }

            logger.info("Game: ${gameName}")
            logger.info("Server: ${serverName}")
            logger.info("Port: ${serverPort}")

            serverGameMap[gameName] = serverName

            if (!server.getServer(serverName).isPresent) {
                server.registerServer(ServerInfo(serverName, InetSocketAddress(localhostName, serverPort)))
            }
        }

        redisson.getTopic("joingame").addListenerAsync(String::class.java) { channel, msg ->
            val args = msg.split(" ")
            val gameName = args[0].lowercase()
            val uuid = args[1]

            val serverName = serverGameMap[gameName] ?: return@addListenerAsync

            logger.info("Sending ${uuid} ${gameName} ${serverName}")

            server.getPlayer(UUID.fromString(uuid)).ifPresent {
                it.sendToServer(serverName, gameName)
            }
        }

    }

    internal fun Player.sendToServer(serverName: String, game: String) {
        if (!serverGameMap.containsKey(game)) {
            logger.error("Game type not registered")
            return
        }

        sendActionBar(Component.text("Joining game $game ($serverName)", NamedTextColor.GREEN))

        var foundServer = false
        currentServer.ifPresent {
            if (it.serverInfo.name == serverName) {
                foundServer = true
                // Player is already connected to this server, instead publish redis changegame message
                logger.info("Player already on correct server, sending changegame ${serverName} ${game}")
                redisson.getTopic("playerpubsub${serverName}").publishAsync("changegame $uniqueId $game")
                return@ifPresent
            }
        }
        if (foundServer) return

        server.getServer(serverName).ifPresentOrElse({ server ->
            logger.info("${this.username} joining server ${server}, subgame: ${game}...")

            redisson.getBucket<String>("${this.uniqueId}-subgame").setAsync(game, 10, TimeUnit.SECONDS).thenRun {
                val future = this.createConnectionRequest(server).connectWithIndication()

                future.thenAccept { successful ->
                    if (successful) {
                        logger.info("Sent player ${this.username} to server ${serverName}")
                    }
                }
            }
        }, {
            logger.error("Couldn't get server by the name of ${serverName}, did it go offline?")

            this.sendMessage(Component.text("Failed to join game", NamedTextColor.RED))
        })
    }

}