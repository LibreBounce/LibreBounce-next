package net.librebounce.utils.extensions

import net.minecraft.client.entity.living.player.Input

fun Input.reset() {
    this.forwardSpeed = 0f
    this.movementSideways = 0f
    this.jump = false
    this.sneak = false
}

val Input.isSideways
    get() = forwardSpeed != 0f && movementSideways != 0f

val Input.isMoving
    get() = forwardSpeed != 0f || movementSideways != 0f