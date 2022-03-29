package dev.emortal.divine.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

object DiscordCommand : DivineCommand() {

    private val message = Component.text()
        .append(Component.text("Click ", NamedTextColor.GRAY))
        .append(
            Component.text("HERE", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("https://discord.gg/TZyuMSha96", NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.openUrl("https://discord.gg/TZyuMSha96"))
        )
        .append(Component.text(" to join our Discord!", NamedTextColor.GRAY))

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val discordNode = LiteralArgumentBuilder.literal<CommandSource>("discord")
            .executes {
                it.source.sendMessage(message)

                1
            }
            .build()

        return discordNode

    }

}