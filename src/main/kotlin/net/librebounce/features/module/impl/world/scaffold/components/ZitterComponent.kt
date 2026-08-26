package net.librebounce.features.module.impl.world.scaffold.components

import net.librebounce.config.Configurable
import net.librebounce.event.Listenable
import net.librebounce.event.handler
import net.librebounce.features.module.base.Module
import net.librebounce.utils.client.MinecraftInstance
import net.librebounce.utils.client.chat
import net.librebounce.utils.movement.MovementUtils.strafe
import net.librebounce.utils.extensions.isReplaceable
import net.librebounce.utils.extensions.toRadians
import net.librebounce.utils.timing.TickDelayTimer
import net.minecraft.client.options.GameOptions
import kotlin.math.sin
import kotlin.math.cos

open class ZitterComponent(owner: Module, shouldApply: Boolean = true, debug: Boolean = false): Configurable(owner.name),
    MinecraftInstance, Listenable {
    private val zitter by choices("Zitter", arrayOf("Off", "Teleport", "Smooth"), "Off") { shouldApply }
    private val zitterSpeed by float("ZitterSpeed", 0.13f, 0.1f..0.3f) { shouldApply && zitter == "Teleport" }
    private val zitterStrength by float("ZitterStrength", 0.05f, 0f..0.2f) { shouldApply && zitter == "Teleport" }
    private val zitterTicks by intRange("ZitterTicks", 2..3, 0..20) { shouldApply && zitter == "Smooth" }

    private val useSneakMidAir by boolean("UseSneakMidAir", false) { shouldApply && zitter == "Smooth" }

    init {
        owner.addValues(this.values)
    }

    private var zitterDirection = false
    private val zitterTimer = TickDelayTimer(zitterTicks.first, zitterTicks.last)

    fun handle() {
        val player = mc.player
        val input = player.input

        when (zitter) {
            "Off" -> return

            "Smooth" -> {
                val notOnGround = !player.onGround || !player.collidingVertically

                /*if (player.onGround) {
                    input.sneaking = eagleSneaking || GameOptions.isPressed(mc.options.sneakKey)
                }*/

                if (input.jumping || mc.options.jumpKey.isPressed || notOnGround) {
                    zitterTimer.reset()

                    if (useSneakMidAir) {
                        input.sneaking = true
                    }

                    if (!notOnGround && !input.jumping) {
                        // Attempt to move against the direction
                        input.movementSideways = if (zitterDirection) 1f else -1f
                    } else {
                        input.movementSideways = 0f
                    }

                    zitterDirection = !zitterDirection

                    // Recreate input in case the user was indeed pressing inputs
                    if (mc.options.leftKey.isPressed) {
                        input.movementSideways++
                    }

                    if (mc.options.rightKey.isPressed) {
                        input.movementSideways--
                    }

                    return
                }

                if (zitterTimer.resetIfPassed())
                    zitterDirection = !zitterDirection

                input.movementSideways = if (zitterDirection) -1f else 1f
            }

            "Teleport" -> {
                strafe(zitterSpeed)
                val yaw = (player.yaw + if (zitterDirection) 90.0 else -90.0).toRadians()
                player.velocityX -= sin(yaw) * zitterStrength
                player.velocityZ += cos(yaw) * zitterStrength

                zitterDirection = !zitterDirection
            }
        }
    }
}