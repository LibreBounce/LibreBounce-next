/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.utils.movement

import net.librebounce.event.Listenable
import net.minecraft.client.Minecraft
import kotlin.math.sqrt

object BPSUtils : Listenable {

	private var lastPosX: Double = 0.0
	private var lastPosZ: Double = 0.0
	private var lastTimestamp: Long = 0

	fun getBPS(): Double {
		val player = Minecraft.getInstance().player ?: return 0.0

		if (player.ticks < 1 || Minecraft.getInstance().world == null) {
			return 0.0
		}

		val currentTime = System.currentTimeMillis()
		val deltaTime = currentTime - lastTimestamp
		val deltaX = player.x - lastPosX
		val deltaZ = player.z - lastPosZ
		val distance = sqrt(deltaX * deltaX + deltaZ * deltaZ)

		if (deltaTime <= 0 || distance <= 0) {
			return 0.0
		}

		val bps = distance * (1000 / deltaTime.toDouble())

		lastPosX = player.x
		lastPosZ = player.z
		lastTimestamp = currentTime

		return bps
	}
}
