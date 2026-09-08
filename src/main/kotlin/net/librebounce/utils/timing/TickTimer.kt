package net.librebounce.utils.timing

class TickTimer {
	private var tick = 0

	fun update() {
		tick++
	}

	fun reset() {
		tick = 0
	}

	fun get(): Int = tick

	fun hasTimePassed(ticks: Int) = tick >= ticks
}
