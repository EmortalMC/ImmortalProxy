package dev.emortal.divine.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import dev.emortal.divine.db.MongoStorage
import dev.emortal.divine.db.MongoStorage.Companion.mongoScope
import kotlinx.coroutines.launch

object DropPlaytimeCommand : DivineCommand() {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val discordNode = LiteralArgumentBuilder.literal<CommandSource>("dropplaytime")
            .executes {
                if (it.source !is ConsoleCommandSource) return@executes 0

                mongoScope.launch {
                    MongoStorage.playerUptime?.drop()
                }

                1
            }
            .build()

        return discordNode

    }

}