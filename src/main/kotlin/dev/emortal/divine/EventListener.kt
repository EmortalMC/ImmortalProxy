package dev.emortal.divine

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.scheduler.ScheduledTask
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.utils.RedisStorage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit

class EventListener(val plugin: DivinePlugin) {

    val logger = LoggerFactory.getLogger("EventListener")

    /*@Subscribe
    fun pluginMessage(e: PluginMessageEvent) {
        val player = e.source as? Player ?: return

        if (e.identifier.id != "Immortal") return
        player.sendMessage(Component.text("Received plugin message"))

        val channel = e.dataAsDataStream().readLine()
        if (channel != "ServerSend") return

        val server = e.dataAsDataStream().readLine() ?: return

        player.sendMessage(Component.text("Recieved plugin message, sending to ${server}"))
        player.sendToServer(server)
    }*/

    var limboReconnectTask: ScheduledTask? = null

    @Subscribe
    fun playerPreJoin(e: ServerPreConnectEvent) {
        RedisStorage.redisson.getBucket<String>("${e.player.uniqueId}-subgame").trySetAsync("lobby", 15, TimeUnit.SECONDS)

        if (e.player.currentServer.isPresent && e.originalServer.serverInfo.name == "limbo") {
            e.result = ServerPreConnectEvent.ServerResult.denied()
            e.player.sendMessage(Component.text("Why would you want to go there?"))
        }
    }

    @Subscribe
    fun login(e: PostLoginEvent) {
        val message = Component.text()
            .append(Component.text("Have you joined our Discord yet?", NamedTextColor.GREEN))
            .append(Component.text("\nWe post all announcements and votes there, maybe you should join us?\n", NamedTextColor.GRAY))
            .append(Component.text("Click ", NamedTextColor.GRAY))
            .append(
                Component.text("HERE", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(Component.text("https://discord.gg/TZyuMSha96", NamedTextColor.GREEN)))
                    .clickEvent(ClickEvent.openUrl("https://discord.gg/TZyuMSha96"))
            )
            .append(Component.text(" to join.", NamedTextColor.GRAY))
        server.scheduler.buildTask(plugin) {
            e.player.sendMessage(message)
            //e.player.playSound(Sound.sound(Key.key("minecraft:entity.villager.celebrate"), Sound.Source.MASTER, 1f, 1f))
        }.delay(Duration.ofSeconds(5)).schedule()
    }

    @Subscribe
    fun playerLeaveServer(e: DisconnectEvent) {
        refreshTablist()
    }

    @Subscribe
    fun playerJoinServer(e: ServerConnectedEvent) {
        //logger.info("Connected!")

        refreshTablist()


        if (e.server.serverInfo.name == "limbo") {
            e.player.sendMessage(Component.text("It looks like we're experiencing downtime. You will be automatically reconnected when we're back online!", NamedTextColor.RED))

            if (limboReconnectTask == null) {
                logger.info("Starting new reconnect task!")

                limboReconnectTask = server.scheduler.buildTask(plugin) {
                    val limboServer = server.getServer("lobby")
                    limboServer.ifPresentOrElse(
                        { // Present
                            logger.info("Lobby server found")

                            val connectedResults = BooleanArray(e.server.playersConnected.size)
                            e.server.playersConnected.forEachIndexed { i, plr ->
                                try {
                                    plr.createConnectionRequest(it).connect().thenAcceptAsync { result ->
                                        connectedResults[i] = result != null && result.isSuccessful
                                    }
                                } catch (_: Exception) {

                                }

                            }

                            if (connectedResults.all { it }) {
                                logger.info("Everyone has been sent to a lobby, stopping reconnect task.")
                                limboReconnectTask!!.cancel()
                            }

                        },
                        { // Not present
                            logger.warn("Lobby server not present")
                        }
                    )

                }.delay(Duration.ofSeconds(3)).repeat(Duration.ofSeconds(3)).schedule()
            }
        }
    }

    fun refreshTablist() {
        val mini = MiniMessage.miniMessage()

        server.allPlayers.forEach {
            it.sendPlayerListHeaderAndFooter(
                Component.text()
                    .append(Component.text("┌${" ".repeat(50)}", NamedTextColor.GOLD))
                    .append(Component.text("┐ ", NamedTextColor.LIGHT_PURPLE))
                    .append(mini.deserialize("\n<gradient:gold:light_purple><bold>EmortalMC"))
                    .append(Component.text("\n", NamedTextColor.GRAY)),
                Component.text()
                    .append(Component.text("\n ", NamedTextColor.GRAY))
                    .append(Component.text("${server.allPlayers.size} online", NamedTextColor.GRAY))
                    .append(Component.text("\n└${" ".repeat(50)}", NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text("┘ ", NamedTextColor.GOLD))
            )
        }
    }

}