package net.librebounce.features.ui.elements.clickgui.dropdown.elements

import net.librebounce.config.FloatRangeValue
import net.librebounce.config.FloatValue
import net.librebounce.config.IntRangeValue
import net.librebounce.config.IntValue
import net.librebounce.config.ListValue
import net.librebounce.config.TextValue
import net.librebounce.features.module.Module
import net.librebounce.features.ui.elements.clickgui.dropdown.elements.values.FloatElement
import net.librebounce.features.ui.elements.clickgui.dropdown.elements.values.IntElement
import net.librebounce.features.ui.elements.clickgui.dropdown.elements.values.ListElement
import net.librebounce.features.ui.elements.clickgui.dropdown.elements.values.TextElement
import org.joml.Vector2f
import java.awt.Color

class ModuleElement(val module: Module, var sidebarLocation: Vector2f) : Element {
    private var valueY = sidebarLocation.y

    override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
        sidebarLocation = Vector2f(sidebarLocation.x + 20f, sidebarLocation.y + 20f)

        renderBox(sidebarLocation.x, sidebarLocation.y, sidebarLocation.x + 20f, sidebarLocation. Color.BLACK.withAlpha(100))

        for (value in Module.values) {
            valueY += 10f

            draw(value.name, sidebarLocation.x + 5f, valueY, Color.WHITE)

            when (value) {
                is FloatValue -> FloatElement(value)
                is FloatRangeValue -> FloatRangeElement(value)
                is IntValue -> IntElement(value)
                is IntRangeValue -> IntRangeElement(value)
                is TextValue -> TextElement(value)
                is ListValue -> ListElement(value)
                //is BlockValue -> BlockElement(value)
            }
        }
    }
}
