package net.librebounce.utils.block

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class PlaceInfo(val pos: BlockPos, val enumFacing: Direction, var vec3: Vec3d = pos.center) {

    companion object {

        /**
         * Allows you to find a specific place info for your [pos]
         */
        fun get(pos: BlockPos) = Direction.entries.find {
            it != Direction.UP && pos.offset(it).canBeClicked()
        }?.let { side -> PlaceInfo(pos.offset(side), side.opposite) }
    }
}
