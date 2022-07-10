package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.emortal.divine.DivinePlugin
import dev.emortal.divine.EventListener
import dev.emortal.divine.db.MongoStorage
import kotlinx.coroutines.launch
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

        val playerArgNode = RequiredArgumentBuilder
            .argument<CommandSource, String>("player", StringArgumentType.string())
            .executes {
                val playerArgument = it.arguments["player"]?.result as? String ?: return@executes 0

                DivinePlugin.server.getPlayer(playerArgument).ifPresentOrElse({ player ->
                    it.source.sendMessage(
                        Component.text()
                            .append(Component.text("${player.username}'s ping is: ", NamedTextColor.GRAY))
                            .append(Component.text(if (player.ping == -1L) "unknown :)" else player.ping.toString(), NamedTextColor.GREEN))
                            .append(Component.text("ms", NamedTextColor.GREEN))
                    )
                }, {
                    it.source.sendMessage(Component.text("Invalid player", NamedTextColor.RED))
                })


                1
            }
            .build()

        pingNode.addChild(playerArgNode)

        return pingNode

    }

}