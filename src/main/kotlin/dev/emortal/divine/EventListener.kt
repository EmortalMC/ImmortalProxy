package dev.emortal.divine

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.scheduler.ScheduledTask
import dev.emortal.divine.DivinePlugin.Companion.mongoStorage
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.db.MongoStorage
import dev.emortal.divine.db.PlayerUptime
import dev.emortal.divine.utils.RedisStorage
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.LoggerFactory
import java.net.URL
import java.security.MessageDigest
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class EventListener(val plugin: DivinePlugin) {

    companion object {
        val lastServerMap = ConcurrentHashMap<UUID, String>()
        val loginTimesMap = ConcurrentHashMap<UUID, Long>()


        private const val url = "https://github.com/EmortalMC/Resourcepack/releases/download/latest/pack.zip"

        private val resourcePackPrompt = Component.text()
            .append(Component.text("This resource pack is required.\n", NamedTextColor.RED))
            .append(Component.text("By clicking accept, this prompt will no longer appear after future logins", NamedTextColor.GRAY))
            .build()

        var lastRefreshed = 0L

        var hash = refreshSha1()
            set(value) {
                resourcePackBuilder = server.createResourcePackBuilder(url)
                    .setHash(value)
                    .setPrompt(resourcePackPrompt)
                    .setShouldForce(true)
                    .build()

                field = value
            }

        private var resourcePackBuilder = server.createResourcePackBuilder(url)
            .setHash(hash)
            .setPrompt(resourcePackPrompt)
            .setShouldForce(true)
            .build()

        fun refreshSha1(): ByteArray {
            lastRefreshed = System.currentTimeMillis()

            val digest = MessageDigest.getInstance("SHA-1")
            val fileInputStream = URL(url).openStream()
            var n = 0
            val buffer = ByteArray(8192)
            while (n != -1) {
                n = fileInputStream.read(buffer)
                if (n > 0)
                    digest.update(buffer, 0, n)
            }
            fileInputStream.close()
            return digest.digest()
        }
    }

    val libertyBansCommands = listOf(
        "libertybans",
        "ipwarn",
        "ipkick",
        "banlist",
        "unmuteip",
        "alts",
        "mute",
        "ipmute",
        "history",
        "ban",
        "warn",
        "unban",
        "unbanip",
        "unwarn",
        "unwarnip",
        "ipban",
        "kick",
        "accounthistory",
        "warns",
        "blame",
        "unmute",
        "mutelist",
        "unbanip",
    )
    val luckpermsCommands = listOf(
        "luckperms",
        "lp",
        "lpv",
        "luckpermsvelocity",
        "perm",
        "perms",
        "permissions",
        "permission"
    )
    @Subscribe
    fun onPlayerTab(event: PlayerAvailableCommandsEvent) {
        val iteration = event.rootNode.children.iterator()
        while (iteration.hasNext()) {
            val next = iteration.next()

            if (libertyBansCommands.contains(next.name) && !event.player.hasPermission("divine.seelibertybans")) {
                iteration.remove()
            }
            if (luckpermsCommands.contains(next.name) && !event.player.hasPermission("divine.seeluckperms")) {
                iteration.remove()
            }
        }



    }

    @Subscribe
    fun onPlayerResourceStatus(event: PlayerResourcePackStatusEvent) {
        when (event.status) {
            PlayerResourcePackStatusEvent.Status.DECLINED ->
                event.player.disconnect(
                    Component.text(
                        "Using the resource pack is required. It isn't big and only has to be downloaded once.\nIf the dialog is didn't appear, you need to enable 'Server Resource Packs' in the server settings.",
                        NamedTextColor.GRAY
                    )
                )

            PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD -> {
                event.player.sendMessage(Component.text(
                    "The resource pack download failed.\nIf the issue persists, contact a staff member",
                    NamedTextColor.RED
                ))
                if (lastRefreshed + 120000 < System.currentTimeMillis()){
                    hash = refreshSha1()
                }
                server.scheduler.buildTask(plugin) {

                    event.player.sendResourcePackOffer(resourcePackBuilder)
                }.delay(Duration.ofSeconds(5)).schedule()
            }

            else -> {

            }
        }
    }


    val logger = LoggerFactory.getLogger("EventListener")

    var limboReconnectTask: ScheduledTask? = null

    @Subscribe
    fun playerInitialServer(e: PlayerChooseInitialServerEvent) {
        e.player.sendResourcePackOffer(resourcePackBuilder)
    }

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
            .append(Component.text("\nWe post all announcements and votes there\n", NamedTextColor.GRAY))
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
        val lastServer = lastServerMap[e.player.uniqueId] ?: return
        val loginTime = loginTimesMap[e.player.uniqueId]
        MongoStorage.mongoScope.launch {
            val playtimeMap = (mongoStorage.getUptime(e.player.uniqueId)?.playtimeMap ?: mutableMapOf())

            logger.info(loginTime.toString())
            if (!playtimeMap.containsKey(lastServer)) playtimeMap[lastServer] = 0
            playtimeMap[lastServer] = playtimeMap[lastServer]!! + (System.currentTimeMillis() - (loginTime ?: System.currentTimeMillis())) / 1000

            val newObj = PlayerUptime(e.player.uniqueId.toString(), playtimeMap)
            mongoStorage.saveUptime(newObj)
            logger.info("Saved player uptime")
        }

        refreshTablist()

        lastServerMap.remove(e.player.uniqueId)
        loginTimesMap.remove(e.player.uniqueId)
    }


    @Subscribe
    fun playerJoinServer(e: ServerConnectedEvent) {
        //logger.info("Connected!")

        refreshTablist()

        val loginTime = loginTimesMap[e.player.uniqueId]
        e.previousServer.ifPresent {
            MongoStorage.mongoScope.launch {
                val playtimeMap = mongoStorage.getUptime(e.player.uniqueId)?.playtimeMap ?: mutableMapOf()


                if (!playtimeMap.containsKey(it.serverInfo.name)) playtimeMap[it.serverInfo.name] = 0
                playtimeMap[it.serverInfo.name] = playtimeMap[it.serverInfo.name]!! + (System.currentTimeMillis() - (loginTime ?: System.currentTimeMillis())) / 1000

                val newObj = PlayerUptime(e.player.uniqueId.toString(), playtimeMap)
                mongoStorage.saveUptime(newObj)
                logger.info("Saved player uptime")
            }
        }

        lastServerMap[e.player.uniqueId] = e.server.serverInfo.name
        loginTimesMap[e.player.uniqueId] = System.currentTimeMillis()


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
                    .append(Component.text("\nmc.emortal.dev", NamedTextColor.DARK_GRAY))
                    .append(Component.text("\n└${" ".repeat(50)}", NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text("┘ ", NamedTextColor.GOLD))
            )
        }
    }

}