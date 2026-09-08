package net.librebounce.features.module.modules.world

import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.modules.world.scaffold.components.EagleComponent

object Eagle : Module("Eagle", Category.WORLD) {
    private val eagleComponent = EagleComponent(this, true)
}
