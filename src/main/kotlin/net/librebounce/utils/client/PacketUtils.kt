package net.librebounce.utils.client

import kotlinx.coroutines.Dispatchers
import net.librebounce.event.*
/*import net.librebounce.features.module.modules.combat.FakeLag
import net.librebounce.features.module.modules.combat.Velocity
import net.librebounce.injection.implementations.IMixinEntity*/
import net.librebounce.utils.extensions.currPos
import net.librebounce.utils.kotlin.removeEach
import net.librebounce.utils.render.RenderUtils
import net.librebounce.utils.rotation.Rotation
import net.minecraft.entity.living.LivingEntity
import net.minecraft.network.Connection
import net.minecraft.network.packet.Packet
import net.minecraft.client.network.handler.ClientPlayPacketHandler
import net.minecraft.client.render.Camera.dx
import net.minecraft.client.render.Camera.dy
import net.minecraft.client.render.Camera.dz
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.concurrent.write
import kotlin.math.roundToInt

object PacketUtils : MinecraftInstance, Listenable {

    private val queuedPackets = ArrayDeque<Packet<*>>()
    private val queueLock = ReentrantLock()

    fun schedulePacketProcess(elements: Collection<Packet<*>>): Boolean = queueLock.withLock {
        queuedPackets.addAll(elements)
    }

    fun schedulePacketProcess(element: Packet<*>): Boolean = queueLock.withLock {
        queuedPackets.add(element)
    }

    fun isQueueEmpty(): Boolean = queueLock.withLock {
        queuedPackets.isEmpty()
    }

    /*val onTick = handler<GameTickEvent>(priority = 2) {
        for (entity in mc.world.entities) {
            if (entity is LivingEntity) {
                (entity as? Entity)?.apply {
                    if (!truePos) {
                        updateSpawnPosition(entity.currPos)
                    }
                }
            }
        }
    }*/

    /*val onPacket = handler<PacketEvent>(dispatcher = Dispatchers.Main, priority = 2) { event ->
        val world = mc.world ?: return@handler

        when (val packet = event.packet) {
            is AddPlayerS2CPacket -> (world.getEntity(packet.id) as? Entity)?.apply {
                updateSpawnPosition(Vec3d(packet.realX, packet.realY, packet.realZ))
            }

            is AddMobS2CPacket -> (world.getEntity(packet.id) as? Entity)?.apply {
                updateSpawnPosition(Vec3d(packet.realX, packet.realY, packet.realZ))
            }

            is EntityMoveS2CPacket -> {
                val entity = packet.getEntity(world)
                val mixinEntity = entity as? Entity

                mixinEntity?.apply {
                    if (!truePos) {
                        updateSpawnPosition(entity.currPos)
                    }

                    trueX += packet.realMotionX
                    trueY += packet.realMotionY
                    trueZ += packet.realMotionZ
                }
            }

            is EntityTeleportS2CPacket -> (world.getEntity(packet.id) as? Entity)?.apply {
                updateSpawnPosition(Vec3d(packet.realX, packet.realY, packet.realZ), true)
            }
        }
    }*/

    val onGameLoop = handler<GameLoopEvent>(priority = -5) {
        if (EventManager.call(DelayedPacketProcessEvent()).isCancelled) {
            return@handler
        }

        queueLock.withLock {
            queuedPackets.removeEach { packet ->
                handlePacket(packet)
                val packetEvent = PacketEvent(packet, EventState.RECEIVE)
                //EventManager.call(packetEvent, FakeLag)
                //EventManager.call(packetEvent, Velocity)

                true
            }
        }
    }

    val onWorld = handler<WorldEvent>(priority = -1) { event ->
        if (event.clientWorld == null) {
            queueLock.withLock {
                queuedPackets.clear()
            }
        }
    }

