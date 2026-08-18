package net.librebounce.features.module.impl.movement

import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.base.Category
import net.minecraft.client.options.GameOptions

object AutoWalk : Module("AutoWalk", Category.MOVEMENT, subjective = true, gameDetecting = false) {
    val onUpdate = handler<UpdateEvent> {
        mc.options.forwardKey.pressed = true
    }

    override fun onDisable() {
        mc.options.forwardKey.pressed = GameOptions.isPressed(mc.options.forwardKey)
    }
}
