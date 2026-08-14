package net.librebounce.utils.render

import io.github.axolotlclient.rendering.DrawUtil
import io.github.axolotlclient.rendering.font.Font
import java.awt.Color

object RenderUtils {
    val drawUtil = DrawUtil.get()

    fun drawString(font: Font, text: String, x: Float, y: Float, color: Color, shadow: Boolean = false) = drawString(font, text, x, y, color.rgb, shadow)
    fun drawString(font: Font, text: String, x: Float, y: Float, color: Int, shadow: Boolean = false) = drawUtil.`axolotlclient_rendering$drawString`(font, text, x, y, color, shadow)

    fun drawRoundedRect(x: Int, y: Int, x1: Int, y2: Int, color: Color, rounding: Float = 0f) = drawRoundedRect(x, y, x1, y2, color.rgb, rounding)
    fun drawRoundedRect(x: Int, y: Int, x1: Int, y2: Int, color: Int, rounding: Float = 0f) = drawUtil.`axolotlclient_rendering$roundedRect`(x, y, x1, y2, color, rounding)

    fun drawOutlineRoundedRect(x: Int, y: Int, x1: Int, y2: Int, color: Color, rounding: Float = 0f, outlineWidth: Float = 1f) = drawOutlineRoundedRect(x, y, x1, y2, color.rgb, rounding, outlineWidth)
    fun drawOutlineRoundedRect(x: Int, y: Int, x1: Int, y2: Int, color: Int, rounding: Float = 0f, outlineWidth: Float = 1f) = drawUtil.`axolotlclient_rendering$outlineRoundedRect`(x, y, x1, y2, color, rounding, outlineWidth)
}
