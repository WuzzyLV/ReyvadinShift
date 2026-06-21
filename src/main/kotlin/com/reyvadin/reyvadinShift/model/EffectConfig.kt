package com.reyvadin.reyvadinShift.model

import org.bukkit.configuration.ConfigurationSection

// Scaffold: parsed and persisted now, consumed by EffectService in a later phase. Defaults off.
data class EffectConfig(
    val particle: String? = null,
    val particleCount: Int = 10,
    val sound: String? = null,
    val soundVolume: Float = 1.0f,
    val soundPitch: Float = 1.0f,
    val actionbar: String? = null,
) {
    fun save(section: ConfigurationSection) {
        section.set("particle", particle)
        section.set("particleCount", particleCount)
        section.set("sound", sound)
        section.set("soundVolume", soundVolume.toDouble())
        section.set("soundPitch", soundPitch.toDouble())
        section.set("actionbar", actionbar)
    }

    companion object {
        val EMPTY = EffectConfig()

        fun load(section: ConfigurationSection?): EffectConfig {
            if (section == null) return EMPTY
            return EffectConfig(
                particle = section.getString("particle"),
                particleCount = section.getInt("particleCount", 10),
                sound = section.getString("sound"),
                soundVolume = section.getDouble("soundVolume", 1.0).toFloat(),
                soundPitch = section.getDouble("soundPitch", 1.0).toFloat(),
                actionbar = section.getString("actionbar"),
            )
        }
    }
}
