package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.player.PlayerChatEvent
import dev.emortal.divine.DivinePlugin.Companion.server
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

object SudoCommand : DivineCommand() {

    val mini = MiniMessage.miniMessage()

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val sudoNode = LiteralArgumentBuilder.literal<CommandSource>("sudo")
            .requires { hasPermission(it) }
            .executes {
                it.source.sendMessage(Component.text("Usage /sudo <player> <command or message>", NamedTextColor.RED))

                1
            }
            .build()

        val playerArgNode = RequiredArgumentBuilder
            .argument<CommandSource, String>("player", StringArgumentType.string())
            .requires { SendCommand.hasPermission(it) }
            .executes {
                0
            }
            .build()

        val inputArg = RequiredArgumentBuilder
            .argument<CommandSource, String>("input", StringArgumentType.greedyString())
            .requires { hasPermission(it) }
            .executes {
                val message = it.arguments["input"]!!.result as String
                val playerArgument = it.arguments["player"]?.result as? String ?: return@executes 0

                server.getPlayer(playerArgument).ifPresentOrElse({ player ->
                    if (message.startsWith("/")) {
                        player.spoofChatInput(message)
                    } else {
                        server.eventManager.fireAndForget(PlayerChatEvent(player, message))
                    }
                }, {
                    it.source.sendMessage(Component.text("Invalid player", NamedTextColor.RED))
                })

                1
            }
            .build()

        playerArgNode.addChild(inputArg)
        sudoNode.addChild(playerArgNode)

        return sudoNode

    }

    override fun hasPermission(source: CommandSource) = source.hasPermission("divine.sudo")

}