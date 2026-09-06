package net.librebounce.utils.extensions

import net.minecraft.client.entity.living.player.Input

fun Input.reset() {
    this.movementForward = 0f
    this.movementSideways = 0f
    this.jumping = false
    this.sneaking = false
}

val Input.isSideways
    get() = movementForward != 0f && movementSideways != 0f

val Input.isMoving
    get() = movementForward != 0f || movementSideways != 0f