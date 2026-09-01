package net.librebounce.features.module.base.settings

import net.librebounce.config.Configurable
import net.librebounce.features.module.base.Module
import net.librebounce.utils.kotlin.RandomUtils.nextRateMilliseconds
import net.librebounce.utils.input.InputUtils.requestClick
import net.librebounce.utils.timing.MSTimer

open class ClickingSettings(owner: Module, prefix: String = "", shouldApply: Boolean = true): Configurable(owner.name) {
    private val cps by intRange(prefix + "CPS", 8..12, 0..50) { shouldApply }
    private val clicksAtATime by intRange(prefix + "ClicksAtATime", 1..1, 0..5) { shouldApply }

    private var delay = nextRateMilliseconds(cps)
    private var lastClick = MSTimer()
    var clicks = clicksAtATime.random()

    fun canClick() = lastClick.hasTimePassed(delay) && run {
        clicks = clicksAtATime.random()
        delay = nextRateMilliseconds(cps)
        lastClick.reset()
        true
    }

    fun requestClick(button: Int) {
        if (canClick()) {
            requestClick(button, clicksAtATime)      
        }
    }

    init {
        owner.addValues(this.values)
    }
}