    @JvmStatic
    fun sendPacket(packet: Packet<*>, triggerEvent: Boolean = true) {
        if (triggerEvent) {
            mc.networkHandler?.sendPacket(packet)
            return
        }

        val netManager = mc.networkHandler?.connection ?: return

        PPSCounter.registerType(PPSCounter.PacketType.SEND)
        if (netManager.isConnected) {
            netManager.flushQueue()
            netManager.doSend(packet, null)
        } else {
            netManager.lock.write {
                netManager.sendQueue += Connection.QueuedPacket(packet, null)
            }
        }
    }

    @JvmStatic
    fun sendPackets(vararg packets: Packet<*>, triggerEvents: Boolean = true) =
        packets.forEach { sendPacket(it, triggerEvents) }

    @JvmStatic
    fun handlePackets(vararg packets: Packet<*>) =
        packets.forEach { handlePacket(it) }

    @JvmStatic
    private fun handlePacket(packet: Packet<*>?) {
        runCatching { (packet as Packet<ClientPlayPacketHandler>).handle(mc.networkHandler) }.onSuccess {
            PPSCounter.registerType(PPSCounter.PacketType.RECEIVED)
        }
    }
}

/*fun Entity.updateSpawnPosition(target: Vec3d, ignoreInterpolation: Boolean = false) {
    trueX = target.x
    trueY = target.y
    trueZ = target.z
    if (!ignoreInterpolation) {
        lerpX = trueX
        lerpY = trueY
        lerpZ = trueZ
    }
    truePos = true
}

fun interpolatePosition(entity: Entity) = entity.run {
    val delta = RenderUtils.deltaTimeNormalized(3)

    lerpX += (trueX - lerpX) * delta
    lerpY += (trueY - lerpY) * delta
    lerpZ += (trueZ - lerpZ) * delta
}*/

var EntityVelocityS2CPacket.realMotionX
    get() = velocityX / 8000.0
    set(value) {
        velocityX = (value * 8000.0).roundToInt()
    }
var EntityVelocityS2CPacket.realMotionY
    get() = velocityY / 8000.0
    set(value) {
        velocityY = (value * 8000.0).roundToInt()
    }
var EntityVelocityS2CPacket.realMotionZ
    get() = velocityZ / 8000.0
    set(value) {
        velocityZ = (value * 8000.0).roundToInt()
    }

val EntityMoveS2CPacket.realMotionX
    get() = dx() / 32.0
val EntityMoveS2CPacket.realMotionY
    get() = dy() / 32.0
val EntityMoveS2CPacket.realMotionZ
    get() = dz() / 32.0

var AddEntityS2CPacket.realX
    get() = x / 32.0
    set(value) {
        x = (value * 32.0).roundToInt()
    }
var AddEntityS2CPacket.realY
    get() = y / 32.0
    set(value) {
        y = (value * 32.0).roundToInt()
    }
var AddEntityS2CPacket.realZ
    get() = z / 32.0
    set(value) {
        z = (value * 32.0).roundToInt()
    }

val AddPlayerS2CPacket
        .realX
    get() = x / 32.0
val AddPlayerS2CPacket
        .realY
    get() = y / 32.0
val AddPlayerS2CPacket
        .realZ
    get() = z / 32.0

val AddMobS2CPacket.realX
    get() = x / 32.0
val AddMobS2CPacket.realY
    get() = y / 32.0
val AddMobS2CPacket.realZ
    get() = z / 32.0

val EntityTeleportS2CPacket.realX
    get() = x / 32.0
val EntityTeleportS2CPacket.realY
    get() = y / 32.0
val EntityTeleportS2CPacket.realZ
    get() = z / 32.0

val BlockBBEvent.pos
    get() = BlockPos(x, y, z)

var PlayerMoveC2SPacket.rotation
    get() = Rotation(yaw, pitch)
    set(value) {
        yaw = value.yaw
        pitch = value.pitch
    }

var PlayerMoveC2SPacket.pos
    get() = Vec3d(x, minY, z)
    set(value) {
        x = value.x
        minY = value.y
        z = value.z
    }

