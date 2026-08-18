package net.librebounce.features.module.base.settings

import net.librebounce.config.Configurable
import net.librebounce.config.ListValue
import net.librebounce.features.module.base.Module
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
