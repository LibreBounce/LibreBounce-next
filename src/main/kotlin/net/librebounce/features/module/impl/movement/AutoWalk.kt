package net.librebounce.features.module.impl.movement

import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.Module
import net.librebounce.features.module.Category
import net.minecraft.client.options.GameOptions

object AutoWalk : Module("AutoWalk", Category.MOVEMENT, subjective = true, gameDetecting = false) {
    val onUpdate = handler<UpdateEvent> {
        options.forwardKey.pressed = true
    }

    override fun onDisable() {
        options.forwardKey.pressed = GameOptions.isPressed(options.forwardKey)
    }
}
