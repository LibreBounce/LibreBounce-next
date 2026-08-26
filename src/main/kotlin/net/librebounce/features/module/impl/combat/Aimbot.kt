package net.librebounce.features.module.impl.combat

import net.librebounce.event.*
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.base.settings.RotationSettings
import net.librebounce.features.module.base.settings.RandomizationSettings
import net.librebounce.features.module.impl.combat.AutoClicker
//import net.librebounce.features.module.impl.combat.Backtrack.runWithSimulatedPosition
//import net.librebounce.features.module.modules.world.Fucker
//import net.librebounce.features.module.modules.world.Nuker
//import net.librebounce.features.module.modules.world.scaffolds.*
import net.librebounce.utils.attack.EntityUtils.isLookingOnEntities
import net.librebounce.utils.attack.EntityUtils.isSelected
import net.librebounce.utils.extensions.*
import net.librebounce.utils.inventory.ItemUtils.isConsumingItem
import net.librebounce.utils.inventory.SilentHotbar
import net.librebounce.utils.rotation.RotationUtils
import net.librebounce.utils.rotation.RotationUtils.currentRotation
import net.librebounce.utils.rotation.RotationUtils.isFaced
import net.librebounce.utils.rotation.RotationUtils.rotationDifference
import net.librebounce.utils.rotation.RotationUtils.searchCenter
import net.librebounce.utils.rotation.RotationUtils.setTargetRotation
import net.librebounce.utils.simulation.SimulatedPlayer
import net.librebounce.utils.timing.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.living.LivingEntity
//import net.minecraft.potion.Potion
import net.minecraft.util.*
import java.awt.Color

object Aimbot : Module("Aimbot", Category.COMBAT) {

    // Range
    private val attackRange by float("AttackRange", 3f, 0f..8f, suffix = "blocks")
    private val range by floatRange("Range", 0f..3f, 0f..8f, suffix = "blocks")
    private val throughWallsRange by floatRange("ThroughWallsRange", 0f..3f, 0f..8f, suffix = "blocks")

    private val activationSlot by boolean("ActivationSlot", false)
    private val preferredSlot by int("PreferredSlot", 1, 1..9) { activationSlot }

    private val clickOnly by boolean("ClickOnly", false)
    private val clickDelay by int("ClickDelay", 1, 1..1000) { clickOnly }
    private val notOnConsume by boolean("NotOnConsume", false)

