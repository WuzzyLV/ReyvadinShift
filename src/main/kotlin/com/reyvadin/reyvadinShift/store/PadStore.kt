package com.reyvadin.reyvadinShift.store

import com.reyvadin.reyvadinShift.model.Pad
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File

// Backed by pads.yml. Map keys are lowercased names; pad.name keeps the original casing.
class PadStore(private val plugin: Plugin) {
    private val file = File(plugin.dataFolder, "pads.yml")
    private val pads = LinkedHashMap<String, Pad>()

    fun load() {
        pads.clear()
        if (!file.exists()) return
        val yaml = YamlConfiguration.loadConfiguration(file)
        val section = yaml.getConfigurationSection("pads") ?: return
        for (key in section.getKeys(false)) {
            val padSection = section.getConfigurationSection(key) ?: continue
            val pad = Pad.load(key, padSection) ?: continue
            pads[pad.name.lowercase()] = pad
        }
    }

    fun save() {
        val yaml = YamlConfiguration()
        val root = yaml.createSection("pads")
        for (pad in pads.values) {
            pad.save(root.createSection(pad.name))
        }
        plugin.dataFolder.mkdirs()
        yaml.save(file)
    }

    fun get(name: String): Pad? = pads[name.lowercase()]
    fun all(): Collection<Pad> = pads.values
    fun exists(name: String): Boolean = pads.containsKey(name.lowercase())

    fun add(pad: Pad) {
        pads[pad.name.lowercase()] = pad
        save()
    }

    fun remove(name: String): Pad? {
        val pad = pads.remove(name.lowercase()) ?: return null
        // Don't leave partners pointing at a pad that's gone.
        for (other in pads.values) {
            if (other.target.equals(pad.name, ignoreCase = true)) other.target = null
        }
        save()
        return pad
    }

    fun link(a: Pad, b: Pad) {
        a.target = b.name
        b.target = a.name
        save()
    }

    fun unlink(pad: Pad) {
        val partnerName = pad.target
        pad.target = null
        if (partnerName != null) {
            val partner = get(partnerName)
            if (partner != null && partner.target.equals(pad.name, ignoreCase = true)) {
                partner.target = null
            }
        }
        save()
    }

    fun padStoodOn(player: Player): Pad? {
        val loc = player.location
        val w = loc.world ?: return null
        val x = loc.blockX
        val y = loc.blockY
        val z = loc.blockZ
        // Match the feet block and the one below, so a flat pad you stand on top of still counts.
        return pads.values.firstOrNull { pad ->
            pad.cuboid.contains(w.uid, x, y, z) || pad.cuboid.contains(w.uid, x, y - 1, z)
        }
    }
}
