package net.librebounce.features.module.base.settings

import net.librebounce.config.Configurable
import net.librebounce.features.module.base.Module
import net.librebounce.utils.extensions.safeDiv
import net.librebounce.utils.timing.MSTimer
import kotlin.math.roundToInt

open class ClickingSettings(owner: Module, prefix: String = "", generalApply: () -> Boolean = { true }): Configurable("ClickingSettings") {
    private val cps by intRange(prefix + "CPS", 8..12, 0..50) { generalApply() }
    private val simulateDoubleClicking by boolean(prefix + "SimulateDoubleClicking", false) { generalApply() }
    private val doubleClicks by intRange(prefix + "DoubleClicks", -1..2, -5..5)

    init {
        owner.addValues(this.values)
    }

    private var delay = randomClickDelay(cps)
    private var lastClick = MSTimer()
    var clicks = if (simulateDoubleClicking) doubleClicks.random() + 1 else 1

    fun canClick(): Boolean {
        if (lastClick.hasTimePassed(delay)) {
            clicks = if (simulateDoubleClicking) doubleClicks.random() + 1 else 1
            delay = randomClickDelay(cps)
            lastClick.reset()
            return true
        }
        return false
    }

    fun randomClickDelay(minCPS: Int, maxCPS: Int): Int {
        val minDelay = 1000 safeDiv minCPS
        val maxDelay = 1000 safeDiv maxCPS
        return (Math.random() * (minDelay - maxDelay) + maxDelay).roundToInt()
    }

    fun randomClickDelay(cps: IntRange): Int {
        return randomClickDelay(cps.first, cps.last)
    }
}
