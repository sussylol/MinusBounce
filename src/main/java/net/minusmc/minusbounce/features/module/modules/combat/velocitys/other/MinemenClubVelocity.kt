package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.MovementUtils

class MinemenClubVelocity : VelocityMode("MinemenClub") {

    private var ticks = 0
    private var lastCancel = false
    private var canCancel = false

    override fun onUpdate() {
        ticks ++
        if (ticks > 23) {
            canCancel = true
        }
        if (ticks in 2..4 && !lastCancel) {
            mc.thePlayer.motionX *= 0.99
            mc.thePlayer.motionZ *= 0.99
        } else if (ticks == 5 && !lastCancel) {
            MovementUtils.strafe()
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) return
            ticks = 0
            if (canCancel) {
                event.cancelEvent()
                lastCancel = true
                canCancel = false
            } else {
                mc.thePlayer.jump()
                lastCancel = false
            }
        }
    }
}