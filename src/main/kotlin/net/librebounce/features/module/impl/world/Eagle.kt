package net.librebounce.features.module.impl.world

import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.impl.world.scaffold.components.EagleComponent

object Eagle : Module("Eagle", Category.WORLD) {
    private var shouldDebug = false
    private val eagleComponent = EagleComponent(this, debug = shouldDebug)
    private val debug by boolean("Debug", false).onChange { _, new ->
        shouldDebug = debug
    }
}
