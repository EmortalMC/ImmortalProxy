package dev.emortal.divine.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object PingCommand : DivineCommand() {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val pingNode = LiteralArgumentBuilder.literal<CommandSource>("ping")
            .executes {
                val player = it.source as? Player
                if (player == null) {
                    it.source.sendMessage(Component.text("0 lol", NamedTextColor.RED))
                    return@executes 0
                }

                it.source.sendMessage(
                    Component.text()
                        .append(Component.text("Your ping is: ", NamedTextColor.GRAY))
                        .append(Component.text(if (player.ping == -1L) "unknown :)" else player.ping.toString(), NamedTextColor.GREEN))
                        .append(Component.text("ms", NamedTextColor.GREEN))
                )

                1
            }
            .build()

        return pingNode

    }

}