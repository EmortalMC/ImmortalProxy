package dev.emortal.divine.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object VoteCommand : DivineCommand("vote") {

    override fun createBrigadierCommand(): LiteralCommandNode<CommandSource> {

        val voteNode = LiteralArgumentBuilder.literal<CommandSource>("vote")
            .executes {
                it.source.sendMessage(Component.text("Usage: /vote <option>", NamedTextColor.RED))

                1
            }
            .build()

        val optionArg = RequiredArgumentBuilder
            .argument<CommandSource, Int>("option", IntegerArgumentType.integer(0))
            .executes {
                val option = it.arguments["option"]!!.result as Int

                val activePoll = null//PollCommand.activePoll

                if (activePoll == null) {
                    it.source.sendMessage(Component.text("There is not an active poll", NamedTextColor.RED))

                    return@executes 1
                }

                it.source.sendMessage(Component.text("Your vote has been recorded!", NamedTextColor.GREEN))
                // TODO: FIX double votes
                //activePoll.votes[option - 1]++

                1
            }
            .build()

        voteNode.addChild(optionArg)

        return voteNode

    }

}