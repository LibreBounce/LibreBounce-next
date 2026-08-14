package net.librebounce.features.ui.elements.clickgui.dropdown.elements

interface Element {
    open fun render(mouseX: Int, mouseY: Int, partialT: Float) {}
    open fun onClick(mouseX: Int, mouseY: Int, partialT: Float) {}
}
