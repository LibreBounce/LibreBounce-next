package net.librebounce.features.module.impl.render

import net.librebounce.LibreBounce.moduleManager
import net.librebounce.event.Render2DEvent
import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.Category
import net.librebounce.features.module.Module
import net.librebounce.utils.render.ColorSettingsInteger
import net.minecraft.client.Minecraft
import java.awt.Color

object Arraylist : Module("Arraylist", Category.RENDER, defaultState = true) {
    val onRender2D = handler<Render2DEvent> {
        val color = ColorSettingsInteger(this, "Color").with(Color.WHITE)

        var yOffset = 5

        for (module in moduleManager) {
            Minecraft.getInstance().textRenderer.draw("$module.name", 0, yOffset, 4673984)
            yOffset += 5
        }
    }
}
