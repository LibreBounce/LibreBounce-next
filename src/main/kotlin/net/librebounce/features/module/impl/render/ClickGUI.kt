package net.librebounce.features.module.impl.render

import net.librebounce.LibreBounce.clickGui
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.base.Category
import net.minecraft.client.Minecraft

object ClickGUI : Module("ClickGUI", Category.RENDER) {
    override fun onEnable() {
        mc.openScreen(clickGui)
    }
}
