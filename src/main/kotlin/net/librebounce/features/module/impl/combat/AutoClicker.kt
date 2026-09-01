package net.librebounce.features.module.impl.combat

import net.librebounce.event.Render3DEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.base.settings.ClickingSettings
import net.librebounce.utils.attack.CombatUtils.lastTarget
import net.librebounce.utils.attack.CombatUtils.timeUntilHit
import net.librebounce.utils.attack.EntityUtils.isLookingOnEntities
import net.librebounce.utils.attack.EntityUtils.isSelected
import net.librebounce.utils.client.EntityLookup
import net.librebounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.living.LivingEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.SwordItem
import net.minecraft.world.HitResult

object AutoClicker : Module("AutoClicker", Category.COMBAT) {
    private val left by boolean("Left", true)
    private val leftSettings = ClickingSettings(this, "Left", left)
    private val requiresNoInput by boolean("RequiresNoInput", false) { left }
    private val maxAngleDifference by float("MaxAngleDifference", 30f, 10f..180f, suffix = "º") { left && requiresNoInput }
    private val range by float("Range", 3f, 0.1f..5f, suffix = "blocks") { left && requiresNoInput }
    private val hurtTime by int("HurtTime", 10, 0..10) { left }
    private val onDestroyBlock by boolean("OnDestroyBlock", false) { left }

    private val block by boolean("Block", false) { left }
    private val blockSettings = ClickingSettings(this, "Block", left && block)
    private val neverStopHits by boolean("NeverStopHits", true) { left && block }

    private val right by boolean("Right", false)
    private val rightSettings = ClickingSettings(this, "Right", right)
    private val onlyBlocks by boolean("OnlyBlocks", true) { right }
    //private val debug by boolean("Debug", false)

    val onRender3D = handler<Render3DEvent> {
        mc.player?.let { player ->
            val shouldLeftClick = if (requiresNoInput) lookingAtAnEntity() else mc.options.attackKey.isPressed
            val heldItem = player.displayItemInHand

            if (left && shouldLeftClick &&
                (lastTarget == null || if (SmartHit.handleEvents()) SmartHit.shouldHit(lastTarget!!) else lastTarget!!.damagedTimer <= hurtTime) &&
                (player.abilities.creativeMode || (onDestroyBlock || mc.crosshairTarget.type != HitResult.Type.BLOCK))) {
                leftSettings.requestClick(1)
            }

            if (left && block && shouldLeftClick && heldItem?.item is SwordItem && (!neverStopHits || timeUntilHit > 50)) {
                blockSettings.requestClick(3)
            }

            if (right && mc.options.useKey.isPressed && (!onlyBlocks || heldItem?.item is BlockItem)) {
                rightSettings.requestClick(3)
            }
        }
    }

    private val entities by EntityLookup<LivingEntity> {
        isSelected(it, true) && mc.player.getDistanceToEntityBox(it) <= range
    }

    private fun lookingAtAnEntity(): Boolean {
        val nearbyEntity = entities.minByOrNull { mc.player.getDistanceToEntityBox(it) } ?: return false

        return isLookingOnEntities(nearbyEntity, maxAngleDifference.toDouble())
    }
}