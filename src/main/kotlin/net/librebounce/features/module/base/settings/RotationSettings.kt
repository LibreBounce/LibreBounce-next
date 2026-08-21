package net.librebounce.features.module.base.settings

import net.librebounce.config.Configurable
import net.librebounce.config.ListValue
import net.librebounce.features.module.base.Module
import net.librebounce.utils.extensions.plus
import net.librebounce.utils.extensions.random
import net.librebounce.utils.extensions.times
import net.librebounce.utils.rotation.Rotation
import net.librebounce.utils.rotation.RotationUtils.angleDifference
import net.librebounce.utils.rotation.RotationUtils.getRotationVector
import net.librebounce.utils.rotation.RotationUtils.lastRotations
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.math.sign
import kotlin.math.abs

class AlwaysRotationSettings(owner: Module, generalApply: () -> Boolean = { true }) :
    RotationSettings(owner, generalApply) {
    //override val rotations = super.rotations.apply { excludeWithState(true) }
    override val rotationsActive: Boolean = true
}

@Suppress("MemberVisibilityCanBePrivate")
open class RotationSettings(owner: Module, generalApply: () -> Boolean = { true }) : Configurable("RotationSettings") {

    // TODO: Currently, any rotation modules affect legit rotations, even when they're not doing anything
    // or even turned off
    // This is a high priority issue
    open val rotations by boolean("Rotations", true) { generalApply() }
    open val applyServerSide by boolean("ApplyServerSide", true) { rotationsActive && generalApply() }

    open val simulateShortStop by boolean("SimulateShortStop", false) { rotationsActive && generalApply() }
    open val rotationDiffBuildUpToStop by float("RotationDiffBuildUpToStop", 180f, 50f..720f) { simulateShortStop }
    open val maxThresholdAttemptsToStop by int("MaxThresholdAttemptsToStop", 1, 0..5) { simulateShortStop }
    open val shortStopDuration by intRange("ShortStopDuration", 1..2, 1..5, suffix = "ticks") { simulateShortStop }

    open val strafe by boolean("Strafe", false) { rotationsActive && applyServerSide && generalApply() }
    open val strict by boolean("Strict", false) { strafe && generalApply() }
    open val keepRotation by boolean("KeepRotation", true) { rotationsActive && applyServerSide && generalApply() }

    open val resetTicks by int("ResetTicks", 1, 1..20) {
        rotationsActive && applyServerSide && generalApply()
    }

    // TODO: Add reaction time, a speed curve, and a lazy option that only rotates as much as required
    open val legitimize by boolean("Legitimize", false) { rotationsActive && generalApply() }
    open val legitimizeHorizontalJitter by
        floatRange("LegitimizeHorizontalJitter", -0.03f..0.03f, -1f..1f) { rotationsActive && generalApply() && legitimize }
    open val legitimizeVerticalJitter by
        floatRange("LegitimizeVerticalJitter", -0.02f..0.02f, -1f..1f) { rotationsActive && generalApply() && legitimize }
    open val legitimizeHorizontalSlowdown by
        floatRange("LegitimizeHorizontalSlowdown", 0f..0.1f, 0f..1f) { rotationsActive && generalApply() && legitimize }
    open val legitimizeVerticalSlowdown by
        floatRange("LegitimizeVerticalSlowdown", 0f..0.1f, 0f..1f) { rotationsActive && generalApply() && legitimize }
    open val legitimizeHorizontalImperfectCorrelationFactor by
        floatRange("LegitimizeHorizontalImperfectCorrelationFactor", 0.9f..1.1f, 0f..2f) { rotationsActive && generalApply() && legitimize }
    open val legitimizeVerticalImperfectCorrelationFactor by
        floatRange("LegitimizeVerticalImperfectCorrelationFactor", 0.9f..1.1f, 0f..2f) { rotationsActive && generalApply() && legitimize }

    open val horizontalAngleChange by
        floatRange("HorizontalAngleChange", 180f..180f, 1f..180f, suffix = "º") { rotationsActive && generalApply() }
    open val verticalAngleChange by
        floatRange("VerticalAngleChange", 180f..180f, 1f..180f, suffix = "º") { rotationsActive && generalApply() }

    open val angleResetDifference by float("AngleResetDifference", 5f/*.withGCD()*/, 0.0f..180f, suffix = "º") {
        rotationsActive && applyServerSide && generalApply()
    }

    open val minRotationDifference by float(
        "MinRotationDifference", 2f, 0f..4f
    ) { rotationsActive && generalApply() }

    open val minRotationDifferenceResetTiming by choices(
        "MinRotationDifferenceResetTiming", arrayOf("OnStart", "OnSlowDown", "Always"), "OnStart"
    ) { rotationsActive && generalApply() }

    var prioritizeRequest = false
    var immediate = false
    var instant = false

    var rotDiffBuildUp = 0f
    var maxThresholdReachAttempts = 0

