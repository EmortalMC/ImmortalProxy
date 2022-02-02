package dev.emortal.immortalproxy

import dev.emortal.immortalproxy.GameManager.sendToServer
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component

object EventListener {

    @Subscribe
    fun pluginMessage(e: PluginMessageEvent) {
        val player = e.source as? Player ?: return

        if (e.identifier.id != "Immortal") return
        player.sendMessage(Component.text("Recieved plugin message"))

        val channel = e.dataAsDataStream().readLine()
        if (channel != "ServerSend") return

        val server = e.dataAsDataStream().readLine() ?: return

        player.sendMessage(Component.text("Recieved plugin message, sending to ${server}"))
        player.sendToServer(server)
    }

}