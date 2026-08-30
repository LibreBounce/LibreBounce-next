package net.librebounce.utils.attack

import net.librebounce.event.AttackEvent
import net.librebounce.event.UpdateEvent
import net.librebounce.event.Listenable
import net.librebounce.event.handler
import net.librebounce.features.module.impl.combat.HitDetector.debug
import net.librebounce.features.module.impl.combat.HitDetector.hitDelay
import net.librebounce.features.module.impl.combat.HitDetector.resetTargetAfter
import net.librebounce.utils.client.chat
import net.librebounce.utils.client.MinecraftInstance
import net.librebounce.utils.timing.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.living.LivingEntity
import net.minecraft.entity.living.player.PlayerEntity
import kotlin.math.abs

object CombatUtils : MinecraftInstance, Listenable {
    var lastValidAttack = MSTimer()
    var lastValidAttackIsCrit = false
    var lastAttackCrit = false
    var lastAttackBlocked = false
    var lastTarget: LivingEntity? = null

    val onAttack = handler<AttackEvent> { event ->
        if (lastTarget != event.targetEntity) {
            lastTarget = event.targetEntity!! as LivingEntity?
            lastValidAttack.reset()
            
            if (debug) chat("Reset target stats due to target changing!")
        }

        if (lastValidAttack.hasTimePassed(hitDelay)) {
            lastValidAttack.reset()
            lastValidAttackIsCrit = canCritHit(mc.player)
        }

        lastAttackCrit = canCritHit(mc.player)
        lastAttackBlocked = (event.targetEntity!! as PlayerEntity).isSwordBlocking

        if (debug) chat("Hit delay: $hitDelay, last valid attack: ${abs(lastValidAttack.getTime())}, is the last attack a critical hit: $lastAttackCrit")
    }

    val onUpdate = handler<UpdateEvent> { event ->
        if (lastValidAttack.hasTimePassed(resetTargetAfter * 1000)) {
            lastTarget = null
            lastAttackCrit = false
            lastAttackBlocked = false

            val seconds = if (resetTargetAfter == 1) "second" else "seconds"
            if (debug) chat("Reset due to $resetTargetAfter $seconds passing")
        }
    }

    val timeUntilHit = (hitDelay - lastValidAttack.getTime()).coerceAtLeast(0)

    fun canHit(): Boolean = lastValidAttack.hasTimePassed(hitDelay)
    fun canHit(customHurtTime: Int) = customHurtTime <= hitDelay / 50

    fun canCritHit(player: PlayerEntity): Boolean =
        player.fallDistance > 0 &&
        !player.isClimbing &&
        !player.inWater &&
        //!player.hasStatusEffect(blindness) &&
        player.vehicle == null
}
