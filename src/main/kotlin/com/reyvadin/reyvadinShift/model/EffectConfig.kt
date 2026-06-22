package com.reyvadin.reyvadinShift.model

import org.bukkit.configuration.ConfigurationSection

// Every field null means "inherit from the config default". A pad stores only its overrides;
// withDefaults() fills the gaps before the effect is played.
data class EffectConfig(
    val particle: String? = null,
    val particleCount: Int? = null, // number of points around the circle
    val radius: Double? = null,     // circle radius in blocks
    val sound: String? = null,
    val soundVolume: Float? = null,
    val soundPitch: Float? = null,
) {
    fun withDefaults(default: EffectConfig) = EffectConfig(
        particle = particle ?: default.particle,
        particleCount = particleCount ?: default.particleCount,
        radius = radius ?: default.radius,
        sound = sound ?: default.sound,
        soundVolume = soundVolume ?: default.soundVolume,
        soundPitch = soundPitch ?: default.soundPitch,
    )

    fun save(section: ConfigurationSection) {
        section.set("particle", particle)
        section.set("count", particleCount)
        section.set("radius", radius)
        section.set("sound", sound)
        section.set("volume", soundVolume?.toDouble())
        section.set("pitch", soundPitch?.toDouble())
    }

    companion object {
        val EMPTY = EffectConfig()

        fun load(section: ConfigurationSection?): EffectConfig {
            if (section == null) return EMPTY
            return EffectConfig(
                particle = section.getString("particle"),
                particleCount = if (section.contains("count")) section.getInt("count") else null,
                radius = if (section.contains("radius")) section.getDouble("radius") else null,
                sound = section.getString("sound"),
                soundVolume = if (section.contains("volume")) section.getDouble("volume").toFloat() else null,
                soundPitch = if (section.contains("pitch")) section.getDouble("pitch").toFloat() else null,
            )
        }
    }
}
