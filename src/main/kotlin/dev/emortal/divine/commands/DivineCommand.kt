package dev.emortal.divine.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import dev.emortal.divine.DivinePlugin.Companion.server

abstract class DivineCommand(vararg val aliases: String) {

    open fun hasPermission(source: CommandSource): Boolean = true

    abstract fun createBrigadierCommand(): LiteralCommandNode<CommandSource>

    fun register() {
        val cmd = BrigadierCommand(createBrigadierCommand())
        val meta = server.commandManager.metaBuilder(cmd)
            .aliases(*aliases)
            .build()
        server.commandManager.register(meta, cmd)
    }

}