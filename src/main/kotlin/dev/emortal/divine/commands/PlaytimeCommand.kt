package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.emortal.divine.DivinePlugin
import dev.emortal.divine.DivinePlugin.Companion.mongoStorage
import dev.emortal.divine.EventListener
import dev.emortal.divine.db.MongoStorage.Companion.mongoScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

//object PlaytimeCommand : DivineCommand() {
//
//    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {
//
//        val playtimeNode = LiteralArgumentBuilder.literal<CommandSource>("playtime")
//            .executes {
//                val player = it.source as? Player ?: return@executes 0
//
//                mongoScope.launch {
//                    val uptimeObj = mongoStorage.getUptime(player.uniqueId) ?: return@launch
//
//                    val lastLoginTime = EventListener.loginTimesMap[player.uniqueId]
//
//                    println(EventListener.loginTimesMap)
//                    println(EventListener.lastServerMap)
//
//                    val extraSeconds = ((System.currentTimeMillis() - (lastLoginTime ?: System.currentTimeMillis())) / 1000L)
//                    println(extraSeconds)
//                    val secondsUptime = uptimeObj.playtimeMap.map { it.value }.sumOf { it }
//
//                    it.source.sendMessage(
//                        Component.text()
//                            .append(Component.text("Your playtime is: ", NamedTextColor.GRAY))
//                            .append(Component.text((secondsUptime + extraSeconds).parsed(), NamedTextColor.GREEN))
//                    )
//                }
//
//
//                1
//            }
//            .build()
//
//        val playerArgNode = RequiredArgumentBuilder
//            .argument<CommandSource, String>("player", StringArgumentType.string())
//            .executes {
//                val playerArgument = it.arguments["player"]?.result as? String ?: return@executes 0
//
//                DivinePlugin.server.getPlayer(playerArgument).ifPresentOrElse({ player ->
//                    mongoScope.launch {
//                        val uptimeObj = mongoStorage.getUptime(player.uniqueId) ?: return@launch
//
//                        val lastLoginTime = EventListener.loginTimesMap[player.uniqueId]
//
//                        println(EventListener.loginTimesMap)
//                        println(EventListener.lastServerMap)
//
//                        val extraSeconds = ((System.currentTimeMillis() - (lastLoginTime ?: System.currentTimeMillis())) / 1000L)
//                        println(extraSeconds)
//                        val secondsUptime = uptimeObj.playtimeMap.map { it.value }.sumOf { it }
//
//                        it.source.sendMessage(
//                            Component.text()
//                                .append(Component.text("${player.username}'s playtime is: ", NamedTextColor.GRAY))
//                                .append(Component.text((secondsUptime + extraSeconds).parsed(), NamedTextColor.GREEN))
//                        )
//                    }
//                }, {
//                    it.source.sendMessage(Component.text("Invalid player", NamedTextColor.RED))
//                })
//
//
//                1
//            }
//            .build()
//
//        playtimeNode.addChild(playerArgNode)
//
//        return playtimeNode
//
//    }
//
//}
//
//fun Long.parsed(): String {
//    if (this == 0L) return "0s"
//
//    val stringBuilder = StringBuilder()
//    val days = this / 86400
//    val hours = (this % 86400) / 3600
//    val minutes = (this % 3600) / 60
//    val seconds = this % 60
//
//    if (days > 0) stringBuilder.append(days).append("d ")
//    if (hours > 0) stringBuilder.append(hours).append("h ")
//    if (minutes > 0) stringBuilder.append(minutes).append("m ")
//    if (seconds > 0) stringBuilder.append(seconds).append("s ")
//
//    return stringBuilder.toString().trim()
//}