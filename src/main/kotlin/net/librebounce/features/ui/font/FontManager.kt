/*package net.librebounce.features.ui.font

import io.github.axolotlclient.rendering.font.Font
import io.github.axolotlclient.rendering.font.FontAdapter
import java.nio.file.Path

object FontManager {
    private val roboto: Font = Path.getDefault().getPath()("/assets/librebounce/fonts/roboto_variable.ttf")
    val roboto30 = Font.read(roboto, 30)

    private val INTER_ADAPTER: FontAdapter

    init {
        try {
            DefaultFont::class.java.getResourceAsStream("/assets/librebounce/fonts/roboto_variable.ttf")
                .use { regular ->
                    DefaultFont::class.java.getResourceAsStream("/assets/axolotlclient-rendering/fonts/roboto_variable_italic.ttf")
                        .use { italic ->
                            check(!(regular == null || italic == null))
                            INTER = Font.read(regular, 9)
                                .addSubFont(Font.TAG_ITALIC, Font.ITALIC_ENABLED, Font.read(italic, 9))
                        }
                }
        } catch (e: Exception) {
            throw RuntimeException("The Inter font is packaged with this library!", e)
        }
        io.github.axolotlclient.rendering.font.DefaultFont.INTER_ADAPTER =
            FontAdapter(io.github.axolotlclient.rendering.font.DefaultFont.INTER)
    }
}*/
