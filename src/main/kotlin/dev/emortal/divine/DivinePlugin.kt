package dev.emortal.divine

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import dev.emortal.divine.commands.PlayCommand
import dev.emortal.divine.commands.SendAllCommand
import dev.emortal.divine.commands.SendCommand
import dev.emortal.divine.config.ConfigHelper
import dev.emortal.divine.config.DivineConfig
import dev.emortal.divine.utils.RedisStorage.redisson
import net.luckperms.api.LuckPerms
import java.nio.file.Path
import java.util.logging.Logger

@Plugin(
    id = "divine",
    name = "Divine",
    version = "1.0.0",
    description = "Handles proxy business, such as DN or YM",
    dependencies = [Dependency(id = "luckperms")]
)
class DivinePlugin @Inject constructor(private val server: ProxyServer, private val logger: Logger) {

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        divineConfig = ConfigHelper.initConfigFile(configPath, DivineConfig())

        Companion.server = server
        GameManager.initListener()

        redisson.getTopic("proxyhello").publishAsync("")

        server.eventManager.register(this, EventListener(this))

        PlayCommand.register()
        SendCommand.register()
        SendAllCommand.register()

        logger.info("[Divine] has been enabled!")

    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        redisson.shutdown()
    }

    companion object {
        lateinit var server: ProxyServer
        lateinit var luckperms: LuckPerms

        lateinit var divineConfig: DivineConfig
        val configPath = Path.of("./divineconfig.json")
    }

}