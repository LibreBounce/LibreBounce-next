/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.utils.client

import net.minecraft.client.Minecraft
//import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.resource.Identifier
import net.ornithemc.osl.lifecycle.api.client.MinecraftInstance

interface MinecraftInstance {
    val mc: Minecraft
        get() = MinecraftInstance.get()

    /*companion object {
        @JvmField
        val mc: Minecraft = MinecraftInstance.get()
    }*/
}

/*fun Minecraft.playSound(
    resourceLocation: Identifier,
    pitch: Float = 1.0f,
) = synchronized(this.soundHandler) {
    this.soundHandler.playSound(PositionedSoundRecord.create(resourceLocation, pitch))
}*/

fun String.asIdentifier() = Identifier(this)
