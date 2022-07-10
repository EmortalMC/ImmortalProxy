package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.GameManager
import dev.emortal.divine.GameManager.sendToServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor


object SendCommand : DivineCommand() {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val playNode = LiteralArgumentBuilder.literal<CommandSource>("send")
            .requires { hasPermission(it) }
            .executes {
                it.source.sendMessage(Component.text("Usage: /send <player> <game>", NamedTextColor.RED))
                0
            }
            .build()

        val playerArgNode = RequiredArgumentBuilder
            .argument<CommandSource, String>("player", StringArgumentType.string())
            .requires { hasPermission(it) }
            .executes {
                0
            }
            .build()

        val gameArgNode = RequiredArgumentBuilder
            .argument<CommandSource, String>("game", StringArgumentType.string())
            .requires { hasPermission(it) }
            .suggests { context: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                GameManager.serverGameMap.keys.forEach {
                    builder.suggest(it)
                }
                return@suggests builder.buildFuture()
            }
            .executes {
                val gameName = (it.arguments["game"]?.result as? String)?.lowercase() ?: return@executes 0
                val playerArgument = it.arguments["player"]?.result as? String ?: return@executes 0

                server.getPlayer(playerArgument).ifPresentOrElse({ player ->
                    val serverName = GameManager.serverGameMap[gameName]
                    if (serverName == null) {
                        it.source.sendMessage(Component.text("That isn't a game, try again", NamedTextColor.RED))
                        return@ifPresentOrElse
                    }

                    player.sendActionBar(Component.text("Joining game $gameName ($serverName)", NamedTextColor.GREEN))
                    it.source.sendMessage(Component.text("Sending player ${player.username} to game ${gameName}"))
                    player.sendToServer(serverName, gameName)

                }, {
                    it.source.sendMessage(Component.text("Invalid player", NamedTextColor.RED))
                })



                1
            }
            .build()

        playerArgNode.addChild(gameArgNode)
        playNode.addChild(playerArgNode)


        return playNode

    }

    override fun hasPermission(source: CommandSource) = source.hasPermission("divine.send")

}