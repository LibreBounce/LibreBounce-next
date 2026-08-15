package net.librebounce.features.module.impl.combat

import kotlinx.coroutines.Dispatchers
import net.librebounce.event.AttackEvent
import net.librebounce.event.DelayedPacketProcessEvent
import net.librebounce.event.EventState
import net.librebounce.event.GameLoopEvent
import net.librebounce.event.GameTickEvent
import net.librebounce.event.MoveEvent
import net.librebounce.event.PlayerTickEvent
import net.librebounce.event.async.waitTicks
import net.librebounce.event.handler
import net.librebounce.features.module.Category
import net.librebounce.features.module.Module
import net.librebounce.utils.client.chat
import net.librebounce.utils.simulation.SimulatedPlayer
import net.minecraft.entity.living.LivingEntity
import net.minecraft.util.math.Vec3d

object TickManipulation : Module("TickManipulation", Category.COMBAT) {
    private val maxTicksToSkip by int("MaxTicksToSkip", 2, 0..10)
    private val maxTicksToStop by int("MaxTicksToStop", 2, 0..10)
    private val allowedRange by floatRange("AllowedRange", 2.5f..3.08f, 0f..6f)
    private val preferredRange by float("PreferredRange", 3.08f, 0f..6f)
    private val debug by boolean("Debug", false).subjective()

    private var ticksToSkip = 0
    private var tickBalance = 0f
    private var reachedTheLimit = false
    private var attacked = false
    private val tickBuffer = mutableListOf<TickData>()
    var duringTickModification = false

    override fun onToggle(state: Boolean) {
        duringTickModification = false
    }

    val onPreTick = handler<PlayerTickEvent> { event ->
        val player = mc.player ?: return@handler

        if (player.vehicle != null) {
            return@handler
        }

        if (event.state == EventState.PRE && ticksToSkip-- > 0) {
            event.cancelEvent()
        }
    }

    private var modificationFlag = false

    val onGameLoop = handler<GameLoopEvent> {
        if (modificationFlag) {
            modificationFlag = false
            duringTickModification = false
        }
    }

    val onGameTick = handler<GameTickEvent>(dispatcher = Dispatchers.Main, priority = 1) {
        val player = mc.player ?: return@handler

        if (player.vehicle != null) {
            return@handler
        }

        if (!duringTickModification && tickBuffer.isNotEmpty()) {
            val nearbyEnemy = getNearestEntityInRange() ?: return@handler
            val currentDistance = player.commandSourcePos.distanceTo(nearbyEnemy.commandSourcePos)

            val possibleTicks = tickBuffer.mapIndexedNotNull { index, tick ->
                val tickDistance = tick.position.distanceTo(nearbyEnemy.commandSourcePos)

                (index to tick).takeIf {
                    tickDistance < currentDistance && tickDistance in allowedRange && !tick.collidingHorizontally
                }
            }

            val (bestTick, _) = possibleTicks.minByOrNull { (index, _) -> index } ?: return@handler

            if (bestTick == 0) return@handler

            duringTickModification = true

            val skipTicks = (bestTick + maxTicksToStop).coerceAtMost(maxTicksToSkip + maxTicksToStop)

            fun tick() {
                repeat(skipTicks) {
                    player.tick()
                }
            }

            tick()

            if (attacked) {
                waitTicks(maxTicksToStop.coerceAtMost(skipTicks))
            }

            modificationFlag = true

            if (debug) chat("(TickBase) Lag ticks: ${skipTicks}, best ticks: ${bestTick}, additional pause ticks: ${maxTicksToSkip})")

        }
    }

    val onMove = handler<MoveEvent> {
        val player = mc.player ?: return@handler

        if (player.vehicle != null) {
            return@handler
        }

        tickBuffer.clear()

        val simPlayer = SimulatedPlayer.fromClientPlayer(/*RotationUtils.modifiedInput*/mc.player.input)

        simPlayer.yaw = player.yaw//RotationUtils.currentRotation?.yaw ?: player.yaw

        repeat(maxTicksToSkip) {
            simPlayer.tick()

            tickBuffer += TickData(
                simPlayer.pos,
                simPlayer.fallDistance,
                simPlayer.velocityX,
                simPlayer.velocityY,
                simPlayer.velocityZ,
                simPlayer.onGround,
                simPlayer.collidingHorizontally
            )
        }
    }

    val onDelayedPacketProcess = handler<DelayedPacketProcessEvent> {
        if (duringTickModification) {
            it.cancelEvent()
        }
    }

    val onAttack = handler<AttackEvent> {
        attacked = true
    }
    private data class TickData(
        val position: Vec3d,
        val fallDistance: Float,
        val velocityX: Double,
        val velocityY: Double,
        val velocityZ: Double,
        val onGround: Boolean,
        val collidingHorizontally: Boolean,
    )

    private fun getNearestEntityInRange(): LivingEntity? {
        mc.player ?: return null
        val entities = mc.world.entities ?: return null

        return entities.asSequence().filterIsInstance<LivingEntity>()
            /*.filter { EntityUtils.isSelected(it, true) }*/.minByOrNull { mc.player.distanceTo(it) }
    }
}
