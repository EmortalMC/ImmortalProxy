package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import dev.emortal.divine.DivinePlugin.Companion.plugin
import dev.emortal.divine.DivinePlugin.Companion.server
import dev.emortal.divine.Poll
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.time.Duration

object PollCommand : DivineCommand("poll") {

    var activePoll: Poll? = null

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val pollNode = LiteralArgumentBuilder.literal<CommandSource>("poll")
            .requires { hasPermission(it) }
            .executes {
                it.source.sendMessage(Component.text("Usage: /poll <duration> <options separated by ;>", NamedTextColor.RED))

                1
            }
            .build()

        val durationArg = RequiredArgumentBuilder
            .argument<CommandSource, String>("duration", StringArgumentType.word())
            .requires { hasPermission(it) }
            .build()

        val messageArg = RequiredArgumentBuilder
            .argument<CommandSource, String>("message", StringArgumentType.greedyString())
            .requires { hasPermission(it) }
            .executes {
                val options = (it.arguments["message"]!!.result as String).split(";")
                val durationString = (it.arguments["duration"]!!.result as String)

                val messageComponent = Component.text()
                    .append(Component.text("POLL\n", NamedTextColor.GOLD, TextDecoration.BOLD))

                val duration = Duration.parse("PT$durationString")
                activePoll = Poll(duration, options)

                options.forEachIndexed { i, it ->
                    messageComponent
                        .append(
                            Component.text()
                                .append(Component.newline())
                                .append(Component.space())
                                .append(Component.text(i, NamedTextColor.YELLOW))
                                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(it, NamedTextColor.GRAY))
                                .build()
                                .hoverEvent(HoverEvent.showText(Component.text("Click to select option ${i}", NamedTextColor.YELLOW)))
                                .clickEvent(ClickEvent.runCommand("/vote ${i}"))
                        )
                }

                server.allPlayers.forEach { plr ->
                    plr.sendMessage(messageComponent)
                }

                server.scheduler.buildTask(plugin) {
                    val endComponent = Component.text()
                        .append(Component.text("POLL RESULTS\n", NamedTextColor.GOLD, TextDecoration.BOLD))

                    activePoll!!.options.forEachIndexed { i, it ->
                        endComponent
                            .append(Component.newline())
                            .append(Component.space())
                            .append(Component.text(i, NamedTextColor.YELLOW))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(it, NamedTextColor.GRAY))
                    }
                    activePoll = null

                    server.allPlayers.forEach { plr ->
                        plr.sendMessage(endComponent)
                    }
                }.delay(duration).schedule()

                1
            }
            .build()

        pollNode.addChild(durationArg)
        durationArg.addChild(messageArg)

        return pollNode

    }

    override fun hasPermission(source: CommandSource) = source.hasPermission("divine.poll")

}