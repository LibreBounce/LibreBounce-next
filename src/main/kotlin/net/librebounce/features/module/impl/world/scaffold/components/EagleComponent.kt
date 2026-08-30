package net.librebounce.features.module.impl.world.scaffold.components

import net.librebounce.config.Configurable
import net.librebounce.event.Listenable
import net.librebounce.event.SneakSlowDownEvent
import net.librebounce.event.StrafeEvent
import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Module
import net.librebounce.utils.client.MinecraftInstance
import net.librebounce.utils.client.chat
import net.librebounce.utils.extensions.isReplaceable
import net.librebounce.utils.timing.WaitTickUtils
import net.minecraft.network.packet.c2s.play.PlayerMovementActionC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import org.lwjgl.input.Keyboard
import kotlin.math.abs

open class EagleComponent(owner: Module, shouldApply: Boolean = true, debug: Boolean = false): Configurable(owner.name),
    MinecraftInstance, Listenable {
    private val eagle by
        choices("Eagle", arrayOf("Normal", "Silent", "Off"), "Normal") { shouldApply }
    private val eagleMode by choices("EagleMode", arrayOf("Both", "OnGround", "InAir"), "Both")
    { shouldApply && eagle != "Off" }
    private val adjustedSneakSpeed by boolean("AdjustedSneakSpeed", true)
    { shouldApply && eagle == "Silent"}
    private val eagleSpeed by float("EagleSpeed", 0.3f, 0.3f..1.0f)
    { shouldApply && eagle != "Off" }
    val eagleSprint by boolean("EagleSprint", false) { shouldApply && eagle == "Normal" }
    private val blocksToEagle by intRange("BlocksToEagle", 0..0, 0..10)
    { shouldApply && eagle != "Off" }
    private val edgeDistance by float("EagleEdgeDistance", 0f, 0f..0.5f)
    { shouldApply && eagle != "Off" }
    private val useMaxSneakTime by boolean("UseMaxSneakTime", true) { shouldApply && eagle != "Off" }
    private val maxSneakTicks by intRange("MaxSneakTicks", 3..3, 0..10)
    { shouldApply && eagle != "Off" && useMaxSneakTime }
    private val blockSneakingAgainUntilOnGround by boolean("BlockSneakingAgainUntilOnGround", true)
    { useMaxSneakTime && eagleMode != "OnGround" }

    init {
        owner.addValues(this.values)
    }

    private var debug = debug

    private var placedBlocksWithoutEagle = 0
    var eagleSneaking = false
    private var requestedStopSneak = false

    val onUpdate = handler<UpdateEvent> { event ->
        val player = mc.player
        if (eagle == "Off") return@handler
        if (debug) chat("Now in handler!")

        var dif = 0.5
        val pos = BlockPos(player).down()

        for (side in Direction.entries) {
            if (side.axis == Direction.Axis.Y)
                continue

            if (debug) chat("Now in side for loop!")
            val neighbor = pos.offset(side)

            if (neighbor.isReplaceable) {
                if (debug) chat("Neighbour is replaceable")

                val calcDif = (if (side.axis == Direction.Axis.Z) {
                    abs(neighbor.z + 0.5 - player.z)
                } else {
                    abs(neighbor.x + 0.5 - player.x)
                }) - 0.5

                dif.coerceAtMost(calcDif)
            }
        }

        val blockSneaking = WaitTickUtils.hasScheduled("block")
        val alreadySneaking = WaitTickUtils.hasScheduled("sneak")

        run {
            if (debug) chat("Now in run!")
            if (placedBlocksWithoutEagle < blocksToEagle.random() && !alreadySneaking && !blockSneaking && !eagleSneaking && !requestedStopSneak) {
                return@run
            }

            val eagleCondition = when (eagleMode) {
                "OnGround" -> player.onGround
                "InAir" -> !player.onGround
                else -> true
            }

            // For better sneak support we could move this to InputEvent
            val pressedOnKeyboard = Keyboard.isKeyDown(mc.options.sneakKey.keyCode)

            var shouldEagle =
                eagleCondition && (pos.isReplaceable || dif < edgeDistance) || pressedOnKeyboard

            val shouldSchedule = !requestedStopSneak

            if (requestedStopSneak) {
                requestedStopSneak = false

                if (!player.onGround) shouldEagle = pressedOnKeyboard
            } else if (blockSneaking || alreadySneaking) {
                return@run
            }

            if (eagle == "Silent") {
                if (eagleSneaking != shouldEagle) {
                    /*sendPacket(
                        PlayerMovementActionC2SPacket(
                            player, if (shouldEagle) {
                                PlayerMovementActionC2SPacket.Action.START_SNEAKING
                            } else {
                                PlayerMovementActionC2SPacket.Action.STOP_SNEAKING
                            }
                        )
                    )*/

                    // Adjust speed when silent sneaking
                    if (adjustedSneakSpeed && shouldEagle) {
                        player.velocityX *= eagleSpeed
                        player.velocityZ *= eagleSpeed
                    }
                }
            } else {
                player.isSneaking = shouldEagle
            }

            eagleSneaking = shouldEagle
            if (debug) chat("Tried to sneak")

            if (eagleSneaking && shouldSchedule) {
                if (useMaxSneakTime) {
                    WaitTickUtils.conditionalSchedule("sneak") { elapsed ->
                        (elapsed >= maxSneakTicks.random() + 1).also { requestedStopSneak = it }
                    }
                }

                if (blockSneakingAgainUntilOnGround && !player.onGround) {
                    WaitTickUtils.conditionalSchedule("block") {
                        mc.player?.onGround.also { if (it != false) requestedStopSneak = true } ?: true
                    }
                }
            }

            placedBlocksWithoutEagle = 0
        }
    }

    /*val onStrafe = handler<StrafeEvent> { event ->
        if (eagle != "Silent" || !adjustedSneakSpeed || !eagleSneaking) return@handler

        /*event.forward *= eagleSpeed
        event.strafe *= eagleSpeed*/
    }

    val onSneakSlowDown = handler<SneakSlowDownEvent> { event ->
        if (eagle != "Normal") return@handler

        event.forward *= eagleSpeed / 0.3f
        event.strafe *= eagleSpeed / 0.3f
    }*/
}
