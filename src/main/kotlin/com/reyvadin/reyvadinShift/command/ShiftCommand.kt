package com.reyvadin.reyvadinShift.command

import com.reyvadin.reyvadinShift.ReyvadinShift
import com.reyvadin.reyvadinShift.message.MessageService
import com.reyvadin.reyvadinShift.model.Pad
import com.reyvadin.reyvadinShift.model.TriggerType
import com.reyvadin.reyvadinShift.selection.SelectionManager
import com.reyvadin.reyvadinShift.store.PadStore
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ShiftCommand(
    private val plugin: ReyvadinShift,
    private val store: PadStore,
    private val selection: SelectionManager,
    private val messages: MessageService,
) : CommandExecutor, TabCompleter {

    private val subcommands = listOf(
        "select", "create", "link", "unlink", "trigger", "arrival",
        "list", "info", "remove", "tp", "reload", "help",
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("reyvadinshift.admin")) {
            messages.send(sender, "error.no-permission")
            return true
        }
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }
        when (args[0].lowercase()) {
            "select" -> select(sender)
            "create" -> create(sender, args)
            "link" -> link(sender, args)
            "unlink" -> unlink(sender, args)
            "trigger" -> trigger(sender, args)
            "arrival" -> arrival(sender, args)
            "list" -> list(sender)
            "info" -> info(sender, args)
            "remove" -> remove(sender, args)
            "tp" -> tp(sender, args)
            "reload" -> reload(sender)
            else -> sendHelp(sender)
        }
        return true
    }

    private fun select(sender: CommandSender) {
        val player = sender as? Player ?: return playerOnly(sender)
        val on = selection.toggle(player)
        messages.send(player, if (on) "selection.enabled" else "selection.disabled")
    }

    private fun create(sender: CommandSender, args: Array<out String>) {
        val player = sender as? Player ?: return playerOnly(sender)
        if (args.size < 2) return usage(sender, "/shift create <name> [sneak|jump]")
        val name = args[1]
        if (store.exists(name)) return messages.send(sender, "error.name-taken", messages.ph("pad", name))
        val cuboid = selection.selectionCuboid(player) ?: return messages.send(sender, "error.no-selection")
        val trigger = if (args.size >= 3) {
            TriggerType.fromString(args[2]) ?: return messages.send(sender, "error.invalid-trigger", messages.ph("value", args[2]))
        } else {
            TriggerType.SNEAK
        }
        store.add(
            Pad(
                name = name,
                cuboid = cuboid,
                trigger = trigger,
                arrivalYaw = player.location.yaw,
                arrivalPitch = player.location.pitch,
            ),
        )
        messages.send(sender, "pad.created", messages.ph("pad", name), messages.ph("trigger", trigger.lower()))
    }

    private fun link(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) return usage(sender, "/shift link <padA> <padB>")
        val a = store.get(args[1]) ?: return notFound(sender, args[1])
        val b = store.get(args[2]) ?: return notFound(sender, args[2])
        if (a.name.equals(b.name, ignoreCase = true)) return messages.send(sender, "error.same-pad")
        store.link(a, b)
        messages.send(sender, "pad.linked", messages.ph("a", a.name), messages.ph("b", b.name))
    }

    private fun unlink(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) return usage(sender, "/shift unlink <pad>")
        val pad = store.get(args[1]) ?: return notFound(sender, args[1])
        store.unlink(pad)
        messages.send(sender, "pad.unlinked", messages.ph("pad", pad.name))
    }

    private fun trigger(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) return usage(sender, "/shift trigger <name> <sneak|jump>")
        val pad = store.get(args[1]) ?: return notFound(sender, args[1])
        val t = TriggerType.fromString(args[2]) ?: return messages.send(sender, "error.invalid-trigger", messages.ph("value", args[2]))
        pad.trigger = t
        store.save()
        messages.send(sender, "pad.trigger-set", messages.ph("pad", pad.name), messages.ph("trigger", t.lower()))
    }

    private fun arrival(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) return usage(sender, "/shift arrival <name> [yaw] [pitch]")
        val pad = store.get(args[1]) ?: return notFound(sender, args[1])
        val yaw: Float
        val pitch: Float
        if (args.size >= 4) {
            yaw = args[2].toFloatOrNull() ?: return messages.send(sender, "error.invalid-number", messages.ph("value", args[2]))
            pitch = args[3].toFloatOrNull() ?: return messages.send(sender, "error.invalid-number", messages.ph("value", args[3]))
        } else {
            val player = sender as? Player ?: return playerOnly(sender)
            yaw = player.location.yaw
            pitch = player.location.pitch
        }
        pad.arrivalYaw = yaw
        pad.arrivalPitch = pitch
        store.save()
        messages.send(sender, "pad.arrival-set", messages.ph("pad", pad.name), messages.ph("yaw", fmt(yaw)), messages.ph("pitch", fmt(pitch)))
    }

    private fun list(sender: CommandSender) {
        val all = store.all()
        if (all.isEmpty()) return messages.send(sender, "list.empty")
        messages.send(sender, "list.header")
        for (pad in all) {
            sender.sendMessage(
                messages.component(
                    "list.entry",
                    messages.ph("pad", pad.name),
                    messages.ph("trigger", pad.trigger.lower()),
                    messages.ph("target", pad.target ?: "-"),
                ),
            )
        }
    }

    private fun info(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) return usage(sender, "/shift info <name>")
        val pad = store.get(args[1]) ?: return notFound(sender, args[1])
        val c = pad.cuboid
        messages.send(sender, "info.header", messages.ph("pad", pad.name))
        sender.sendMessage(
            messages.component(
                "info.region",
                messages.ph("min", "${c.minX}, ${c.minY}, ${c.minZ}"),
                messages.ph("max", "${c.maxX}, ${c.maxY}, ${c.maxZ}"),
            ),
        )
        sender.sendMessage(messages.component("info.trigger", messages.ph("trigger", pad.trigger.lower())))
        sender.sendMessage(messages.component("info.arrival", messages.ph("yaw", fmt(pad.arrivalYaw)), messages.ph("pitch", fmt(pad.arrivalPitch))))
        sender.sendMessage(messages.component("info.link", messages.ph("target", pad.target ?: "-")))
    }

    private fun remove(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) return usage(sender, "/shift remove <name>")
        val pad = store.remove(args[1]) ?: return notFound(sender, args[1])
        messages.send(sender, "pad.removed", messages.ph("pad", pad.name))
    }

    private fun tp(sender: CommandSender, args: Array<out String>) {
        val player = sender as? Player ?: return playerOnly(sender)
        if (args.size < 2) return usage(sender, "/shift tp <name>")
        val pad = store.get(args[1]) ?: return notFound(sender, args[1])
        val loc = pad.cuboid.centerTopLocation(pad.arrivalYaw, pad.arrivalPitch)
            ?: return messages.send(sender, "error.target-world-missing", messages.ph("pad", pad.name))
        player.teleport(loc)
        messages.send(sender, "pad.tp", messages.ph("pad", pad.name))
    }

    private fun reload(sender: CommandSender) {
        plugin.reloadCooldown()
        messages.load()
        store.load()
        messages.send(sender, "reload.done")
    }

    private fun sendHelp(sender: CommandSender) {
        messages.send(sender, "help.header")
        for (line in messages.helpLines()) sender.sendMessage(line)
    }

    private fun usage(sender: CommandSender, usage: String) =
        messages.send(sender, "error.usage", messages.ph("usage", usage))

    private fun notFound(sender: CommandSender, name: String) =
        messages.send(sender, "error.pad-not-found", messages.ph("pad", name))

    private fun playerOnly(sender: CommandSender) =
        messages.send(sender, "error.player-only")

    private fun fmt(value: Float): String = String.format("%.1f", value)

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (!sender.hasPermission("reyvadinshift.admin")) return emptyList()
        if (args.size == 1) return subcommands.filter { it.startsWith(args[0].lowercase()) }
        return when (args[0].lowercase()) {
            "create" -> if (args.size == 3) triggers(args[2]) else emptyList()
            "trigger" -> when (args.size) {
                2 -> padNames(args[1])
                3 -> triggers(args[2])
                else -> emptyList()
            }
            "link" -> when (args.size) {
                2 -> padNames(args[1])
                3 -> padNames(args[2])
                else -> emptyList()
            }
            "unlink", "info", "remove", "tp", "arrival" -> if (args.size == 2) padNames(args[1]) else emptyList()
            else -> emptyList()
        }
    }

    private fun padNames(prefix: String): List<String> =
        store.all().map { it.name }.filter { it.startsWith(prefix, ignoreCase = true) }

    private fun triggers(prefix: String): List<String> =
        listOf("sneak", "jump").filter { it.startsWith(prefix.lowercase()) }
}
