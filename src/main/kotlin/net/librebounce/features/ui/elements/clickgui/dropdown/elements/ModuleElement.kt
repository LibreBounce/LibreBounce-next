package net.librebounce.features.ui.elements.clickgui.dropdown.elements

import io.github.axolotlclient.rendering.font.DefaultFont
import net.librebounce.config.*
import net.librebounce.features.module.base.Module
import net.librebounce.features.ui.elements.clickgui.dropdown.elements.values.*
import net.librebounce.utils.render.ColorUtils.withAlpha
import net.librebounce.utils.render.RenderUtils.drawRoundedRect
import net.librebounce.utils.render.RenderUtils.drawString
import org.joml.Vector2i
import java.awt.Color

/*
 * This element is meant to show the values of the module,
 * while allowing you to change them as fit.
 *
 * Rendering the module name itself along with the activated state
 * is handled by CategoryElement, which calls this element when a module's sidebar
 * is enabled.
 */
class ModuleElement(val module: Module, var sidebarLocation: Vector2i) : Element {
    private var valueY = sidebarLocation.y

    override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
        sidebarLocation = Vector2i(sidebarLocation.x + 20, sidebarLocation.y + 20)

        drawRoundedRect(
            sidebarLocation.x,
            sidebarLocation.y,
            sidebarLocation.x + 20,
            valueY + 10,
            Color.BLACK.withAlpha(100)
        )

        val moduleValues = module.values.filter { it.shouldRender() }

        for (value in moduleValues) {
            valueY += 10

            drawString(
                DefaultFont.inter(),
                value.name,
                sidebarLocation.x + 5f,
                valueY.toFloat(),
                Color.WHITE
            )

            when (value) {
                is FloatValue -> FloatElement(value)
                //is FloatRangeValue -> FloatRangeElement(value)
                is IntValue -> IntElement(value)
                //is IntRangeValue -> IntRangeElement(value)
                is TextValue -> TextElement(value)
                is ListValue -> ListElement(value)
                else -> return
                //is BlockValue -> BlockElement(value)
            }
        }
    }
}
