package dev.emortal.divine.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

object RulesCommand : DivineCommand() {

    private val message = Component.text()
        .append(Component.text("              ", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
        .append(Component.text(" RULES ", NamedTextColor.RED, TextDecoration.BOLD))
        .append(Component.text("              \n\n", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
        .append(Component.text("1. ", NamedTextColor.RED, TextDecoration.BOLD))
        .append(Component.text("No NSFW or obscene content. This includes text, images, or links featuring nudity, sex, violence, or other graphically disturbing content.\n", NamedTextColor.GRAY))
        .append(Component.text("2. ", NamedTextColor.RED, TextDecoration.BOLD))
        .append(Component.text("No cheating (Most mods will be fine, contact staff member if unsure, but otherwise use common sense)\n", NamedTextColor.GRAY))
        .append(Component.text("3. ", NamedTextColor.RED, TextDecoration.BOLD))
        .append(Component.text("No spam or self promotion (server invites, server IPs, advertisements, etc) whatsoever. (This includes direct messages)\n", NamedTextColor.GRAY))
        .append(Component.text("4. ", NamedTextColor.RED, TextDecoration.BOLD))
        .append(Component.text("Treat everyone with respect (they deserve it). Absolutely no harassment, witch hunting, sexism, racism, or hate speech will be tolerated", NamedTextColor.GRAY))


    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val rulesNode = LiteralArgumentBuilder.literal<CommandSource>("rules")
            .executes {
                it.source.sendMessage(message)

                1
            }
            .build()

        return rulesNode

    }

}