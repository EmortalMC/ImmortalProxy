package dev.emortal.divine.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import dev.emortal.divine.EventListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object PackerRefreshCommand : DivineCommand("packerrefresh") {


    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val packerRefreshNode = LiteralArgumentBuilder.literal<CommandSource>("poll")
            .requires { hasPermission(it) }
            .executes {
                EventListener.hash = EventListener.refreshSha1()

                it.source.sendMessage(Component.text("Refreshed resource pack hash", NamedTextColor.GREEN))

                1
            }
            .build()


        return packerRefreshNode

    }

    override fun hasPermission(source: CommandSource) = source.hasPermission("divine.packerrefresh")

}