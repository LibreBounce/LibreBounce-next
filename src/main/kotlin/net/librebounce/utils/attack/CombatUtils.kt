package net.librebounce.utils.attack

import net.librebounce.event.AttackEvent
import net.librebounce.event.Listenable
import net.librebounce.event.handler
import net.librebounce.features.module.impl.combat.HitDetector.debug
import net.librebounce.features.module.impl.combat.HitDetector.hitDelay
import net.librebounce.utils.client.chat
import net.librebounce.utils.timing.MSTimer
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.living.player.PlayerEntity
import kotlin.math.abs

object CombatUtils : Listenable {
    private val mc = Minecraft.getInstance()
    var lastValidAttack = MSTimer()
    var lastValidAttackIsCrit = false
    var lastAttackCrit = false
    var lastAttackBlocked = false
    var lastTarget: Entity? = null

    val onAttack = handler<AttackEvent> { event ->
        if (lastTarget != event.targetEntity) {
            lastTarget = event.targetEntity!!
            lastValidAttack.reset()
        }

        if (lastValidAttack.hasTimePassed(hitDelay)) {
            lastValidAttack.reset()
            lastValidAttackIsCrit = mc.player.fallDistance > 0
        }

        lastAttackCrit = canCritHit(mc.player)
        lastAttackBlocked = (event.targetEntity!! as PlayerEntity).isSwordBlocking

        if (debug) chat("Hit delay: $hitDelay, last valid attack: ${abs(lastValidAttack.getTime())}, is the last attack a critical hit: $lastAttackCrit")
    }

    val timeUntilHit = (hitDelay - lastValidAttack.getTime()).coerceAtLeast(0)

    fun canHit(): Boolean = timeUntilHit.toInt() == 0
    fun canHit(customHurtTime: Int) = customHurtTime <= hitDelay / 50

    fun canCritHit(player: PlayerEntity): Boolean =
        player.fallDistance > 0 &&
        !player.isClimbing &&
        !player.inWater &&
        //!player.hasStatusEffect(blindness) &&
        player.vehicle == null
}
