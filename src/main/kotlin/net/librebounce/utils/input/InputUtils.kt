package net.librebounce.utils.input

import net.librebounce.utils.client.MinecraftInstance
import net.minecraft.client.options.KeyBinding.click

object InputUtils : MinecraftInstance {

    /*
     * Request for a click to be performed; this is merely an abstraction, for the time being.
     *
     * Buttons supported:
     * - Attack (1)
     * - Middle (2)
     * - Use (3)
     */
    
    fun requestClick(button: Int, amount: Int = 1) {
        val key = when (button) {
            1 -> mc.options.attackKey.keyCode
            //2 -> mc.options.selectKey.keyCode
            3 -> mc.options.useKey.keyCode
            else -> mc.options.attackKey.keyCode
        }

        repeat(amount) {
            click(key)
        }
    }
}