package com.reyvadin.reyvadinShift.message

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class MessageService(private val plugin: Plugin) {
    private val mini = MiniMessage.miniMessage()
    private val file = File(plugin.dataFolder, "messages.yml")
    private var yaml: YamlConfiguration = YamlConfiguration()
    private var prefix: Component = Component.empty()

    fun load() {
        if (!file.exists()) plugin.saveResource("messages.yml", false)
        yaml = YamlConfiguration.loadConfiguration(file)
        prefix = component("prefix")
    }

    // Missing keys fall back to the key itself, so a typo shows up loudly instead of as silence.
    private fun raw(key: String): String = yaml.getString(key) ?: key

    fun component(key: String, vararg placeholders: TagResolver): Component =
        mini.deserialize(raw(key), *placeholders)

    private fun prefixed(key: String, vararg placeholders: TagResolver): Component =
        prefix.append(component(key, *placeholders))

    fun send(audience: Audience, key: String, vararg placeholders: TagResolver) {
        audience.sendMessage(prefixed(key, *placeholders))
    }

    fun actionbar(audience: Audience, key: String, vararg placeholders: TagResolver) {
        audience.sendActionBar(component(key, *placeholders))
    }

    fun helpLines(): List<Component> =
        yaml.getStringList("help.lines").map { mini.deserialize(it) }

    fun ph(name: String, value: String): TagResolver = Placeholder.unparsed(name, value)
}
