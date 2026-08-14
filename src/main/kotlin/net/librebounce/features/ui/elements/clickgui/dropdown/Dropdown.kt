package net.librebounce.features.ui.elements.clickgui.dropdown

import net.librebounce.features.module.Category
import net.librebounce.features.ui.elements.clickgui.dropdown.elements.CategoryElement
import net.minecraft.client.gui.screen.Screen

object Dropdown : Screen() {
    override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
        for (category in Category.entries.toTypedArray()) {
            CategoryElement(category)
        }
    }
}
