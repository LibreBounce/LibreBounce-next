package net.librebounce.features.module.impl.combat

import net.librebounce.features.module.Category
import net.librebounce.features.module.Module

object HitDetector : Module("HitDetector", Category.COMBAT) {
    val hitDelay by int("HitDelay", 400, 0..1000)
    val debug by boolean("Debug", false)
}
