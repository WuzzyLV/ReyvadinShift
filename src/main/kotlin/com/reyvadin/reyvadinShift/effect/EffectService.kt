package com.reyvadin.reyvadinShift.effect

import com.reyvadin.reyvadinShift.ReyvadinShift
import com.reyvadin.reyvadinShift.model.Pad
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val GROUND_OFFSET = 0.1 // lift the ring off the floor so it isn't clipped inside the block

class EffectService(private val plugin: ReyvadinShift) {

    // Played at both ends of a shift, so onlookers see it at the pad too.
    fun play(pad: Pad, at: Location) {
        val world = at.world ?: return
        val cfg = pad.effects.withDefaults(plugin.defaultEffects)

        cfg.particle?.let { name ->
            val particle = runCatching { Particle.valueOf(name.uppercase()) }.getOrNull()
            if (particle == null) {
                plugin.logger.warning("Pad '${pad.name}': unknown particle '$name'")
            } else {
                circle(
                    world, at, particle,
                    count = (cfg.particleCount ?: 40).coerceAtLeast(1),
                    radius = cfg.radius ?: 1.5,
                )
            }
        }

        cfg.sound?.let { name ->
            world.playSound(at, name, cfg.soundVolume ?: 1.0f, cfg.soundPitch ?: 1.0f)
        }
    }

    // A flat ring of [count] stationary particles around the player's feet.
    private fun circle(world: World, center: Location, particle: Particle, count: Int, radius: Double) {
        val y = center.y + GROUND_OFFSET
        for (i in 0 until count) {
            val angle = 2.0 * PI * i / count
            val px = center.x + cos(angle) * radius
            val pz = center.z + sin(angle) * radius
            world.spawnParticle(particle, px, y, pz, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }
}
