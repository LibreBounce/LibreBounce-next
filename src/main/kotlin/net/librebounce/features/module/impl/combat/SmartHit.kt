package net.librebounce.features.module.impl.combat

import net.librebounce.event.AttackEvent
import net.librebounce.event.GameTickEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.impl.combat.HitDetector.hitDelay
import net.librebounce.utils.attack.CombatUtils.canCritHit
import net.librebounce.utils.attack.CombatUtils.canHit
import net.librebounce.utils.attack.CombatUtils.lastAttackBlocked
import net.librebounce.utils.attack.CombatUtils.lastAttackCrit
import net.librebounce.utils.extensions.*
import net.librebounce.utils.simulation.SimulatedPlayer
import net.librebounce.utils.timing.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.living.player.PlayerEntity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object SmartHit: Module("SmartHit", Category.COMBAT) {
    private val usePredictedTargetHurtTime by boolean("UsePredictedTargetHurtTime", true)

    private val distanceHandling by choices("DistanceHandling", arrayOf("Allow", "Forbid", "Ignore"), "Ignore")
    private val distance by floatRange("Distance", 2.7f..8f, 0f..8f, suffix = "blocks") { distanceHandling != "Ignore" }

    private val predictedDistanceHandling by choices("PredictedDistanceHandling", arrayOf("Allow", "Forbid", "Ignore"), "Allow")
    private val predictedDistance by floatRange("PredictedDistance", 3.1f..8f, 0f..8f, suffix = "blocks") { predictedDistanceHandling != "Ignore" }

    private val minTargetRotationDifference by float("MinTargetRotationDifference", 8f, 0f..180f, suffix = "º")

    private val checkForCriticalHits by boolean("CheckForCriticalHits", true)
    private val improveCritHandling by boolean("ImproveCritHandling", false) { checkForCriticalHits }
    private val minTicksUntilFallingToCancel by int("MinTicksUntilFallingToCancel", 3, 0..10) { checkForCriticalHits && improveCritHandling }

    private val checkForBlockedHits by boolean("CheckForBlockedHits", true)

    private val experimentalChecks by boolean("ExperimentalChecks", true)
    private val failsafe by boolean("Failsafe", false)

    private val notBelowOwnHealth by float("NotBelowOwnHealth", 5f, 0f..20f)
    private val notBelowTargetHealth by float("NotBelowTargetHealth", 5f, 0f..20f)

    private val notOnEdge by boolean("NotOnEdge", false)
    private val notOnEdgeLimit by float("NotOnEdgeLimit", 1f, 0f..8f, suffix = "blocks") { notOnEdge }

    private val targetHurtTimeHandling by choices("TargetHurtTimeHandling", arrayOf("Allow", "Forbid", "Ignore"), "Ignore")
    private val targetHurtTime by intRange("TargetHurtTime", 0..1, 0..10) { targetHurtTimeHandling != "Ignore" }

    private val ownHurtTimeHandling by choices("OwnHurtTimeHandling", arrayOf("Allow", "Forbid", "Ignore"), "Allow")
    private val ownHurtTime by intRange("OwnHurtTime", 7..10, 0..10) { ownHurtTimeHandling != "Ignore" }

    private val predictClientMovement by int("PredictClientMovement", 5, 0..5, suffix = "ticks")
    private val predictEnemyPosition by float("PredictEnemyPosition", 1.5f, 0f..2f)

    private val simulateKnockback by boolean("SimulateKnockback", true)
    private val simulatedHorizontalKnockback by floatRange("SimulatedHorizontalKnockback", 0.88f..1f, 0f..4f) { simulateKnockback }
    private val simulatedVerticalKnockback by floatRange("SimulatedVerticalKnockback", 0.4f..0.5f, 0f..2f) { simulateKnockback }

    private val debug by boolean("Debug", false).subjective()

    private var simHurtTime = MSTimer()
    //private var simTargetHurtTime = MSTimer()

    /*val onAttack = handler<AttackEvent> { event ->
        val player = mc.player ?: return@handler
        val target = event.targetEntity ?: return@handler

        val targetPlayer = target as PlayerEntity

        val hittable = canHit(simTargetHurtTime)
        val latency = latencyInTicks(player as PlayerEntity)

        simTargetHurtTime = targetPlayer.damagedTimer - latency

        simTargetHurtTime = if (usePredictedTargetHurtTime)
            if (hittable)
                10 + latency else simTargetHurtTime
        else targetPlayer.damagedTimer
    }*/

    fun shouldHit(target: Entity): Boolean {
        val player = mc.player ?: return false

        if (target.removed) return false

        val playerPing = (player as PlayerEntity).getPing()
        val playerLatencyInTicks = latencyInTicks(player as PlayerEntity)
        val targetPing = (target as PlayerEntity).getPing()

        val combinedPing = playerPing + targetPing
        val combinedPingMult = combinedPing.toFloat() / 100f

        val dist = player.getDistanceToEntityBox(target)
        val targetDistance = target.getDistanceToEntityBox(player)

        val simPlayer = SimulatedPlayer.fromClientPlayer(mc.player.input/*RotationUtils.modifiedInput*/)
        var ticksUntilFalling = 0
        //simHurtTime = player.damagedTimer

        repeat(predictClientMovement + 1) {
            simPlayer.tick()

            if (simPlayer.velocityY >= 0) ++ticksUntilFalling
        }

        val hittable = canHit()

        /*if (failsafe && ticksSinceHit > playerLatencyInTicks + 1 &&
            target.damagedTimer !in (target.damagedTimer + 1 - playerLatencyInTicks)..(target.damagedTimer - 1 - playerLatencyInTicks)) {
            ticksSinceHit = attackDelay + 1
        }*/

        val rotDiff = rotationDifference(
            toRotation(player.hitBox.center, true, target!!),
            target.rotation
        )

        //val targetCanHit = rotDiff < 22f + (18f * combinedPingMult) && !target.hitBox.contains(player.eyes) && canHit(player.damagedTimer - playerLatencyInTicks)
        //val targetHitLikely = targetCanHit && !target.isUsingItem && targetDistance < 3.08f

        val simDistance = simulateDistance(simPlayer, target, simulateKnockback /*&& targetHitLikely*/)

        val playerHurtTimeAllowed = when (ownHurtTimeHandling) {
            "Allow" -> player.damagedTimer in ownHurtTime
            "Forbid" -> player.damagedTimer !in ownHurtTime
            else -> false
        }

        val targetHurtTimeAllowed = when (targetHurtTimeHandling) {
            "Allow" -> target.damagedTimer in targetHurtTime
            "Forbid" -> target.damagedTimer !in targetHurtTime
            else -> false
        }

        val distanceAllowed = when (distanceHandling) {
            "Allow" -> dist in distance
            "Forbid" -> dist !in distance
            else -> false
        }

        val predictedDistanceAllowed = when (predictedDistanceHandling) {
            "Allow" -> simDistance in predictedDistance
            "Forbid" -> simDistance !in predictedDistance
            else -> false
        }

        val groundHit =
            player.onGround && /*player.groundTicks > 1 &&*/ simPlayer.onGround && hittable

        val airHit =
            (hittable && (!checkForCriticalHits || !improveCritHandling || ticksUntilFalling < minTicksUntilFallingToCancel)) ||
            (checkForCriticalHits && canCritHit(player) && (!lastAttackCrit || hittable))

        //val baseHurtTime = 3f / (1f + sqrt(dist) - (rotDiff / 180f))
        val damagedTimerNoEscape = (2 * dist * 8).toInt() / 10

        val shouldHit = when {
            groundHit || airHit -> true
            checkForBlockedHits && lastAttackBlocked && !target.isSwordBlocking -> true
            minTargetRotationDifference != 0f && rotDiff < minTargetRotationDifference -> true
            //experimentalChecks && player.damagedTimer !in damagedTimerNoEscape..8 && targetHitLikely -> true
            experimentalChecks && targetDistance > 3.05f && hittable -> true
            player.health < notBelowOwnHealth || target.health < notBelowTargetHealth -> true
            //notOnEdge && player.isNearEdge(notOnEdgeLimit) -> true

            else -> playerHurtTimeAllowed || targetHurtTimeAllowed || distanceAllowed || predictedDistanceAllowed
        }

        if (debug) chat("(SmartHit) Will hit: ${shouldHit}, hit on the way: ${!hittable}, last hit blocked: ${lastAttackBlocked}, current distance: ${dist}, current distance (target POV): ${targetDistance}, predicted distance: ${simDistance}, combined ping: ${combinedPing}, combined ping multiplier: ${combinedPingMult}, own hurttime: ${player.damagedTimer}, simulated own hurttime: ${simHurtTime}, target hurttime: ${target.damagedTimer}, on ground: ${player.onGround}, predicted ground: ${simPlayer.onGround}, can critical hit: ${canCritHit(player)}")
        //if (debug) chat("(SmartHit) Will hit: ${shouldHit}, hit on the way: ${hitOnTheWay}, last hit blocked: ${lastHitBlocked}, current distance: ${dist}, current distance (target POV): ${targetDistance}, predicted distance: ${simDistance}, combined ping: ${combinedPing}, combined ping multiplier: ${combinedPingMult}, rotation difference: ${rotDiff}, target hit likely: ${targetHitLikely}, own hurttime: ${player.damagedTimer}, simulated own hurttime: ${simHurtTime}, target hurttime: ${target.damagedTimer}, simulated target hurt time: ${simTargetHurtTime}, on ground: ${player.onGround}, predicted ground: ${simPlayer.onGround}, can critical hit: ${canCritHit(player)}")

        return shouldHit
    }

    private fun latencyInTicks(player: PlayerEntity): Int =
        player.getPing().ceilDiv(2).ceilDiv(20)

    private fun simulateDistance(simPlayer: SimulatedPlayer, target: Entity, simulateKnockback: Boolean): Double {
        val player = mc.player ?: return 0.0

        val targetBox = target.hitBox.moved(
            target.currPos.subtract(target.last).times(predictEnemyPosition.toDouble())
        )

        if (simulateKnockback && simHurtTime.hasTimePassed(hitDelay))
            simulateOwnKnockback(simPlayer, target)

        val (currPos, last) = player.currPos to player.last
        player.setPosAndPrevPos(simPlayer.pos)
        val distance = player.getDistanceToBox(targetBox)
        player.setPosAndPrevPos(currPos, last)
        return distance
    }

    private fun simulateOwnKnockback(simPlayer: SimulatedPlayer, target: Entity) {
        val modifier = simulatedHorizontalKnockback.random()
        val fullModifier = (target.yaw * (PI.toFloat() / 180.0f)) * modifier * 0.5f

        val knockbackX = -sin(fullModifier)
        val knockbackY = simulatedVerticalKnockback.random()
        val knockbackZ = cos(fullModifier)

        simPlayer.apply {
            velocityX += knockbackX
            velocityY += knockbackY
            velocityZ += knockbackZ
        }

        if (debug) chat("(SmartHit) Simulated knockback. X: ${knockbackX}, Y + vertical modifier: ${knockbackY}, Z: ${knockbackZ}, horizontal modifier: ${modifier}")

        simHurtTime.reset()
    }
}
