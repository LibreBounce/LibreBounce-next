package net.librebounce.utils.client

import net.minecraft.client.gui.widget.TextFieldWidget
import org.lwjgl.input.Keyboard

object TabUtils {
    fun tab(vararg textFields: TextFieldWidget) {
        textFields.forEachIndexed { i, textField ->
            if (textField.isFocused) {
                textField.isFocused = false

                // Cycle to previous textField when holding shift.
                textFields[
                    (i + (if (Keyboard.pressed(Keyboard.KEY_LSHIFT)) -1 else 1) + textFields.size)
                            % textFields.size
                ].isFocused = true

                return
            }
        }

        // Focus first when no text field is focused.
        textFields[0].isFocused = true
    }
}
