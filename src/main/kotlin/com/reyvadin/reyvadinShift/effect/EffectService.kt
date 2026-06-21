package com.reyvadin.reyvadinShift.effect

import com.reyvadin.reyvadinShift.model.Pad
import com.reyvadin.reyvadinShift.model.TriggerType
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

// No-op hook, already wired into the teleport flow so phase 2 is a drop-in.
@Suppress("unused")
class EffectService(private val plugin: Plugin) {
    fun play(player: Player, fromPad: Pad, toPad: Pad, trigger: TriggerType) {
        // TODO(phase 2): play fromPad.effects at trigger spot, toPad.effects at arrival spot.
    }
}
