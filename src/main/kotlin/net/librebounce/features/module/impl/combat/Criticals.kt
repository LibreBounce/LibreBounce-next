package net.librebounce.features.module.impl.combat

import net.librebounce.event.AttackEvent
import net.librebounce.event.UpdateEvent
import net.librebounce.event.handler
import net.librebounce.features.module.Category
import net.librebounce.features.module.Module
import net.librebounce.utils.attack.CombatUtils
import net.librebounce.utils.simulation.SimulatedPlayer

object Criticals : Module("Criticals", Category.COMBAT) {
    private val mode by choices("Mode", arrayOf("Timer", "Blink"), "Timer")
    private val maxBlinkTime by int("MaxBlinkTime", 250, 0..1000, "ms") { mode == "Blink" }

    private var attackHeld = false
    private var prepareToHoldAttack = false

    val onUpdate = handler<UpdateEvent> {
        val simPlayer = SimulatedPlayer.fromClientPlayer(mc.player.input)

        when (mode) {
            "Blink" -> {
                repeat(maxBlinkTime / 50) {
                    simPlayer.tick()
                }

                if (mc.player.fallDistance > 0 && simPlayer.onGround && CombatUtils.timeUntilHit > maxBlinkTime && !attackHeld) {
                    prepareToHoldAttack = true
                } else if (mc.player.onGround && CombatUtils.timeUntilHit.toInt() == 0 && attackHeld) {
                    releasePackets()
                    attackHeld = false
                    prepareToHoldAttack = false
                }
            }
        }
    }

    val onAttack = handler<AttackEvent> {
        if (prepareToHoldAttack) {
            holdPackets()
            attackHeld = true
        }
    }

    fun holdPackets() {}
    fun releasePackets() {}
}
