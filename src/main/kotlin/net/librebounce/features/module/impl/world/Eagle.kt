package net.librebounce.features.module.impl.world

import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.impl.world.scaffold.EagleComponent

object Eagle : Module("Eagle", Category.WORLD) {
    private val eagle = EagleComponent(this)

    val onUpdate = handler<UpdateEvent> {
        eagle.handle()
    }
}
