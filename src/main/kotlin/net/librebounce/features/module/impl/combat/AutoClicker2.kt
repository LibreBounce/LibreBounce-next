package net.librebounce.features.module.impl.combat

import net.librebounce.event.Render3DEvent
import net.librebounce.event.handler
import net.librebounce.features.module.Category
import net.librebounce.features.module.Module
import net.librebounce.features.module.base.ClickingSettings
import net.minecraft.client.options.KeyBinding

object AutoClicker2 : Module("AutoClicker2", Category.COMBAT) {
    private val left by boolean("Left", false)
    private val leftSettings = ClickingSettings(this, "Left", { left })

    val onRender3D = handler<Render3DEvent> {
        mc.player?.let { player ->
            if (left && mc.options.attackKey.isPressed && !mc.options.useKey.isPressed && leftSettings.canClick()) {
                repeat(1 + leftSettings.doubleClick) {
                    KeyBinding.click(mc.options.attackKey.keyCode)
                }
            }
        }
    }
}
