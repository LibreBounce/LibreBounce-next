package net.librebounce.utils.attack

import net.librebounce.event.AttackEvent
import net.librebounce.event.Listenable
import net.librebounce.event.handler
import net.librebounce.features.module.impl.combat.HitDetector.hitDelay
import net.librebounce.utils.client.chat
import net.librebounce.utils.timing.MSTimer
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import kotlin.math.abs

object CombatUtils : Listenable {
    private val mc = Minecraft.getInstance()
    var lastValidAttack = MSTimer()
    var lastAttackCrit = false
    var lastTarget: Entity? = null

    val onAttack = handler<AttackEvent> { event ->
        if (lastTarget != event.targetEntity) {
            lastTarget = event.targetEntity!!
            lastValidAttack.reset()
        }

        if (lastValidAttack.hasTimePassed(hitDelay))
            lastValidAttack.reset()

        lastAttackCrit = mc.player.fallDistance > 0

        chat("Hit delay: $hitDelay, last valid attack: ${abs(lastValidAttack.getTime())}, is the last attack a critical hit: $lastAttackCrit")
    }
}
