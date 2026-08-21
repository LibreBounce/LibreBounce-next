/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.librebounce.utils.extensions

import net.librebounce.config.FloatRangeValue
import net.librebounce.config.FloatValue
import net.librebounce.config.IntRangeValue
import net.librebounce.config.IntValue
import net.librebounce.utils.rotation.RotationUtils.getFixedAngleDelta
/*import net.librebounce.utils.block.toVec
import net.librebounce.utils.rotation.Rotation
import net.librebounce.utils.rotation.RotationUtils.getFixedAngleDelta*/
import net.minecraft.block.Block
import net.minecraft.client.render.Window
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.entity.Entity
import net.minecraft.util.*
import net.minecraft.util.math.*
import org.joml.Vector2f
//import javax.vecmath.Vector2f
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Provides:
 * ```
 * val (x, y, z) = pos
 */
operator fun Vec3i.component1() = x
operator fun Vec3i.component2() = y
operator fun Vec3i.component3() = z

/**
 * Provides:
 * ```
 * val (x, y, z) = vec
 */
operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

/**
 * Provides:
 * ```
 * val (x, y) = vec
 */
operator fun Vector2f.component1() = x
operator fun Vector2f.component2() = y

/**
 * Provides:
 * ```
 * val (x, y, z) = mc.player
 */
operator fun Entity.component1() = x
operator fun Entity.component2() = y
operator fun Entity.component3() = z

/**
 * Provides:
 * ```
 * val (width, height) = Window(mc)
 */
operator fun Window.component1() = this.scaledWidth
operator fun Window.component2() = this.scaledHeight

/**
 * Provides:
 * `vec + othervec`, `vec - othervec`, `vec * number`, `vec / number`, `-vec`
 * */
operator fun Vec3d.plus(vec:Vec3d): Vec3d = add(vec)
operator fun Vec3d.minus(vec:Vec3d): Vec3d = subtract(vec)
operator fun Vec3d.times(number: Double) = Vec3d(x * number, y * number, z * number)
operator fun Vec3d.div(number: Double) = times(1 / number)
operator fun Vec3d.unaryMinus():Vec3d = times(-1.0)

fun Vec3i.manhattanDistance(another: Vec3i): Int {
    return abs(x - another.x) + abs(y - another.y) + abs(z - another.z)
}

fun Vec3i.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = Vec3i(x, y, z)
fun BlockPos.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPos(x, y, z)
fun BlockPos.Mutable.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPos.Mutable(x, y, z)
fun Vec3d.copy(x: Double = this.x, y: Double = this.y, z: Double = this.z) = Vec3d(x, y, z)

fun BlockPos.immutableCopy() = BlockPos(x, y, z)
fun BlockPos.mutableCopy() = BlockPos.Mutable(x, y, z)

fun Vec3d.moved(direction: Direction, value: Double): Vec3d {
    val vec3i = direction.normal //direction.directionVec

    return Vec3d(
        this.x + value * vec3i.x.toDouble(),
        this.y + value * vec3i.y.toDouble(),
        this.z + value * vec3i.z.toDouble()
    )
}

fun Vec3d.withY(value: Double, useCurrentY: Boolean = false): Vec3d {
    return Vec3d(x, (if (useCurrentY) y else 0.0) + value, z)
}

val Vec3d_ZERO: Vec3d
    get() = Vec3d(0.0, 0.0, 0.0)

val EntityRenderDispatcher.offset
    get() = Vec3d(offsetX, offsetY, offsetZ)

fun Vec3d.toFloatArray() = floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
fun Vec3d.toDoubleArray() = doubleArrayOf(x, y, z)

fun Float.ceilInt() = MathHelper.ceil(this)
fun Float.floorInt() = MathHelper.floor(this)
fun Float.toRadians() = this * 0.017453292f
fun Float.toRadiansD() = toRadians().toDouble()
fun Float.toDegrees() = this * 57.29578f
fun Float.toDegreesD() = toDegrees().toDouble()
fun Float.withGCD() = (this / getFixedAngleDelta()).roundToInt() * getFixedAngleDelta()

/**
 * Prevents possible NaN / (-) Infinity results.
 */
infix fun Int.safeDiv(b: Int) = if (b == 0) 0f else this.toFloat() / b.toFloat()
infix fun Int.safeDivI(b: Int) = if (b == 0) 0 else this / b
infix fun Int.safeDivD(b: Double) = if (b == 0.0) 0.0 else this / b
infix fun Int.ceilDiv(b: Int): Int = ceil(this / b.toDouble()).toInt()

infix fun Float.safeDiv(b: Float) = if (b == 0f) 0f else this / b