    open val rotationsActive
        get() = rotations

    /*fun withoutKeepRotation() = apply {
        keepRotation.excludeWithState()
    }*/

    fun updateSimulateShortStopData(diff: Float) {
        rotDiffBuildUp += diff
    }

    fun resetSimulateShortStopData() {
        rotDiffBuildUp = 0f
        maxThresholdReachAttempts = 0
    }

    fun shouldPerformShortStop(): Boolean {
        if (abs(rotDiffBuildUp) < rotationDiffBuildUpToStop || !simulateShortStop) return false

        if (maxThresholdReachAttempts < maxThresholdAttemptsToStop) {
            maxThresholdReachAttempts++
            return false
        }

        return true
    }

    init {
        owner.addValues(this.values)
    }
}

class RotationSettingsWithRotationModes(
    owner: Module, listValue: ListValue, generalApply: () -> Boolean = { true },
) : RotationSettings(owner, generalApply) {

    override val rotations = super.rotations.apply { excludeWithState() }

    val rotationModeValues = listValue.setSupport { generalApply() }

    val rotationMode by +rotationModeValues

    override val rotationsActive: Boolean
        get() = rotationMode != "Off"
}

class RandomizationSettings(owner: Module, val generalApply: () -> Boolean = { true }) : Configurable("Randomization") {

    // TODO: Add an option to increase randomization exponentially, with higher movement speeds
    private val randomizationPattern by choices(
        "RandomizationPattern", arrayOf("None", "Zig-Zag", "LazyFlick"), "None"
    ) { generalApply() }
    private val yawRandomizationChance by floatRange(
        "YawRandomizationChance", 0.8f..1.0f, 0f..1f
    ) { randomizationChosen }
    private val yawRandomizationRange by floatRange(
        "YawRandomizationRange",
        5f..10f,
        0f..30f
    ) { isZigZagActive && randomizationChosen && yawRandomizationChance.start != 1F }
    private val yawSpeedIncreaseMultiplier by intRange(
        "YawSpeedIncreaseMultiplier", 50..120, 0..500, suffix = "%"
    ) { !isZigZagActive && randomizationChosen && yawRandomizationChance.start != 1F }
    private val yawPrevSmoothingTicks by intRange(
        "YawPrevSmoothingTicks",
        2..2,
        0..2
    ) { randomizationChosen && pitchRandomizationChance.start != 1F }
    private val pitchRandomizationChance by floatRange(
        "PitchRandomizationChance", 0.8f..1.0f, 0f..1f
    ) { randomizationChosen }
    private val pitchRandomizationRange by floatRange(
        "PitchRandomizationRange",
        5f..10f,
        0f..30f
    ) { randomizationChosen && pitchRandomizationChance.start != 1F }
    private val pitchPrevSmoothingTicks by intRange(
        "PitchPrevSmoothingTicks",
        2..2,
        0..2
    ) { randomizationChosen && pitchRandomizationChance.start != 1F }
    private val pitchPrevSmoothingTicksRandom by intRange(
        "PitchPrevSmoothingTicksRandom",
        -1..1,
        -5..5
    ) { randomizationChosen && pitchRandomizationChance.start != 1F }

    private val isZigZagActive
        get() = randomizationPattern == "Zig-Zag"

    val randomizationChosen
        get() = randomizationPattern != "None" && generalApply()

    fun processNextSpot(box: Box, rotation: Rotation, eyes: Vec3d, range: Double) {
        val intercept = box.clip(eyes, eyes + getRotationVector(lastRotations.random()) * range)

        // Smooth out randomized rotation pattern using previous rotation to simulate natural movement
        val pitchMovement =
            angleDifference(rotation.pitch, lastRotations[pitchPrevSmoothingTicks.random()].pitch).sign.takeIf { it != 0f } ?: pitchPrevSmoothingTicksRandom.random()
                .toFloat()
        val yawMovement = angleDifference(rotation.yaw, lastRotations[yawPrevSmoothingTicks.random()].yaw)

        val yawSign = yawMovement.sign.takeIf { it != 0f } ?: arrayOf(-1f, 1f).random()

        val yawIncrease = if (Math.random() > yawRandomizationChance.random()) {
            if (!isZigZagActive) {
                yawSpeedIncreaseMultiplier.random() / 100f * yawMovement
            } else {
                yawRandomizationRange.random() * yawSign
            }
        } else 0f

        val pitchIncrease = if (Math.random() > pitchRandomizationChance.random()) {
            if (!isZigZagActive) {
                pitchRandomizationRange.random() + pitchMovement
            } else {
                pitchRandomizationRange.random() * pitchMovement
            }
        } else 0f

        if (isZigZagActive || intercept?.facePos == null) {
            rotation.yaw += yawIncrease
            rotation.pitch += pitchIncrease

            rotation.fixedSensitivity()
        }
    }

    init {
        owner.addValues(this.values)
    }
}
