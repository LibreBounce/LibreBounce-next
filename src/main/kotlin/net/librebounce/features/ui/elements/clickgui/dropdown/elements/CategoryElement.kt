package net.librebounce.features.ui.elements.clickgui.dropdown.elements

import net.librebounce.features.module.Category
import net.librebounce.features.module.ModuleManager
import org.joml.Vector2f

class CategoryElement(val category: Category) : Element {
    private val position = Vector2f(0f, 0f)

    override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
        var moduleY = position.y - 5

        drawOutlineRect(position.x, position.y, position.x + 25, moduleY - 5)

        draw(
            category.displayName,
            position.x + 5,
            position.y - 5,
            00
        )

        for (module in ModuleManager[category]) {
            moduleY += 10

            draw(
                module.name,
                position.x + 5,
                moduleY,
                00
            )

            if (module.isActive) ModuleElement(module, Vector2f(moduleY, position.x))
        }
    }
}
