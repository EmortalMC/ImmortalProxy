package dev.emortal.divine.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.emortal.divine.GameManager.sendToServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor


object LobbyCommand : DivineCommand("l", "hub") {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val playNode = LiteralArgumentBuilder.literal<CommandSource>("lobby")
            .executes {
                val player = it.source as? Player
                if (player == null) {
                    it.source.sendMessage(Component.text("Lobby command cannot be used via console", NamedTextColor.RED))
                    return@executes 0
                }

                player.sendToServer("lobby", "lobby")

                1
            }
            .build()

        return playNode

    }

}