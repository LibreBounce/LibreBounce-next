/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.utils.extensions

//import net.librebounce.file.FileManager.friendsConfig
//import net.librebounce.injection.implementations.IMixinEntity
//import net.librebounce.utils.attack.CPSCounter
/*import net.librebounce.utils.block.set
import net.librebounce.utils.block.state
import net.librebounce.utils.block.toVec*/
//import net.librebounce.utils.client.MinecraftInstance.Companion.mc
/*import net.librebounce.utils.client.PacketUtils.sendPacket
import net.librebounce.utils.inventory.SilentHotbar
import net.librebounce.utils.movement.MovementUtils
import net.librebounce.utils.render.ColorUtils.stripColor*/
import net.librebounce.utils.rotation.Rotation
import net.librebounce.utils.rotation.RotationUtils.getFixedSensitivityAngle
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.living.LivingEntity
import net.minecraft.entity.living.mob.GolemEntity
import net.minecraft.entity.living.mob.MobEntity
import net.minecraft.entity.living.mob.ambient.BatEntity
import net.minecraft.entity.living.mob.monster.GhastEntity
import net.minecraft.entity.living.mob.monster.SlimeEntity
import net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity
import net.minecraft.entity.living.mob.passive.VillagerEntity
import net.minecraft.entity.living.mob.passive.animal.AnimalEntity
import net.minecraft.entity.living.mob.water.SquidEntity
/*import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager*/
import net.minecraft.entity.living.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.ArmSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerUseC2SPacket
import net.minecraft.util.math.Box
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
//import net.minecraftforge.event.ForgeEventFactory

val mc = Minecraft.getInstance()

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity) = eyes.distanceTo(getNearestPointBB(eyes, entity.hitBox))

fun Entity.getDistanceToBox(box: Box) = eyes.distanceTo(getNearestPointBB(eyes, box))

fun LocalClientPlayerEntity.isNearEdge(threshold: Float): Boolean {
    val playerPos = Vec3d(x, y, z)
    val pos = BlockPos(playerPos)

    val mutable = BlockPos.Mutable()
    for (x in -3..3) {
        for (z in -3..3) {
            val checkPos = mutable.set(x, y.toInt(), z)
            if (world.isAir(checkPos)) {
                val checkPosCenter = Vec3d(checkPos.x + 0.5, checkPos.y.toDouble(), checkPos.z + 0.5)
                val distance = playerPos.distanceTo(checkPosCenter)
                if (distance <= threshold) {
                    return true
                }
            }
        }
    }
    return false
}

fun getNearestPointBB(eye: Vec3d, box: Box):Vec3d {
    val origin = doubleArrayOf(eye.x, eye.y, eye.z)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        if (origin[i] > destMaxs[i]) origin[i] = destMaxs[i] else if (origin[i] < destMins[i]) origin[i] = destMins[i]
    }
    return Vec3d(origin[0], origin[1], origin[2])
}

fun PlayerEntity.getPing() = Minecraft.getInstance().networkHandler.getOnlinePlayer(uuid)?.ping ?: 0

fun Entity.isAnimal() =
    this is AnimalEntity
            || this is SquidEntity
            || this is GolemEntity
            || this is BatEntity

fun Entity.isMob() =
    this is MobEntity
            || this is VillagerEntity
            || this is SlimeEntity
            || this is GhastEntity
            || this is EnderDragonEntity

/*fun PlayerEntity.isClientFriend(): Boolean {
    val entityName = name ?: return false

    return friendsConfig.isFriend(stripColor(entityName))
}*/

var Entity?.rotation
    get() = Rotation(this?.yaw ?: 0f, this?.pitch ?: 0f)
    set(value) {
        this?.run {
            yaw = value.yaw
            pitch = value.pitch
        }
    }
var Entity?.prevRotation
    get() = Rotation(this?.lastYaw ?: 0f, this?.lastPitch ?: 0f)
    set(value) {
        this?.run {
            lastYaw = value.yaw
            lastPitch = value.pitch
        }
    }

val Entity.hitBox: Box
    get() {
        val borderSize = pickRadius.toDouble()
        return shape.grown(borderSize, borderSize, borderSize)
    }

val Entity.eyes: Vec3d
    get() = getEyePosition(1f)

val Entity.last: Vec3d
    get() = Vec3d(lastX, lastY, lastZ)

val Entity.currPos: Vec3d
    get() = this.commandSourcePos

val Entity.lastTickPos: Vec3d
    get() = Vec3d(prevX, prevY, prevZ)

val LivingEntity?.isMoving: Boolean
    get() = this?.run { forwardSpeed != 0F || sidewaysSpeed != 0F } == true

/*val LocalClientPlayerEntity.airTicks
    get() = MovementUtils.airTicks

val LocalClientPlayerEntity.groundTicks
    get() = MovementUtils.groundTicks*/

val Entity.isInLiquid: Boolean
    get() = inWater || isInLava

fun Entity.setPosAndPrevPos(currPos: Vec3d, last: Vec3d = currPos, lastTickPos: Vec3d? = null) {
    setPosition(currPos.x, currPos.y, currPos.z)
    lastX = last.x
    lastY = last.y
    lastZ = last.z

    lastTickPos?.let {
        this.prevX = it.x
        this.prevY = it.y
        this.prevZ = it.z
    }
}

fun LocalClientPlayerEntity.setFixedSensitivityAngles(yaw: Float? = null, pitch: Float? = null) {
    if (yaw != null) fixedSensitivityYaw = yaw

    if (pitch != null) fixedSensitivityPitch = pitch
}

