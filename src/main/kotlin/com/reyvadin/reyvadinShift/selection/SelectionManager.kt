package com.reyvadin.reyvadinShift.selection

import com.reyvadin.reyvadinShift.model.Cuboid
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class SelectionManager {
    private val active = HashSet<UUID>()
    private val pos1 = HashMap<UUID, Location>()
    private val pos2 = HashMap<UUID, Location>()

    fun isActive(player: Player): Boolean = active.contains(player.uniqueId)

    // Returns the new state: true = now on, false = now off.
    fun toggle(player: Player): Boolean = if (active.add(player.uniqueId)) {
        true
    } else {
        active.remove(player.uniqueId)
        false
    }

    fun clear(player: Player) {
        active.remove(player.uniqueId)
        pos1.remove(player.uniqueId)
        pos2.remove(player.uniqueId)
    }

    fun setPos1(player: Player, loc: Location) { pos1[player.uniqueId] = loc }
    fun setPos2(player: Player, loc: Location) { pos2[player.uniqueId] = loc }

    fun selectionCuboid(player: Player): Cuboid? {
        val a = pos1[player.uniqueId] ?: return null
        val b = pos2[player.uniqueId] ?: return null
        return Cuboid.between(a, b)
    }
}
