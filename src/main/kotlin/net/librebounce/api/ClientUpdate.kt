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