var LocalClientPlayerEntity.fixedSensitivityYaw
    get() = getFixedSensitivityAngle(mc.player.yaw)
    set(yaw) {
        yaw = getFixedSensitivityAngle(yaw, yaw)
    }

var LocalClientPlayerEntity.fixedSensitivityPitch
    get() = getFixedSensitivityAngle(pitch)
    set(pitch) {
        pitch = getFixedSensitivityAngle(pitch.coerceIn(-90f, 90f), pitch)
    }

/*val IMixinEntity.interpolatedPosition
    get() = Vec3d(lerpX, lerpY, lerpZ)*/

// Makes fixedSensitivityYaw, ... += work
operator fun LocalClientPlayerEntity.plusAssign(value: Float) {
    fixedSensitivityYaw += value
    fixedSensitivityPitch += value
}

fun Entity.interpolatedPosition(start: Vec3d, extraHeight: Float? = null) =Vec3d(
    start.x + (x - start.x) * mc.timer.partialTick,
    start.y + (y - start.y) * mc.timer.partialTick + (extraHeight ?: 0f),
    start.z + (z - start.z) * mc.timer.partialTick
)

fun LocalClientPlayerEntity.stopY() {
    velocityY = 0.0
}

fun LocalClientPlayerEntity.stopXZ() {
    velocityX = 0.0
    velocityZ = 0.0
}

fun LocalClientPlayerEntity.stop() {
    stopXZ()
    stopY()
}

/**
 * Its sole purpose is to prevent duplicate sprint state updates.
 */
infix fun LivingEntity.setSprintSafely(new: Boolean) {
    if (new == isSprinting) {
        return
    }

    isSprinting = new
}

// Modified mc.interactionManager.onPlayerRightClick() that sends correct stack in its C08
/*fun LocalClientPlayerEntity.onPlayerRightClick(
    clickPos: BlockPos, side: Direction, clickVec: Vec3d,
    stack: ItemStack? = inventory.items[SilentHotbar.currentSlot],
): Boolean {
    val controller = mc.interactionManager ?: return false

    controller.updateSelectedHotbarSlot()

    if (clickPos !in world.worldBorder)
        return false

    val (facingX, facingY, facingZ) = (clickVec - clickPos.toVec()).toFloatArray()

    val sendClick = {
        sendPacket(PlayerUseC2SPacket(clickPos, side.index, stack, facingX, facingY, facingZ))
        true
    }

    // If player is a spectator, send click and return true
    if (controller.isSpectator)
        return sendClick()

    val item = stack?.item

    if (item?.onItemUseFirst(stack, this, world, clickPos, side, facingX, facingY, facingZ) == true)
        return true

    val blockState = clickPos.state

    // If click had activated a block, send click and return true
    if ((!isSneaking || item == null || item.doesSneakBypassUse(world, clickPos, this))
        && blockState?.block?.onBlockActivated(
            world,
            clickPos,
            blockState,
            this,
            side,
            facingX,
            facingY,
            facingZ
        ) == true
    )
        return sendClick()

    if (item is BlockItem && !item.canPlaceBlockOnSide(world, clickPos, side, this, stack))
        return false

    sendClick()

    if (stack == null)
        return false

    val prevMetadata = stack.metadata
    val prevSize = stack.size

    return stack.onItemUse(this, world, clickPos, side, facingX, facingY, facingZ).also {
        if (controller.hasCreativeInventory) {
            stack.itemDamage = prevMetadata
            stack.size = prevSize
        } else if (stack.size <= 0) {
            ForgeEventFactory.onPlayerDestroyItem(this, stack)
        }
    }
}

// Modified mc.interactionManager.sendUseItem() that sends correct stack in its C08
fun LocalClientPlayerEntity.sendUseItem(stack: ItemStack): Boolean {
    if (mc.interactionManager.isSpectator)
        return false

    mc.interactionManager?.updateSelectedHotbarSlot()

    sendPacket(PlayerUseC2SPacket(stack))

    val prevSize = stack.size

    val newStack = stack.useItemRightClick(world, this)

    return if (newStack != stack || newStack.size != prevSize) {
        if (newStack.size <= 0) {
            mc.player.inventory.items[SilentHotbar.currentSlot] = null
            ForgeEventFactory.onPlayerDestroyItem(mc.player, newStack)
        } else
            mc.player.inventory.items[SilentHotbar.currentSlot] = newStack

        true
    } else false
}*/

fun LocalClientPlayerEntity.tryJump() {
    if (!Minecraft.getInstance().options.jumpKey.isPressed) {
        jump()
    }
}

/*fun LocalClientPlayerEntity.swingArm(silent: Boolean) {
    if (silent) sendPacket(ArmSwingC2SPacket()) else swingArm()
}*/

/*inline fun LocalClientPlayerEntity.attackEntityWithModifiedSprint(
    entity: Entity, affectMovementBySprint: Boolean? = null, swing: () -> Unit
) {
    swing()

    MovementUtils.affectSprintOnAttack = affectMovementBySprint

    try {
        mc.interactionManager?.attackEntity(this, entity)
    } catch (any: Exception) {
        // Unlikely to happen, but if it does, we just want to make sure affectSprintOnAttack is null.
        any.printStackTrace()
    }

    MovementUtils.affectSprintOnAttack = null

    CPSCounter.registerClick(CPSCounter.MouseButton.LEFT)
}*/
