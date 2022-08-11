package dev.emortal.divine

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import dev.emortal.divine.commands.*
import dev.emortal.divine.config.ConfigHelper
import dev.emortal.divine.config.DivineConfig
import dev.emortal.divine.db.MongoStorage
import dev.emortal.divine.utils.RedisStorage.redisson
import net.luckperms.api.LuckPerms
import org.litote.kmongo.serialization.SerializationClassMappingTypeService
import java.nio.file.Path
import java.util.logging.Logger


@Plugin(
    id = "divine",
    name = "Divine",
    version = "1.0.0",
    description = "Handles proxy business, such as DN or YM",
    dependencies = [Dependency(id = "luckperms"), Dependency(id = "datadependency")]
)
class DivinePlugin @Inject constructor(private val server: ProxyServer, private val logger: Logger) {

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        divineConfig = ConfigHelper.initConfigFile(configPath, DivineConfig())
        //luckperms = LuckPermsProvider.get()
        plugin = this

        System.setProperty("org.litote.mongo.mapping.service", SerializationClassMappingTypeService::class.qualifiedName!!)

        //val mini = MiniMessage.miniMessage()

        mongoStorage = MongoStorage()
        mongoStorage.init()

        Companion.server = server
        GameManager.initListener()

        redisson.getTopic("proxyhello").publishAsync("")

        server.eventManager.register(this, EventListener(this))

        PlayCommand.register()
        SpectateCommand.register()
        SendCommand.register()
        SendAllCommand.register()
        LobbyCommand.register()
        PingCommand.register()
        BroadcastCommand.register()
        DiscordCommand.register()
        RulesCommand.register()
        SudoCommand.register()
        //PollCommand.register()
        //VoteCommand.register()
        PlaytimeCommand.register()
        DropPlaytimeCommand.register()

        logger.info("[Divine] has been enabled!")

    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        server.allPlayers.forEach {
            logger.info("Saving uptime for player ${it.username}")
        }

        redisson.shutdown()
    }

    companion object {
        lateinit var server: ProxyServer
        lateinit var luckperms: LuckPerms
        lateinit var plugin: DivinePlugin
        lateinit var mongoStorage: MongoStorage

        lateinit var divineConfig: DivineConfig
        val configPath = Path.of("./divineconfig.json")
    }

}