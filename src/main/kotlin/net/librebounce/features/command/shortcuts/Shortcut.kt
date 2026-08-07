/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.features.command.shortcuts

import net.librebounce.features.command.Command

class Shortcut(val name: String, val script: List<Pair<Command, Array<String>>>) : Command(name) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) = script.forEach { it.first.execute(it.second) }
}
