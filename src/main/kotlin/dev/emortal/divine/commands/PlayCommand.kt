package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.emortal.divine.GameManager
import dev.emortal.divine.GameManager.sendToServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor


object PlayCommand : DivineCommand() {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val playNode = LiteralArgumentBuilder.literal<CommandSource>("play")
            .executes {
                it.source.sendMessage(Component.text("Usage: /play <game>", NamedTextColor.RED))
                0
            }
            .build()

        val argNode = RequiredArgumentBuilder
            .argument<CommandSource, String>("game", StringArgumentType.string())
            .suggests { context: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                GameManager.serverGameMap.keys.forEach {
                    builder.suggest(it)
                }
                return@suggests builder.buildFuture()
            }
            .executes {
                val player = it.source as? Player
                if (player == null) {
                    it.source.sendMessage(Component.text("Play command cannot be used via console", NamedTextColor.RED))
                    return@executes 0
                }

                val gameName = (it.arguments["game"]?.result as? String)?.lowercase() ?: return@executes 0

                val serverName = GameManager.serverGameMap[gameName]
                if (serverName == null) {
                    player.sendMessage(Component.text("That isn't a game, try again", NamedTextColor.RED))
                    return@executes 0
                }

                player.sendToServer(serverName, gameName)

                1
            }
            .build()

        playNode.addChild(argNode)


        return playNode

    }

}