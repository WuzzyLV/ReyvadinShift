package com.reyvadin.reyvadinShift.model

/** How a pad fires its teleport while a player stands on it. */
enum class TriggerType {
    SNEAK,
    JUMP;

    fun lower(): String = name.lowercase()

    companion object {
        fun fromString(value: String?): TriggerType? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
