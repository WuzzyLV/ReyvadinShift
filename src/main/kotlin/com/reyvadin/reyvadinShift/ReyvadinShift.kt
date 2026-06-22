package com.reyvadin.reyvadinShift

import com.reyvadin.reyvadinShift.command.ShiftCommand
import com.reyvadin.reyvadinShift.effect.EffectService
import com.reyvadin.reyvadinShift.listener.SelectionListener
import com.reyvadin.reyvadinShift.listener.TriggerListener
import com.reyvadin.reyvadinShift.message.MessageService
import com.reyvadin.reyvadinShift.model.EffectConfig
import com.reyvadin.reyvadinShift.selection.SelectionManager
import com.reyvadin.reyvadinShift.store.PadStore
import org.bukkit.plugin.java.JavaPlugin

class ReyvadinShift : JavaPlugin() {

    lateinit var store: PadStore
        private set
    lateinit var messages: MessageService
        private set

    var cooldownMs: Long = 500L
        private set

    // Fallback effects a pad uses for any field it doesn't override itself.
    var defaultEffects: EffectConfig = EffectConfig.EMPTY
        private set

    override fun onEnable() {
        saveDefaultConfig()
        reloadSettings()

        messages = MessageService(this).also { it.load() }
        store = PadStore(this).also { it.load() }
        val selection = SelectionManager()
        val effects = EffectService(this)

        server.pluginManager.registerEvents(SelectionListener(selection, messages), this)
        server.pluginManager.registerEvents(TriggerListener(this, store, effects, messages), this)

        val command = ShiftCommand(this, store, selection, messages)
        getCommand("shift")?.let {
            it.setExecutor(command)
            it.tabCompleter = command
        }
    }

    override fun onDisable() {
        if (::store.isInitialized) store.save()
    }

    fun reloadSettings() {
        saveDefaultConfig() // recreate config.yml if it was deleted
        reloadConfig()
        cooldownMs = config.getLong("cooldown-ms", 500L)
        defaultEffects = EffectConfig.load(config.getConfigurationSection("effects"))
    }
}
