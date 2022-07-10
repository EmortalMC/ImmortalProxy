package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.GameManager
import dev.emortal.divine.GameManager.sendToServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor


object SendAllCommand : DivineCommand() {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val sendAllNode = LiteralArgumentBuilder.literal<CommandSource>("sendall")
            .requires { hasPermission(it) }
            .executes {
                it.source.sendMessage(Component.text("Usage: /sendall <game>", NamedTextColor.RED))
                0
            }
            .build()

        val argNode = RequiredArgumentBuilder
            .argument<CommandSource, String>("game", StringArgumentType.string())
            .requires { hasPermission(it) }
            .suggests { context: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                GameManager.serverGameMap.keys.forEach {
                    builder.suggest(it)
                }
                return@suggests builder.buildFuture()
            }
            .executes {
                val username = (it.source as? Player)?.username ?: "Console"

                val gameName = (it.arguments["game"]?.result as? String)?.lowercase() ?: return@executes 0

                val serverName = GameManager.serverGameMap[gameName]
                if (serverName == null) {
                    it.source.sendMessage(Component.text("That isn't a game, try again", NamedTextColor.RED))
                    return@executes 0
                }

                val joiningActionbar = Component.text("Joining game $gameName ($serverName)", NamedTextColor.GREEN)
                val joiningMsg = Component.text()
                    .append(Component.text(username, NamedTextColor.GOLD))
                    .append(Component.text(" sent every player into ", NamedTextColor.GOLD))
                    .append(Component.text(gameName, NamedTextColor.GOLD))

                server.allPlayers.forEach {
                    it.sendMessage(joiningMsg)
                    it.sendActionBar(joiningActionbar)
                    it.sendToServer(serverName, gameName)
                }

                1
            }
            .build()

        sendAllNode.addChild(argNode)


        return sendAllNode

    }

    override fun hasPermission(source: CommandSource) = source.hasPermission("divine.sendall")

}