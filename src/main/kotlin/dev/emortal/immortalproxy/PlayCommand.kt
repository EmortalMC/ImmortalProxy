package dev.emortal.immortalproxy

import dev.emortal.immortalproxy.GameManager.sendToServer
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component

object PlayCommand : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation?) {
        val player = invocation!!.source() as? Player ?: return
        val gameName = invocation.arguments()[0]

        player.sendMessage(Component.text("Sending to game server $gameName"))
        player.sendToServer(gameName)
    }

}