package net.librebounce.utils.movement

import net.librebounce.event.*
import net.librebounce.utils.client.MinecraftInstance
import net.librebounce.utils.extensions.*
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.sign

object MovementUtils : MinecraftInstance, Listenable {
    var affectSprintOnAttack: Boolean? = null

    var speed
        get() = mc.player?.run { sqrt(velocityX * velocityX + velocityZ * velocityZ).toFloat() } ?: .0f
        set(value) {
            strafe(value)
        }

    val hasMotion
        get() = mc.player?.run { velocityX != .0 || velocityY != .0 || velocityZ != .0 } == true

    var airTicks = 0
    var groundTicks = 0

    @JvmOverloads
    fun strafe(
        speed: Float = MovementUtils.speed, stopWhenNoInput: Boolean = false, moveEvent: MoveEvent? = null,
        strength: Double = 1.0,
    ) =
        mc.player?.run {
            if (!isMoving) {
                if (stopWhenNoInput) {
                    moveEvent?.zeroXZ()
                    stopXZ()
                }

                return@run
            }

            val prevX = velocityX * (1.0 - strength)
            val prevZ = velocityZ * (1.0 - strength)
            val useSpeed = speed * strength

            val yaw = direction
            val x = (-sin(yaw) * useSpeed) + prevX
            val z = (cos(yaw) * useSpeed) + prevZ

            if (moveEvent != null) {
                moveEvent.x = x
                moveEvent.z = z
            }

            velocityX = x
            velocityZ = z
        }

    fun Vec3d.strafe(
        yaw: Float = direction.toDegreesF(), speed: Double = sqrt(x * x + z * z),
        strength: Double = 1.0,
        moveCheck: Boolean = false,
    ): Vec3d {
        if (moveCheck) {
            x = 0.0
            z = 0.0
            return this
        }

        val prevX = x * (1.0 - strength)
        val prevZ = x * (1.0 - strength)
        val useSpeed = speed * strength

        val angle = yaw.toRadiansD()
        x = (-sin(angle) * useSpeed) + prevX
        z = (cos(angle) * useSpeed) + prevZ
        return this
    }

    fun forward(distance: Double, vertical: Double = 0) =
        mc.player?.run {
            val yaw = yaw.toRadiansD()

            setPosition(x - sin(yaw) * distance, y + vertical, z + cos(yaw) * distance)
        }

    val direction
        get() = mc.player?.run {
            var yaw = yaw
            var forward = 1f

            if (input.forwardSpeed < 0f) {
                yaw += 180f
                forward = -0.5f
            } else if (input.forwardSpeed > 0f) {
                forward = 0.5f
            }

            yaw -= (sign(input.movementSideways) * 90f) * forward

            yaw.toRadiansD()
        } ?: 0.0

    fun isOnGround(height: Double) =
        mc.world != null && mc.player != null &&
            mc.world.getCollisions(mc.player,
                mc.player.shape.offset(Vec3d_ZERO.withY(-height))
            ).isNotEmpty()

    var serverOnGround = false

    var serverX = .0
    var serverY = .0
    var serverZ = .0

    val onPacket = handler<PacketEvent> { event ->
        if (event.isCancelled)
            return@handler

        (event.packet as? PlayerMoveC2SPacket)?.let {
            serverOnGround = it.onGround

            if (it.isMoving) {
                serverX = it.x
                serverY = it.minY
                serverZ = it.z
            }
        }
    }
}