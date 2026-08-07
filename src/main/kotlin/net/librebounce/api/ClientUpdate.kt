/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.api

import net.librebounce.LibreBounce
import java.util.*

object ClientUpdate {
    val gitInfo = Properties().also {
        val inputStream = LibreBounce::class.java.classLoader.getResourceAsStream("git.properties")

        if (inputStream != null) it.load(inputStream)
        else it["git.build.version"] = "unofficial"
    }
}
