package net.librebounce.utils.extensions

import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i

val BlockPos.state: BlockState?
    get() = mc.world?.getBlockState(this)

val BlockPos.block: Block?
    get() = this.state?.block

val BlockPos.material: Material?
    get() = this.block?.material

val BlockPos.isReplaceable: Boolean
    get() = this.material?.isReplaceable ?: false

val BlockPos.center: Vec3d
    get() = Vec3d(x + 0.5, y + 0.5, z + 0.5)

fun BlockPos.toVec() = Vec3d(this)

/*un BlockPos.canBeClicked(): Boolean {
    val world = mc.world ?: return false
    val state = this.state ?: return false
    val block = state.block ?: return false

    return when {
        this !in world.worldBorder -> false
        !block.canCollide(state, false) -> false
        block.material.isReplaceable -> false
        block.hasBlockEntity(state) -> false
        !isBlockBBValid(this, state, supportSlabs = true, supportPartialBlocks = true) -> false
        world.loadedEntityList.any { it is EntityFallingBlock && it.position == this } -> false
        block is BlockContainer || block is BlockWorkbench -> false
        else -> true
    }
}

val Block.id: Int
    get() = Block.getIdFromBlock(this)

val Int.blockById: Block
    get() = Block.getBlockById(this)

val String.blockByName: Block?
    get() = Block.getBlockFromName(this)*/

fun BlockPos.Mutable.set(vec3i: Vec3i, xOffset: Int = 0, yOffset: Int = 0, zOffset: Int = 0): BlockPos.Mutable =
    set(vec3i.x + xOffset, vec3i.y + yOffset, vec3i.z + zOffset)

fun BlockPos.getAllInBoxMutable(radius: Int): Iterable<BlockPos> {
    return BlockPos.iterateRegionMutable(add(-radius, -radius, -radius), add(radius, radius, radius))
}

fun BlockPos.getAllInBox(radius: Int): Iterable<BlockPos> {
    return BlockPos.iterateRegion(add(-radius, -radius, -radius), add(radius, radius, radius))
}

fun Vec3d.getAllInBoxMutable(radius: Double): Iterable<BlockPos> {
    val from = BlockPos(
        (x - radius).floorInt(),
        (y - radius).floorInt(),
        (z - radius).floorInt()
    )
    val to = BlockPos(
        (x + radius).ceilInt(),
        (y + radius).ceilInt(),
        (z + radius).ceilInt()
    )
    return BlockPos.iterateRegionMutable(from, to)
}

fun Vec3d.getAllInBox(radius: Double): Iterable<BlockPos> {
    val from = BlockPos(
        (x - radius).floorInt(),
        (y - radius).floorInt(),
        (z - radius).floorInt()
    )
    val to = BlockPos(
        (x + radius).ceilInt(),
        (y + radius).ceilInt(),
        (z + radius).ceilInt()
    )
    return BlockPos.iterateRegion(from, to)
}