fun Double.ceilInt() = MathHelper.ceil(this)
fun Double.floorInt() = MathHelper.floor(this)
fun Double.toRadians() = this * 0.017453292
fun Double.toRadiansF() = toRadians().toFloat()
fun Double.toDegrees() = this * 57.295779513
fun Double.toDegreesF() = toDegrees().toFloat()
fun Double.withGCD() = (this / getFixedAngleDelta()).roundToInt() * getFixedAngleDelta().toDouble()

val Vector2f.abs
    get() = Vector2f(abs(x), abs(y))

/**
 * Provides: (step is 0.1 by default)
 * ```
 *      for (x in 0.1..0.9 step 0.05) {}
 *      for (y in 0.1..0.9) {}
 */
class RangeIterator(
    private val range: ClosedFloatingPointRange<Double>, private val step: Double = 0.1,
) : Iterator<Double> {
    private var value = range.start

    override fun hasNext() = value < range.endInclusive

    override fun next(): Double {
        val returned = value
        value = (value + step).coerceAtMost(range.endInclusive)
        return returned
    }
}

operator fun ClosedFloatingPointRange<Double>.iterator() = RangeIterator(this)
infix fun ClosedFloatingPointRange<Double>.step(step: Double) = RangeIterator(this, step)

fun ClosedFloatingPointRange<Float>.random(): Float {
    require(start.isFinite())
    require(endInclusive.isFinite())
    return (start + (endInclusive - start) * Math.random()).toFloat()
}

/**
 * Conditionally shuffles an `Iterable`
 * @param shuffle determines if the returned `Iterable` is shuffled
 */
fun <T> Iterable<T>.shuffled(shuffle: Boolean) = toMutableList().apply { if (shuffle) shuffle() }

fun Box.lerpWith(x: Double, y: Double, z: Double) =
    Vec3d(minX + (maxX - minX) * x, minY + (maxY - minY) * y, minZ + (maxZ - minZ) * z)

fun Box.lerpWith(point: Vec3d) = lerpWith(point.x, point.y, point.z)
fun Box.lerpWith(value: Double) = lerpWith(value, value, value)
fun Box.moved(other: Vec3d) = moved(other.x, other.y, other.z)
fun Box.moved(other: BlockPos) = moved(other.toVec())

val Box.center
    get() = lerpWith(0.5)

fun Box.getPointSequence(step: Double): Sequence<Vec3d> {
    require(step in 0.0..1.0)

    return sequence {
        var x = 0.0
        while (x <= 1.0) {
            var y = 0.0
            while (y <= 1.0) {
                var z = 0.0
                while (z <= 1.0) {
                    yield(lerpWith(x, y, z))
                    z += step
                }
                y += step
            }
            x += step
        }
    }
}

fun Block.lerpWith(x: Double, y: Double, z: Double) = Vec3d(
    minX + (maxX - minX) * x,
    minY + (maxY - minY) * y,
    minZ + (maxZ - minZ) * z
)

fun Vec3d.lerpWith(other: Vec3d, tickDelta: Double) = Vec3d(
    x + (other.x - x) * tickDelta,
    y + (other.y - y) * tickDelta,
    z + (other.z - z) * tickDelta
)

fun Rotation.lerpWith(other: Rotation, tickDelta: Number) =
    net.librebounce.utils.rotation.Rotation(
        (yaw..other.yaw).lerpWith(tickDelta),
        (pitch..other.pitch).lerpWith(tickDelta)
    )

fun Vec3d.lerpWith(other: Vec3d, tickDelta: Float) = lerpWith(other, tickDelta.toDouble())

fun ClosedFloatingPointRange<Double>.lerpWith(t: Number) = start + (endInclusive - start) * t.toDouble()

fun ClosedFloatingPointRange<Float>.lerpWith(t: Number) = start + (endInclusive - start) * t.toFloat()

fun IntRangeValue.lerpWith(t: Float) = (minimum + (maximum - minimum) * t).roundToInt()

fun FloatRangeValue.lerpWith(t: Float) = minimum + (maximum - minimum) * t

fun IntValue.lerpWith(t: Float) = (minimum + (maximum - minimum) * t).roundToInt()

fun FloatValue.lerpWith(t: Float) = minimum + (maximum - minimum) * t

fun IntRange.lerpWith(t: Float) = start + (endInclusive - start) * t

fun Int.lerpWith(other: Int, t: Float) = this + (other - this) * t

fun decimalPlaces(value: Float): Int {
    var count = 0
    var v = value

    while (v != v.toInt().toFloat()) {
        v *= 10
        count++
    }

    return count
}
