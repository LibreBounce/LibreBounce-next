package net.librebounce.features.module.base

import net.librebounce.config.Configurable
import net.librebounce.features.module.Module
import net.librebounce.utils.kotlin.RandomUtils.nextInt
import net.librebounce.utils.timing.MSTimer
import net.librebounce.utils.timing.TimeUtils.randomClickDelay

open class ClickingSettings(owner: Module, prefix: String = "", generalApply: () -> Boolean = { true }): Configurable("ClickingSettings") {
    open val cps by intRange(prefix + "CPS", 8..12, 0..50) { generalApply() }
    open val simulateDoubleClicking by boolean(prefix + "SimulateDoubleClicking", false) { generalApply() }

    private var delay = randomClickDelay(cps)
    private var lastClick = MSTimer()
    var doubleClick = if (simulateDoubleClicking) nextInt(-1, 1) else 0

    fun canClick(): Boolean {
        if (lastClick.getTime() >= delay) {
            doubleClick = if (simulateDoubleClicking) nextInt(-1, 1) else 0
            delay = randomClickDelay(cps)
            lastClick.reset()
            return true
        }
        return false
    }
}