    // Modes
    private val priority by choices(
        "Priority", arrayOf(
            "Optimal",
            "Health",
            "Distance",
            "Direction",
            "LivingTime",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "HealthAbsorption",
            "RegenAmplifier",
            "OnLadder",
            "InLiquid",
            "InWeb"
        ), "Distance"
    )
    private val targetMode by choices("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val limitedMultiTargets by int("LimitedMultiTargets", 0, 0..50) { targetMode == "Multi" }

    private val maxSwitchFOV by float("MaxSwitchFOV", 90f, 30f..180f, suffix = "º") { targetMode == "Switch" }
    private val switchDelay by int("SwitchDelay", 15, 1..1000, suffix = "ms") { targetMode == "Switch" }

    private val autoF5 by boolean("AutoF5", false).subjective()
    private val onDestroyBlock by boolean("OnDestroyBlock", false)

    // Rotations
    private val options = RotationSettings(this)//.withoutKeepRotation()

    private val generateSpotBasedOnDistance by boolean("GenerateSpotBasedOnDistance", false) { options.rotationsActive }

    private val randomization = RandomizationSettings(this) { options.rotationsActive }
    private val outBorder by boolean("Outborder", false) { options.rotationsActive }

    private val highestBodyPointToTargetValue = choices(
        "HighestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Head"
    ) {
        options.rotationsActive
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD)
        coercedPoint.displayName
    }
    private val highestBodyPointToTarget: String by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue = choices(
        "LowestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Feet"
    ) {
        options.rotationsActive
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint)
        coercedPoint.displayName
    }

    private val lowestBodyPointToTarget: String by lowestBodyPointToTargetValue

    private val horizontalBodySearchRange by floatRange(
        "HorizontalBodySearchRange", 0f..1f, 0f..1f
    ) { options.rotationsActive }

    private val lock by boolean("Lock", false)

    private val fov by float("FOV", 180f, 0f..180f, suffix = "º")

    // Prediction
    private val predictClientMovement by int("PredictClientMovement", 2, 0..5, suffix = "ticks")
    private val predictOnlyWhenOutOfRange by boolean(
        "PredictOnlyWhenOutOfRange", false
    ) { predictClientMovement != 0 }
    private val predictEnemyPosition by float("PredictEnemyPosition", 1.5f, -1f..2f)

    private val markComponent = MarkComponent(this)

    // Target
    var target: LivingEntity? = null
    private val prevTargetEntities = mutableListOf<Int>()

    private val switchTimer = MSTimer()
    private val clickTimer = MSTimer()

    override fun onToggle(state: Boolean) {
        target = null
        prevTargetEntities.clear()

        if (autoF5) mc.options.perspective = 0
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        update()
    }

    fun update() {
        if (cancelRun) return

        // Update target
        updateTarget()

        if (autoF5) {
            if (mc.options.perspective != 1 && target != null) {
                mc.options.perspective = 1
            }
        }
    }

    /**
     * Tick event
     */
    val onTick = handler<GameTickEvent>(priority = 2) {
        val player = mc.player ?: return@handler

        if (shouldPrioritize()) {
            target = null
            return@handler
        }

        if (cancelRun) {
            target = null
            return@handler
        }

        // Clicking delay
        if (mc.options.attackKey.isPressed) clickTimer.reset()
    }

    /**
     * Render event
     */
    val onRender3D = handler<Render3DEvent> {
        if (cancelRun) {
            target = null
            return@handler
        }

        target ?: return@handler
    }

    /**
     * Attack event
     */
    val attackEvent = handler<AttackEvent> {
        // TODO: Use target, instead
        val currentTarget = this.target ?: return@handler

        val player = mc.player ?: return@handler
        val world = mc.world ?: return@handler

        val switchMode = targetMode == "Switch"

        if (!switchMode || switchTimer.hasTimePassed(switchDelay)) {
            prevTargetEntities += currentTarget.networkId

            if (switchMode) {
                switchTimer.reset()
            }
        }

        if (shouldPrioritize()) return@handler
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        if (shouldPrioritize()) return

        // Reset fixed target to null
        target = null

        val switchMode = targetMode == "Switch"

        val world = mc.world ?: return
        val player = mc.player ?: return

        var bestTarget: LivingEntity? = null
        var bestValue: Double? = null

        for (entity in world.entities) {
            if (entity !is LivingEntity || !isSelected(
                    entity, true
                ) || switchMode && entity.networkId in prevTargetEntities
            ) continue

            val distance = /*Backtrack.runWithNearestTrackedDistance(entity) { */player.getDistanceToEntityBox(entity) //}

            if (switchMode && distance !in range && prevTargetEntities.isNotEmpty()) continue

            val entityFov = rotationDifference(entity)

            if (distance !in range || fov != 180F && entityFov > fov) continue

            if (switchMode && !isLookingOnEntities(entity, maxSwitchFOV.toDouble())) continue

            // Credits to Gugustus / Augustus b2.6
            // TODO: Maybe we should also prioritize players that are looking at you, with weapons (or without), and breaking blocks (could be possibly trying to break your bed?)
            val optimal = (distance * 2.0) + (entity.health.toDouble() + entity.absorption) + (entity.damagedTimer.toDouble() * 4.0) /*+ (entity.totalArmorValue.toDouble() / 2.0) */+ (entityFov.toDouble() / 2.0)

            val currentValue = when (priority) {
                "Optimal" -> optimal
                "Distance" -> distance
                "Direction" -> entityFov.toDouble()
                "Health" -> entity.health.toDouble()
                "LivingTime" -> -entity.ticks.toDouble()
                //"Armor" -> entity.totalArmorValue.toDouble()
                "HurtResistance" -> entity.invulnerableTimer.toDouble()
                "HurtTime" -> entity.damagedTimer.toDouble()
                "HealthAbsorption" -> (entity.health + entity.absorption).toDouble()
                //"RegenAmplifier" -> if (entity.hasStatusEffect(Potion.regeneration)) {
                    //entity.getEffectInstance(Potion.regeneration).amplifier.toDouble()
                //} else -1.0

                "InWeb" -> if (entity.inCobweb) -1.0 else Double.MAX_VALUE
                "OnLadder" -> if (entity.isClimbing) -1.0 else Double.MAX_VALUE
                "InLiquid" -> if (entity.inWater || entity.isInLava) -1.0 else Double.MAX_VALUE
                else -> null
            } ?: continue

            if (bestValue == null || currentValue < bestValue) {
                bestValue = currentValue
                bestTarget = entity
            }
        }

        if (bestTarget != null) {
            /*if (Backtrack.runWithNearestTrackedDistance(bestTarget) { updateRotations(bestTarget) }) {
                target = bestTarget
                return
            }*/
            if (updateRotations(bestTarget)) {
                target = bestTarget
                return
            }
        }

        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Update rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        val player = mc.player ?: return false

        if (clickOnly && (clickTimer.hasTimePassed(clickDelay) || !mc.options.attackKey.isPressed && AutoClicker.handleEvents())) {
            return false
        }

        // Should it always keep trying to lock on the enemy or just try to assist you?
        if (!lock && isFaced(entity, attackRange.toDouble())) return false

        if (player.getDistanceToEntityBox(entity) !in range) return false

        if (shouldPrioritize()) return false

        val prediction = entity.currPos.subtract(entity.last).times(2 + predictEnemyPosition.toDouble())
        val shape = entity.hitBox.moved(prediction)
        val (currPos, oldPos) = player.currPos to player.last

        val simPlayer = SimulatedPlayer.fromClientPlayer(RotationUtils.modifiedInput)

        simPlayer.yaw = (currentRotation ?: player.rotation).yaw

        var pos = currPos

        repeat(predictClientMovement) {
            val previousPos = simPlayer.pos

            simPlayer.tick()

            player.setPosAndPrevPos(simPlayer.pos)

            val simDist = player.getDistanceToEntityBox(entity)

            player.setPosAndPrevPos(previousPos)

            val prevDist = player.getDistanceToEntityBox(entity)

            player.setPosAndPrevPos(currPos, oldPos)
            pos = simPlayer.pos

            if (predictOnlyWhenOutOfRange && simDist !in range && simDist <= prevDist) {
                return@repeat
            }

            pos = previousPos
        }

        player.setPosAndPrevPos(pos)

        val rotation = searchCenter(
            shape,
            generateSpotBasedOnDistance,
            outBorder,
            randomization,
            predict = false,
            lookRange = range.endInclusive,
            attackRange = attackRange,
            throughWallsRange = throughWallsRange.endInclusive,
            bodyPoints = listOf(highestBodyPointToTarget, lowestBodyPointToTarget),
            horizontalSearch = horizontalBodySearchRange
        )

        if (rotation != null) setTargetRotation(rotation, options = options)

        player.setPosAndPrevPos(currPos, oldPos)

        return rotation != null
    }

    private fun shouldPrioritize(): Boolean = when {
        //!onScaffold && (Scaffold.handleEvents() && (Scaffold.placeRotation != null || currentRotation != null) || Tower.handleEvents() && Tower.isTowering) -> true

        //!onDestroyBlock && (Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null && !Fucker.isOwnBed || Nuker.handleEvents()) -> true

        activationSlot && SilentHotbar.currentSlot != preferredSlot - 1 -> true

        else -> false
    }

    private val cancelRun
        inline get() = mc.player.isSpectator || !isAlive(mc.player) || (notOnConsume && isConsumingItem()) || (onDestroyBlock ||  mc.crosshairTarget.type != HitResult.Type.BLOCK)

    private fun isAlive(entity: LivingEntity) = entity.isAlive && entity.health > 0

    override val tag
        get() = targetMode + if (options.applyServerSide) ", Silent" else ""

    val isBlockingChestAura
        get() = handleEvents() && target != null
}
