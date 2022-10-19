package dev.emortal.divine

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.ServerInfo
import dev.emortal.divine.DivinePlugin.Companion.plugin
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.GameManager.sendToServer
import dev.emortal.divine.utils.JedisStorage.jedis
import dev.emortal.divine.utils.JedisStorage.jedisScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPubSub
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

        jedisScope.launch {
            // Create register game listener
            val registerGamePubSub = object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    val args = message.split(" ")
                    val gameName = args[0].lowercase()
                    val serverName = args[1].lowercase()
                    val serverPort = args[2].toInt()

                    logger.info("Registering game ${gameName} (${localhostName}:${serverPort})")

                    if (!server.getServer(serverName).isPresent && server.allServers.any { it.serverInfo.address.port == serverPort }) {
                        logger.error("Port already in use")
                        return
                    }

                    serverGameMap[gameName] = serverName

                    if (!server.getServer(serverName).isPresent) {
                        server.registerServer(ServerInfo(serverName, InetSocketAddress(localhostName, serverPort)))
                    }

                    if (gameName == DivinePlugin.divineConfig.defaultGame) {
                        // Reconnect players in Limbo
                        server.scheduler.buildTask(plugin) {
                            server.getServer("limbo").ifPresent { limboServer ->
                                server.getServer(DivinePlugin.divineConfig.defaultGame).ifPresent { lobbyServer ->
                                    limboServer.playersConnected.forEach {
                                        it.createConnectionRequest(lobbyServer).fireAndForget()
                                    }
                                }
                            }
                        }.delay(Duration.ofSeconds(2)).schedule()

                    }
                }
            }
            jedis.subscribe(registerGamePubSub, "registergame")

            val joinGamePubSub = object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    val args = message.split(" ")
                    val gameName = args[0].lowercase()
                    val uuid = args[1]
                    val spectating = if (args.size > 2) args[2].toBoolean() else false

                    val serverName = serverGameMap[gameName] ?: return

                    logger.info("Joining ${uuid} ${gameName} ${serverName} | spectating: ${spectating}")

                    server.getPlayer(UUID.fromString(uuid)).ifPresent {
                        it.sendToServer(serverName, gameName, spectating)
                    }
                }
            }
            jedis.subscribe(joinGamePubSub, "joingame")
        }



    }

    fun Player.sendToServer(serverName: String, game: String, spectate: Boolean = false, playerToSpectate: UUID? = null) {
//        if (!serverGameMap.containsKey(game) && !spectate) {
//            logger.error("Game type not registered")
//            return
//        }

        var foundServer = false
        currentServer.ifPresent {
            if (it.serverInfo.name == serverName) {
                //showTitle(Title.title(Component.text("\uE00A"), Component.empty(), Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofMillis(500))))

                foundServer = true
                // Player is already connected to this server, instead publish redis changegame message
                logger.info("Player already on correct server")
                if (spectate) {
                    jedis.publish("playerpubsub${serverName}", "spectateplayer $uniqueId $playerToSpectate")
                } else {
                    jedis.publish("playerpubsub${serverName}", "changegame $uniqueId $game")
                }
                return@ifPresent
            }
        }
        if (foundServer) return

        server.getServer(serverName).ifPresentOrElse({ server ->
            //showTitle(Title.title(Component.text("\uE00A"), Component.empty(), Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofMillis(500))))
            logger.info("${this.username} joining server ${serverName}, subgame: ${game}...")

            jedisScope.launch {
                jedis.setex("${this@sendToServer.uniqueId}-subgame", 10, "$game $spectate $playerToSpectate")

                val future = this@sendToServer.createConnectionRequest(server).connectWithIndication()

                future.thenAccept { successful ->
                    if (successful) {
                        logger.info("Sent player ${this@sendToServer.username} to server ${serverName}")
                    }
                }
            }

        }, {
            logger.error("Couldn't get server by the name of ${serverName}, did it go offline?")

            this.sendMessage(Component.text("Failed to join game", NamedTextColor.RED))
        })
    }

}