package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.emortal.divine.DivinePlugin
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.GameManager
import dev.emortal.divine.GameManager.sendToServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor


object SpectateCommand : DivineCommand() {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val spectateNode = LiteralArgumentBuilder.literal<CommandSource>("spectate")
            .executes {
                it.source.sendMessage(Component.text("Usage: /spectate <player username>", NamedTextColor.RED))
                0
            }
            .build()

        val playerArgNode = RequiredArgumentBuilder
            .argument<CommandSource, String>("player", StringArgumentType.string())
            .executes {
                val executor = it.source as? Player
                if (executor == null) {
                    it.source.sendMessage(Component.text("Spectate command cannot be used via console", NamedTextColor.RED))
                    return@executes 0
                }
                val playerArgument = it.arguments["player"]?.result as? String ?: return@executes 0

                server.getPlayer(playerArgument).ifPresentOrElse({ player ->
                    executor.sendToServer(player.currentServer.get().serverInfo.name, "spectate", true, player.uniqueId)
                }, {
                    it.source.sendMessage(Component.text("Invalid player", NamedTextColor.RED))
                })

                0
            }
            .build()

        spectateNode.addChild(playerArgNode)


        return spectateNode

    }

}