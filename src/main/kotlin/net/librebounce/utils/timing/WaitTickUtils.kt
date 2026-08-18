package net.librebounce.utils.timing

import net.librebounce.event.GameTickEvent
import net.librebounce.event.Listenable
import net.librebounce.event.handler
import net.librebounce.utils.client.ClientUtils
import net.librebounce.utils.kotlin.removeEach

//@Deprecated("Use TickScheduler instead")
object WaitTickUtils : Listenable {

    private val scheduledActions = ArrayDeque<ScheduledAction>()

    inline fun schedule(ticks: Int, requester: Any? = null, crossinline action: () -> Unit = { }) =
        conditionalSchedule(requester, ticks, false) { action(); null }

    fun conditionalSchedule(
        requester: Any? = null,
        ticks: Int? = null,
        isConditional: Boolean = true,
        action: (tick: Int) -> Boolean?
    ) {
        if (ticks == 0) {
            action(0)

            return
        }

        val time = ticks ?: 0

        scheduledActions += ScheduledAction(requester, time, isConditional, ClientUtils.runTimeTicks + time, action)
    }

    fun hasScheduled(obj: Any) = scheduledActions.any { it.requester == obj }

    val onTick = handler<GameTickEvent>(priority = -1) {
        val currentTick = ClientUtils.runTimeTicks

        scheduledActions.removeEach { action ->
            val elapsed = action.duration - (action.ticks - currentTick)
            val shouldRemove = currentTick >= action.ticks

            if (!action.isConditional) {
                if (shouldRemove) {
                    action.action(elapsed) ?: true
                } else {
                    false
                }
            } else {
                action.action(elapsed) ?: shouldRemove
            }
        }
    }

    private data class ScheduledAction(
        val requester: Any?,
        val duration: Int,
        val isConditional: Boolean,
        val ticks: Int,
        val action: (tick: Int) -> Boolean?
    )

}
