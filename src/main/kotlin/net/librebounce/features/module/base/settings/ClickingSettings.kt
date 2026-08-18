package net.librebounce.features.module.base.settings

import net.librebounce.config.Configurable
import net.librebounce.features.module.base.Module
import net.librebounce.utils.extensions.safeDiv
import net.librebounce.utils.timing.MSTimer
import kotlin.math.roundToInt

open class ClickingSettings(owner: Module, prefix: String = "", generalApply: () -> Boolean = { true }): Configurable("ClickingSettings") {
    private val cps by intRange(prefix + "CPS", 8..12, 0..50) { generalApply() }
    private val clicksAtATime by intRange(prefix + "ClicksAtATime", 1..1, 0..5) { generalApply() }

    init {
        owner.addValues(this.values)
    }

    private var delay = randomClickDelay(cps)
    private var lastClick = MSTimer()
    var clicks = clicksAtATime.random()

    fun canClick(): Boolean {
        if (lastClick.hasTimePassed(delay)) {
            clicks = clicksAtATime.random()
            delay = randomClickDelay(cps)
            lastClick.reset()
            return true
        }
        return false
    }

    fun randomClickDelay(cps: IntRange): Int {
        val minDelay = 1000 safeDiv cps.first
        val maxDelay = 1000 safeDiv cps.last
        return (Math.random() * (minDelay - maxDelay) + maxDelay).roundToInt()
    }
}
