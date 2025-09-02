package com.yourname.myplugin;

import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ToolListener implements Listener {
    private final WorldCopyPaste plugin;
    private final ToolManager toolManager;
    private final CommandHandler commandHandler;

    public ToolListener(WorldCopyPaste plugin, ToolManager toolManager, CommandHandler commandHandler) {
        this.plugin = plugin;
        this.toolManager = toolManager;
        this.commandHandler = commandHandler;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack inHand = player.getItemInHand();
        if (inHand == null) return;
        Material mat = inHand.getType();
        short dur = inHand.getDurability();
        UUID uid = player.getUniqueId();

        ToolManager.ToolBinding binding = toolManager.get(uid);
        if (binding == null) return;
        if (!binding.matches(mat, dur)) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        // Alternate between pos1 and pos2 on each right-click
        if (binding.nextIsPos1) {
            commandHandler.setPos1(player, clicked.getLocation());
            player.sendMessage("§apos1 set via tool.");
        } else {
            commandHandler.setPos2(player, clicked.getLocation());
            player.sendMessage("§apos2 set via tool.");
        }
        binding.nextIsPos1 = !binding.nextIsPos1;
        event.setCancelled(true);
    }
}


