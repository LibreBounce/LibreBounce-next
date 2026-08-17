package net.librebounce.features.command.impl

import net.librebounce.features.command.Command
import net.librebounce.utils.io.MiscUtils

object UsernameCommand : Command("username", "ign") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val username = mc.player.name

        chat("Username: $username")
    }
}
