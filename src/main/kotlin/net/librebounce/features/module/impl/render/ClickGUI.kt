package net.librebounce.features.module.impl.render

import net.librebounce.LibreBounce.clickGui
import net.librebounce.features.module.Module
import net.librebounce.features.module.Category
import net.minecraft.client.Minecraft

object ClickGUI : Module("ClickGUI", Category.RENDER) {
    override fun onEnable() {
        Minecraft.getInstance().openScreen(clickGui)
    }
}
