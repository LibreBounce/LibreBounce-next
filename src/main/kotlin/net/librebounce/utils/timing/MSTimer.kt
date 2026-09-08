package net.librebounce.utils.timing

class MSTimer {
	private var time = -1L

    fun getTime(): Long {
        return time - System.currentTimeMillis()
    }

	fun hasTimePassed(ms: Number) = System.currentTimeMillis() >= time + ms.toLong()

	fun hasTimeLeft(ms: Number) = ms.toLong() + time - System.currentTimeMillis()

	fun reset() {
		time = System.currentTimeMillis()
	}

	fun zero() = {
		time = -1L
	}
}
