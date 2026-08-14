package net.librebounce.features.ui.elements.clickgui.dropdown.elements

import io.github.axolotlclient.rendering.font.DefaultFont
import io.github.axolotlclient.rendering.font.Font
import net.librebounce.features.module.Category
import net.librebounce.features.module.ModuleManager
import net.librebounce.utils.render.ColorUtils.withAlpha
import net.librebounce.utils.render.RenderUtils.drawOutlineRoundedRect
import net.librebounce.utils.render.RenderUtils.drawString
import org.joml.Vector2i
import java.awt.Color

class CategoryElement(val category: Category) : Element {
    private val position = Vector2i(0, 0)

    override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
        var moduleY = position.y - 5

        drawOutlineRoundedRect(
            position.x,
            position.y,
            position.x + 25,
            moduleY - 5,
            Color.black.withAlpha(100),
            0f,
            1f
        )

        drawString(
            DefaultFont.inter(),
            category.displayName,
            position.x + 5f,
            position.y - 5f,
            Color.WHITE.withAlpha(255)
        )

        for (module in ModuleManager[category]) {
            moduleY += 10

            drawString(
                DefaultFont.inter(),
                module.name,
                position.x + 5f,
                moduleY.toFloat(),
                Color.white.withAlpha(255)
            )

            if (module.isActive) ModuleElement(module, Vector2i(moduleY, position.x))
        }
    }
}
