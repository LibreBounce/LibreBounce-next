package net.librebounce.features.module.impl.combat

import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.utils.client.PacketUtils.sendPacket
import net.librebounce.utils.kotlin.RandomUtils.nextInt
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.Action.ATTACK
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Position

object AutoLeave : Module("AutoLeave", Category.COMBAT) {
    private val health by float("Health", 8f, 0f..20f)
    private val mode by choices("Mode", arrayOf("Quit", "InvalidPacket", "SelfHurt", "IllegalChat"), "Quit")

    val onUpdate = handler<UpdateEvent> {
        val player = mc.player ?: return@handler

        if (player.health <= health && !player.abilities.creativeMode && !mc.isIntegratedServerRunning) {
            when (mode) {
                "Quit" -> mc.world.disconnect()
                "InvalidPacket" -> sendPacket(
                    Position(
                        Double.NaN,
                        Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        !player.onGround
                    )
                )
                "SelfHurt" -> sendPacket(PlayerInteractEntityC2SPacket(player, ATTACK))
                "IllegalChat" -> player.sendChat(nextInt().toString() + "§§§" + nextInt())
            }

            state = false
        }
    }
}