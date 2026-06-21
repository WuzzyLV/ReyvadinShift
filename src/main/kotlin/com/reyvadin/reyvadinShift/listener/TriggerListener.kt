package com.reyvadin.reyvadinShift.listener

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.reyvadin.reyvadinShift.ReyvadinShift
import com.reyvadin.reyvadinShift.effect.EffectService
import com.reyvadin.reyvadinShift.message.MessageService
import com.reyvadin.reyvadinShift.model.TriggerType
import com.reyvadin.reyvadinShift.store.PadStore
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import java.util.UUID

class TriggerListener(
    private val plugin: ReyvadinShift,
    private val store: PadStore,
    private val effects: EffectService,
    private val messages: MessageService,
) : Listener {
    private val cooldowns = HashMap<UUID, Long>()

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        if (!event.isSneaking) return
        handle(event.player, TriggerType.SNEAK)
    }

    @EventHandler
    fun onJump(event: PlayerJumpEvent) {
        handle(event.player, TriggerType.JUMP)
    }

    private fun handle(player: Player, trigger: TriggerType) {
        if (!player.hasPermission("reyvadinshift.use")) return
        val pad = store.padStoodOn(player) ?: return
        if (pad.trigger != trigger) return
        val target = pad.target?.let { store.get(it) } ?: return

        val now = System.currentTimeMillis()
        if (now < (cooldowns[player.uniqueId] ?: 0L)) return
        cooldowns[player.uniqueId] = now + plugin.cooldownMs

        val yaw = target.arrivalYaw ?: player.location.yaw
        val pitch = target.arrivalPitch ?: player.location.pitch
        val destination = target.cuboid.centerTopLocation(yaw, pitch)
        if (destination == null) {
            messages.send(player, "error.target-world-missing", messages.ph("pad", target.name))
            return
        }
        player.teleport(destination)
        messages.actionbar(player, "teleport.actionbar", messages.ph("pad", target.name))
        effects.play(player, pad, target, trigger)
    }
}
