package net.librebounce.features.module.impl.combat.components

import net.librebounce.event.Listenable
import net.librebounce.event.handler
import net.librebounce.event.Render3DEvent
import net.librebounce.config.Configurable
import net.librebounce.features.module.base.Module
import net.librebounce.utils.attack.CombatUtils
import net.librebounce.utils.client.MinecraftInstance
import net.librebounce.utils.render.ColorUtils.withAlpha
/*import net.librebounce.utils.render.RenderUtils.drawBox
import net.librebounce.utils.render.RenderUtils.drawCircle
import net.librebounce.utils.render.RenderUtils.drawEntityBox
import net.librebounce.utils.render.RenderUtils.drawPlatform*/
import net.librebounce.utils.rotation.RotationUtils.currentRotation
import net.librebounce.utils.rotation.RotationUtils.serverRotation
import net.minecraft.entity.living.LivingEntity
import net.minecraft.util.math.Box
import java.awt.Color

open class MarkComponent(owner: Module, target: LivingEntity?, shouldApply: Boolean = true): Configurable(owner.name),
    MinecraftInstance, Listenable {
    private val renderAimPointBox by boolean("RenderAimPointBox", false) { shouldApply }.subjective()
    private val aimPointBoxColor by color("AimPointBoxColor", Color.CYAN) { shouldApply && renderAimPointBox }.subjective()
    private val aimPointBoxSize by float("AimPointBoxSize", 0.1f, 0f..0.2F) { shouldApply && renderAimPointBox }.subjective()

    private val mark by choices("Mark", arrayOf("None", "Platform", "Box", "Circle"), "Circle") { shouldApply }.subjective()

    private val markColor by color("MarkColor", Color(255, 0, 0, 70)) { shouldApply && mark in arrayOf("Platform", "Box") }.subjective()
    private val markHittableColor by color("MarkHittableColor", Color(37, 126, 255, 70)) { shouldApply && mark in arrayOf("Platform", "Box") }.subjective()
    private val boxOutline by boolean("Outline", true) { shouldApply && mark == "Box" }.subjective()

    // Circle options
    private val circleStartColor by color("CircleStartColor", Color.BLUE) { shouldApply && mark == "Circle" }.subjective()
    private val circleEndColor by color("CircleEndColor", Color.CYAN.withAlpha(0)) { shouldApply && mark == "Circle" }.subjective()
    private val fillInnerCircle by boolean("FillInnerCircle", false) { shouldApply && mark == "Circle" }.subjective()
    private val withHeight by boolean("WithHeight", true) { shouldApply && mark == "Circle" }.subjective()
    private val animateHeight by boolean("AnimateHeight", false) { shouldApply && withHeight && mark == "Circle" }.subjective()
    private val heightRange by floatRange("HeightRange", 0.0f..0.4f, -2f..2f) { shouldApply && withHeight && mark == "Circle" }.subjective()
    private val extraWidth by float("ExtraWidth", 0F, 0F..2F) { shouldApply && mark == "Circle" }.subjective()
    private val animateCircleY by boolean("AnimateCircleY", true) { shouldApply && (fillInnerCircle || withHeight) }.subjective()
    private val circleYRange by floatRange("CircleYRange", 0F..0.5F, 0F..2F) { shouldApply && animateCircleY }.subjective()
    private val duration by float(
        "Duration", 1.5F, 0.5F..3F, suffix = "Seconds"
    ) { shouldApply && (animateCircleY || animateHeight) }.subjective()

    init {
        owner.addValues(this.values)
    }

    val onRender3D = handler<Render3DEvent> { event ->
        if (!shouldApply) return@handler

        mc.player ?: return@handler
        target ?: return@handler

        if (renderAimPointBox) drawAimPointBox()

        val color = if ((target as LivingEntity) == CombatUtils.lastTarget) if (CombatUtils.canHit()) markHittableColor else markColor
            else if (target.damagedTimer == 0) markHittableColor else markColor

        when (mark) {
            /*"Platform" -> drawPlatform(target!!, color)
            "Box" -> drawEntityBox(target!!, color, boxOutline)
            "Circle" -> drawCircle(
                target!!,
                duration * 1000F,
                heightRange.takeIf { animateHeight } ?: heightRange.endInclusive..heightRange.endInclusive,
                extraWidth,
                fillInnerCircle,
                withHeight,
                circleYRange.takeIf { animateCircleY },
                circleStartColor.rgb,
                circleEndColor.rgb
            )*/
            "None" -> return@handler
        }
    }

    private fun drawAimPointBox() {
        val player = mc.player

        val scale = aimPointBoxSize.toDouble()

        val box = Box(0.0, 0.0, 0.0, scale, scale, scale)

        val entityRenderDispatcher = mc.entityRenderDispatcher

        /*runWithSimulatedPosition(player, player.interpolatedPosition(player.last)) {
            runWithSimulatedPosition(target, target.interpolatedPosition(target.last)) {
                val rotationVec = player.eyes + getRotationVector(
                    serverRotation.lerpWith(currentRotation ?: player.rotation, mc.timer.partialTick)
                ) * player.getDistanceToEntityBox(target).coerceAtMost(attackRange.toDouble())

                val offSetBox = box.offset(rotationVec - entityRenderDispatcher.offset)

                drawBox(offSetBox, aimPointBoxColor)
            }
        }*/
    }
}
