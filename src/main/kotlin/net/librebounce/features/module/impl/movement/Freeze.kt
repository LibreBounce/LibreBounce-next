/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.features.module.impl.movement

import net.librebounce.event.PacketEvent
import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.Category
import net.librebounce.features.module.Module
import net.minecraft.client.Minecraft
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.PlayerMoveS2CPacket

object Freeze : Module("Freeze", Category.MOVEMENT) {
    private var velocityX = 0.0
    private var velocityY = 0.0
    private var velocityZ = 0.0
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0

    override fun onEnable() {
        x = mc.player.x
        y = mc.player.y
        z = mc.player.z
        velocityX = mc.player.velocityX
        velocityY = mc.player.velocityY
        velocityZ = mc.player.velocityZ
    }

    val onUpdate = handler<UpdateEvent> {
        mc.player.velocityX = 0.0
        mc.player.velocityY = 0.0
        mc.player.velocityZ = 0.0
        mc.player.updatePositionAndAngles(x, y, z, mc.player.yaw, mc.player.pitch)
    }

    val onPacket = handler<PacketEvent> { event ->
        if (event.packet is PlayerMoveC2SPacket)
            event.cancelEvent()

        if (event.packet is PlayerMoveS2CPacket) {
            x = event.packet.x
            y = event.packet.y
            z = event.packet.z
            velocityX = 0.0
            velocityY = 0.0
            velocityZ = 0.0
        }
    }

    override fun onDisable() {
        mc.player.velocityX = velocityX
        mc.player.velocityY = velocityY
        mc.player.velocityZ = velocityZ
        mc.player.updatePositionAndAngles(x, y, z, mc.player.yaw, mc.player.pitch)
    }
}
