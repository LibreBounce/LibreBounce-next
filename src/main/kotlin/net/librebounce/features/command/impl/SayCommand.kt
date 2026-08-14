/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.features.command.impl

import net.librebounce.features.command.Command
import net.librebounce.utils.kotlin.StringUtils
import net.minecraft.client.Minecraft

object SayCommand : Command("say") {
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            Minecraft.getInstance().player.sendChat(StringUtils.toCompleteString(args, 1))
            chat("Message was sent to the chat.")
            return
        }
        chatSyntax("say <message...>")
    }
}
