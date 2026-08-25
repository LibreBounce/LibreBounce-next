package net.librebounce.utils.movement

import net.librebounce.event.*
import net.librebounce.utils.client.MinecraftInstance
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket

object TimerBalanceUtils : MinecraftInstance, Listenable {
    var balance = 0L
        private set

    private var frametime = -1L
    private var prevframetime = -1L
    private var currframetime = -1L

    private val inGame: Boolean
        get() = mc.player != null && mc.world != null && mc.networkHandler != null && mc.interactionManager != null

    val onGameLoop = handler<GameLoopEvent> {
        if (frametime == -1L) {
            frametime = 0L
            currframetime = System.currentTimeMillis()
            prevframetime = currframetime
        }

        prevframetime = currframetime
        currframetime = System.currentTimeMillis()
        frametime = currframetime - prevframetime

        if (inGame) {
            balance -= frametime
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        if (inGame) {
            if (event.packet is PlayerMoveC2SPacket) {
                balance += 50
            }
        }
    }

    val onWorld = handler<WorldEvent> {
        balance = 0
    }
}
