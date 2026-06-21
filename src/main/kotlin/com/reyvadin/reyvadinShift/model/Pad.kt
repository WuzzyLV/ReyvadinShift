package com.reyvadin.reyvadinShift.model

import org.bukkit.configuration.ConfigurationSection

// Triggering on [cuboid] sends the player to the top-center of [target], facing its arrival angles.
data class Pad(
    val name: String,
    var cuboid: Cuboid,
    var trigger: TriggerType,
    var arrivalYaw: Float,
    var arrivalPitch: Float,
    var target: String? = null,
    var effects: EffectConfig = EffectConfig.EMPTY,
) {
    fun save(section: ConfigurationSection) {
        section.set("trigger", trigger.name)
        section.set("arrivalYaw", arrivalYaw.toDouble())
        section.set("arrivalPitch", arrivalPitch.toDouble())
        section.set("target", target)
        cuboid.save(section.createSection("cuboid"))
        effects.save(section.createSection("effects"))
    }

    companion object {
        fun load(name: String, section: ConfigurationSection): Pad? {
            val cuboidSection = section.getConfigurationSection("cuboid") ?: return null
            val cuboid = Cuboid.load(cuboidSection) ?: return null
            val trigger = TriggerType.fromString(section.getString("trigger")) ?: TriggerType.SNEAK
            return Pad(
                name = name,
                cuboid = cuboid,
                trigger = trigger,
                arrivalYaw = section.getDouble("arrivalYaw", 0.0).toFloat(),
                arrivalPitch = section.getDouble("arrivalPitch", 0.0).toFloat(),
                target = section.getString("target"),
                effects = EffectConfig.load(section.getConfigurationSection("effects")),
            )
        }
    }
}
