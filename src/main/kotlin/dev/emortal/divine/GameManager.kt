package dev.emortal.divine

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.ServerInfo
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.utils.RedisStorage.redisson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.time.Duration
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

            logger.info("Registering game ${gameName} (${localhostName}:${serverPort})")

            if (!server.getServer(serverName).isPresent && server.allServers.any { it.serverInfo.address.port == serverPort }) {
                logger.error("Port already in use")
                return@addListenerAsync
            }

            serverGameMap[gameName] = serverName

            if (!server.getServer(serverName).isPresent) {
                server.registerServer(ServerInfo(serverName, InetSocketAddress(localhostName, serverPort)))
            }
        }

        redisson.getTopic("joingame").addListenerAsync(String::class.java) { channel, msg ->
            val args = msg.split(" ")
            val gameName = args[0].lowercase()
            val uuid = args[1]
            val spectating = if (args.size > 2) args[2].toBoolean() else false

            val serverName = serverGameMap[gameName] ?: return@addListenerAsync

            logger.info("Joining ${uuid} ${gameName} ${serverName} | spectating: ${spectating}")

            server.getPlayer(UUID.fromString(uuid)).ifPresent {
                it.sendToServer(serverName, gameName, spectating)
            }
        }

    }

    fun Player.sendToServer(serverName: String, game: String, spectate: Boolean = false, playerToSpectate: UUID? = null) {
        if (!serverGameMap.containsKey(game) && !spectate) {
            logger.error("Game type not registered")
            return
        }

        var foundServer = false
        currentServer.ifPresent {
            if (it.serverInfo.name == serverName) {
                //showTitle(Title.title(Component.text("\uE00A"), Component.empty(), Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofMillis(500))))

                foundServer = true
                // Player is already connected to this server, instead publish redis changegame message
                logger.info("Player already on correct server")
                val topic = redisson.getTopic("playerpubsub${serverName}")
                if (spectate) {
                    topic.publishAsync("spectateplayer $uniqueId $playerToSpectate")
                } else {
                    topic.publishAsync("changegame $uniqueId $game")
                }
                return@ifPresent
            }
        }
        if (foundServer) return

        server.getServer(serverName).ifPresentOrElse({ server ->
            //showTitle(Title.title(Component.text("\uE00A"), Component.empty(), Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofMillis(500))))
            logger.info("${this.username} joining server ${serverName}, subgame: ${game}...")

            redisson.getBucket<String>("${this.uniqueId}-subgame").setAsync("$game $spectate $playerToSpectate", 10, TimeUnit.SECONDS).thenRun {
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