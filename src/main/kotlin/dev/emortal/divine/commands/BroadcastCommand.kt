package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import dev.emortal.divine.DivinePlugin.Companion.server
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

object BroadcastCommand : DivineCommand("bc", "announce") {

    val mini = MiniMessage.miniMessage()

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val broadcastNode = LiteralArgumentBuilder.literal<CommandSource>("broadcast")
            .requires { hasPermission(it) }
            .executes {
                it.source.sendMessage(Component.text("You should probably specify a message...", NamedTextColor.RED))

                1
            }
            .build()

        val messageArg = RequiredArgumentBuilder
            .argument<CommandSource, String>("message", StringArgumentType.greedyString())
            .requires { hasPermission(it) }
            .executes {
                var message = mini.deserialize(it.arguments["message"]!!.result as String)

                val messageComponent = Component.text()
                    .append(Component.text("ANNOUNCEMENT", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                    .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                    .append(message)
                    .build()

                server.allPlayers.forEach { plr ->
                    plr.sendMessage(messageComponent)
                }

                1
            }
            .build()

        broadcastNode.addChild(messageArg)

        return broadcastNode

    }

    override fun hasPermission(source: CommandSource) = source.hasPermission("divine.broadcast")

}