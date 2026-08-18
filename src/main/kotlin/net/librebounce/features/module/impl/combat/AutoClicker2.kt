package net.librebounce.features.module.impl.combat

import net.librebounce.event.Render3DEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.base.settings.ClickingSettings
import net.librebounce.utils.attack.CombatUtils.lastTarget
import net.minecraft.client.options.KeyBinding
import net.minecraft.item.BlockItem
import net.minecraft.world.HitResult

object AutoClicker2 : Module("AutoClicker2", Category.COMBAT) {
    private val left by boolean("Left", true)
    private val leftSettings = ClickingSettings(this, "Left", { left })
    private val hurtTime by int("HurtTime", 10, 0..10) { left }
    private val breakBlocks by boolean("BreakBlocks", true) { left }
    private val right by boolean("Right", false)
    private val rightSettings = ClickingSettings(this, "Right", { right })
    private val onlyBlocks by boolean("OnlyBlocks", true) { right }
    private val debug by boolean("Debug", false)

    val onRender3D = handler<Render3DEvent> {
        mc.player?.let { player ->
            if (left && mc.options.attackKey.isPressed && !mc.options.useKey.isPressed &&
                (lastTarget == null || if (SmartHit.handleEvents()) SmartHit.shouldHit(lastTarget!!) else lastTarget!!.damagedTimer <= hurtTime) &&
                (mc.player.abilities.creativeMode || (!breakBlocks || mc.crosshairTarget.type != HitResult.Type.BLOCK)) &&
                leftSettings.canClick()
                ) {
                if (debug) chat("Clicked left")

                repeat(leftSettings.clicks) {
                    KeyBinding.click(mc.options.attackKey.keyCode)
                }
            }

            if (right && mc.options.useKey.isPressed &&
                (!onlyBlocks || player.displayItemInHand?.item is BlockItem) &&
                rightSettings.canClick()
                ) {
                if (debug) chat("Clicked right")

                repeat(rightSettings.clicks) {
                    KeyBinding.click(mc.options.useKey.keyCode)
                }
            }
        }
    }
}
