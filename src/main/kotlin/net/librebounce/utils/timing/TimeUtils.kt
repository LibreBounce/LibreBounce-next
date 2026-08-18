/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.librebounce.utils.timing

import net.librebounce.utils.extensions.safeDiv
import net.librebounce.utils.kotlin.RandomUtils.nextInt
import kotlin.ranges.IntRange
import kotlin.math.roundToInt

object TimeUtils {
    fun randomDelay(minDelay: Int, maxDelay: Int) = nextInt(minDelay, maxDelay + 1)
}
