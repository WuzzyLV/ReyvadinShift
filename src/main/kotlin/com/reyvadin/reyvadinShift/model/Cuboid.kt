package com.reyvadin.reyvadinShift.model

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

// Stored normalized (min <= max) so the range checks in contains() don't have to sort.
data class Cuboid(
    val worldId: UUID,
    val minX: Int, val minY: Int, val minZ: Int,
    val maxX: Int, val maxY: Int, val maxZ: Int,
) {
    val world: World? get() = Bukkit.getWorld(worldId)

    fun contains(world: UUID, x: Int, y: Int, z: Int): Boolean =
        world == worldId && x in minX..maxX && y in minY..maxY && z in minZ..maxZ

    fun contains(loc: Location): Boolean {
        val w = loc.world ?: return false
        return contains(w.uid, loc.blockX, loc.blockY, loc.blockZ)
    }

    // Footprint center, on top of the highest layer. +1 on x/z lands on the block center, not its corner.
    fun centerTopLocation(yaw: Float, pitch: Float): Location? {
        val w = world ?: return null
        val x = (minX + maxX + 1) / 2.0
        val z = (minZ + maxZ + 1) / 2.0
        val y = (maxY + 1).toDouble()
        return Location(w, x, y, z, yaw, pitch)
    }

    fun save(section: ConfigurationSection) {
        section.set("world", worldId.toString())
        section.set("minX", minX); section.set("minY", minY); section.set("minZ", minZ)
        section.set("maxX", maxX); section.set("maxY", maxY); section.set("maxZ", maxZ)
    }

    companion object {
        fun of(world: UUID, x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int) = Cuboid(
            world,
            min(x1, x2), min(y1, y2), min(z1, z2),
            max(x1, x2), max(y1, y2), max(z1, z2),
        )

        fun between(a: Location, b: Location): Cuboid? {
            val w = a.world ?: return null
            if (b.world?.uid != w.uid) return null
            return of(w.uid, a.blockX, a.blockY, a.blockZ, b.blockX, b.blockY, b.blockZ)
        }

        fun load(section: ConfigurationSection): Cuboid? {
            val worldStr = section.getString("world") ?: return null
            val worldId = runCatching { UUID.fromString(worldStr) }.getOrNull() ?: return null
            return Cuboid(
                worldId,
                section.getInt("minX"), section.getInt("minY"), section.getInt("minZ"),
                section.getInt("maxX"), section.getInt("maxY"), section.getInt("maxZ"),
            )
        }
    }
}
