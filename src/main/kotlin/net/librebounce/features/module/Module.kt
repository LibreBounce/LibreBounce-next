/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.features.module

import net.librebounce.LibreBounce.isStarting
import net.librebounce.config.Configurable
import net.librebounce.event.Listenable
//import net.librebounce.features.module.modules.misc.GameDetector
import net.librebounce.file.FileManager.modulesConfig
import net.librebounce.file.FileManager.saveConfig
import net.librebounce.file.FileManager.valuesConfig
import net.librebounce.lang.translation
/*import net.librebounce.ui.client.hud.HUD
import net.librebounce.ui.client.hud.HUD.addNotification
import net.librebounce.ui.client.hud.element.elements.Arraylist
import net.librebounce.ui.client.hud.element.elements.Notification
import net.librebounce.ui.client.hud.element.elements.Notifications*/
import net.librebounce.utils.client.ClientUtils.LOGGER
//import net.librebounce.utils.client.MinecraftInstance
import net.librebounce.utils.client.asIdentifier
import net.librebounce.utils.client.chat
//import net.librebounce.utils.client.playSound
import net.librebounce.utils.extensions.addSpaces
import net.librebounce.utils.extensions.toLowerCamelCase
import net.librebounce.utils.kotlin.RandomUtils.nextFloat
import net.minecraft.client.Minecraft
//import net.librebounce.utils.timing.TickedActions.clearTicked
import org.lwjgl.input.Keyboard

open class Module(
    name: String,
    val category: Category,
    defaultKeyBind: Int = Keyboard.KEY_NONE,
    private val canBeEnabled: Boolean = true,
    private val forcedDescription: String? = null,

    subjective: Boolean = category == Category.RENDER,
    val gameDetecting: Boolean = canBeEnabled,
    defaultState: Boolean = false,
    defaultHidden: Boolean = false,
) : Configurable(name), Listenable {

    init {
        if (subjective) subjective()
    }

    // Value that determines whether the module should depend on GameDetector
    /*private val onlyInGameValue = boolean("OnlyInGame", true) {
        GameDetector.state
    }.subjective().excludeWhen(!gameDetecting)*/

    val mc = Minecraft.getInstance()

    var keyBind = defaultKeyBind
        set(keyBind) {
            field = keyBind

            saveConfig(modulesConfig)
        }

    var isHidden: Boolean by boolean("Hide", defaultHidden).subjective().onChanged {
        saveConfig(modulesConfig)
    }

    private val resetValue = boolean("Reset", false).subjective().onChange { _, _ ->
        try {
            values.forEach { if (it !== this) it.resetValue() else return@forEach }
        } catch (any: Exception) {
            LOGGER.error("Failed to reset all values", any)
            chat("Failed to reset all values: ${any.message}")
        } finally {
            //addNotification(Notification("Successfully reset ${this.name}'s settings"))
            saveConfig(valuesConfig)
        }
        return@onChange false
    }

    val description
        get() = forcedDescription ?: translation("module.${name.toLowerCamelCase()}.description")

    var slideStep = 0F

    // Current state of module
    var state = defaultState
        set(value) {
            if (field == value) return

            // Call toggle
            onToggle(value)

            // Clear ticked actions
            //clearTicked()

            // Play sound and add notification
            /*if (!isStarting) {
                mc.playSound("random.click".asIdentifier())

                addNotification(
                    Notification(translation("notification.module" + if (value) "Enabled" else "Disabled", name))
                )
            }*/

            // Call on enabled or disabled
            if (value) {
                onEnable()

                if (canBeEnabled) field = true
            } else {
                onDisable()

                field = false
            }

            // Save module state
            saveConfig(modulesConfig)
        }

    // HUD
    val hue = nextFloat()
    var slide = 0f
    var yAnim = 0f

    open val tag: String?
        get() = null

    fun toggle() {
        state = !state
    }

    /**
     * Called when module toggled
     */
    open fun onToggle(state: Boolean) {}

    /**
     * Called when module enabled
     */
    open fun onEnable() {}

    /**
     * Called when module disabled
     */
    open fun onDisable() {}

    /**
     * Called when module unregistered (for scripts)
     */
    open fun onUnregister() {}

    /**
     * Get value by [valueName]
     */
    fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Get value via `module[valueName]`
     */
    operator fun get(valueName: String) = getValue(valueName)

    val isActive
        get() = true//!gameDetecting || !onlyInGameValue.get() || GameDetector.isInGame()

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state && isActive
}
