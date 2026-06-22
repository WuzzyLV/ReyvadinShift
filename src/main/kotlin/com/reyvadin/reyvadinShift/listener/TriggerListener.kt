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

        // No fixed angles + matching pad size -> keep the player's spot and facing on the pad.
        val keepStance = target.arrivalYaw == null && target.arrivalPitch == null && target.cuboid.sameSizeAs(pad.cuboid)
        val destination = if (keepStance) {
            target.cuboid.mappedTopLocation(pad.cuboid, player.location)
        } else {
            target.cuboid.centerTopLocation(target.arrivalYaw ?: player.location.yaw, target.arrivalPitch ?: player.location.pitch)
        }
        if (destination == null) {
            messages.send(player, "error.target-world-missing", messages.ph("pad", target.name))
            return
        }
        effects.play(pad, player.location) // departure, while the player is still on the pad
        player.teleport(destination)
        messages.actionbar(player, "teleport.${trigger.lower()}", messages.ph("pad", target.name))
        // Delay the arrival ring: on the teleport tick the client is still at the old position and
        // would drop particle packets aimed at the destination.
        plugin.server.scheduler.runTaskLater(plugin, Runnable { effects.play(target, destination) }, 2L)
    }
}
