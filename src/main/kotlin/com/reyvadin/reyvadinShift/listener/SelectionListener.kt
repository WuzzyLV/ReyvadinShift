package com.reyvadin.reyvadinShift.listener

import com.reyvadin.reyvadinShift.message.MessageService
import com.reyvadin.reyvadinShift.selection.SelectionManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

class SelectionListener(
    private val selection: SelectionManager,
    private val messages: MessageService,
) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!selection.isActive(player)) return
        if (event.hand != EquipmentSlot.HAND) return // interact fires twice otherwise (main + off hand)
        val block = event.clickedBlock ?: return
        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> {
                event.isCancelled = true
                selection.setPos1(player, block.location)
                messages.send(
                    player, "selection.pos1",
                    messages.ph("x", block.x.toString()),
                    messages.ph("y", block.y.toString()),
                    messages.ph("z", block.z.toString()),
                )
            }

            Action.RIGHT_CLICK_BLOCK -> {
                event.isCancelled = true
                selection.setPos2(player, block.location)
                messages.send(
                    player, "selection.pos2",
                    messages.ph("x", block.x.toString()),
                    messages.ph("y", block.y.toString()),
                    messages.ph("z", block.z.toString()),
                )
            }

            else -> {}
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (selection.isActive(event.player)) event.isCancelled = true
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        selection.clear(event.player)
    }
}
