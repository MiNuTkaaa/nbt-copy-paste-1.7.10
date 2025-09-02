package com.yourname.myplugin;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.WorldServer;

/**
 * Handles asynchronous pasting of blocks to prevent server freezing
 */
public class PasteTask extends BukkitRunnable {
    private final Player player;
    private final Clipboard clipboard;
    private final Location pasteLocation;
    private final List<BlockData> blocks;
    private final AtomicInteger processedBlocks;
    private final int totalBlocks;
    private final int batchSize;
    
    public PasteTask(Player player, Clipboard clipboard, Location pasteLocation) {
        this.player = player;
        this.clipboard = clipboard;
        this.pasteLocation = pasteLocation;
        this.blocks = clipboard.getBlocks();
        this.totalBlocks = blocks.size();
        this.processedBlocks = new AtomicInteger(0);
        this.batchSize = 1000; // Process 1000 blocks per tick
    }
    
    @Override
    public void run() {
        int startIndex = processedBlocks.get();
        int endIndex = Math.min(startIndex + batchSize, totalBlocks);
        
        if (startIndex >= totalBlocks) {
            // All blocks processed
            player.sendMessage("§aPaste completed! Total blocks: " + totalBlocks);
            this.cancel();
            return;
        }
        
        // Process batch
        for (int i = startIndex; i < endIndex; i++) {
            BlockData blockData = blocks.get(i);
            pasteBlock(blockData);
            processedBlocks.incrementAndGet();
        }
        
        // Show progress
        int progress = (processedBlocks.get() * 100) / totalBlocks;
        player.sendMessage("§ePasting progress: " + progress + "% (" + processedBlocks.get() + "/" + totalBlocks + ")");
        
        // Schedule next batch
        if (processedBlocks.get() < totalBlocks) {
            runTaskLater(WorldCopyPaste.getInstance(), 1L);
        }
    }
    
    private void pasteBlock(final BlockData blockData) {
        final Location blockLocation = pasteLocation.clone().add(
            blockData.getOffsetX(),
            blockData.getOffsetY(),
            blockData.getOffsetZ()
        );
        
        final World world = blockLocation.getWorld();
        final Block block = world.getBlockAt(blockLocation);
        
        // Set block type and data
        block.setTypeIdAndData(blockData.getTypeId(), blockData.getData(), false);
        
        // Handle NBT data if present
        if (blockData.hasNBT()) {
            // Use a different approach - schedule the NBT application directly
            final int x = blockLocation.getBlockX();
            final int y = blockLocation.getBlockY();
            final int z = blockLocation.getBlockZ();
            
            // Schedule NBT application for the next tick
            Bukkit.getScheduler().runTaskLater(WorldCopyPaste.getInstance(), new Runnable() {
                @Override
                public void run() {
                    try {
                        final WorldServer nmsWorld = ((CraftWorld) world).getHandle();
                        
                        // HYBRID APPROACH: Reflection for signs, simple NBT for chests
                        // This should work for both chests and signs
                        
                        try {
                            // Wait for the block to be fully set
                            Bukkit.getScheduler().runTaskLater(WorldCopyPaste.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // For signs, use reflection approach (this was working)
                                        if (blockData.getTypeId() == Material.SIGN_POST.getId() || 
                                            blockData.getTypeId() == Material.WALL_SIGN.getId()) {
                                            

                                            
                                            // Create a new sign tile entity manually
                                            net.minecraft.server.v1_7_R4.TileEntitySign newSign = new net.minecraft.server.v1_7_R4.TileEntitySign();
                                            
                                            // Use reflection to set the position (private method)
                                            try {
                                                java.lang.reflect.Method setPositionMethod = newSign.getClass().getDeclaredMethod("a", int.class, int.class, int.class);
                                                setPositionMethod.setAccessible(true);
                                                setPositionMethod.invoke(newSign, x, y, z);
                                            } catch (Exception e) {
                                                // Silent error handling
                                            }
                                            
                                            // Apply NBT data
                                            newSign.a(blockData.getNBT());
                                            
                                            // Manually add the tile entity to the world
                                            nmsWorld.setTileEntity(x, y, z, newSign);
                                            
                                            // Force world updates
                                            nmsWorld.notify(x, y, z);
                                            

                                            
                                        } else if (blockData.getTypeId() == Material.CHEST.getId() || 
                                                 blockData.getTypeId() == Material.TRAPPED_CHEST.getId()) {
                                            
                                            // PURE BUKKIT APPROACH: Let Bukkit handle everything
                                            
                                            // Use pure Bukkit approach - no NMS tile entity manipulation
                                            try {
                                                // Get the Bukkit chest block state
                                                org.bukkit.block.Chest bukkitChest = (org.bukkit.block.Chest) world.getBlockAt(x, y, z).getState();
                                                org.bukkit.inventory.Inventory chestInventory = bukkitChest.getInventory();
                                                
                                                // Clear the inventory first
                                                chestInventory.clear();
                                                
                                                // Convert NBT items to Bukkit ItemStacks and add them to the chest
                                                if (blockData.getNBT().hasKey("Items")) {
                                                    net.minecraft.server.v1_7_R4.NBTTagList itemsList = blockData.getNBT().getList("Items", 10);
                                                    int itemsAdded = 0;
                                                    
                                                    for (int i = 0; i < itemsList.size(); i++) {
                                                        net.minecraft.server.v1_7_R4.NBTTagCompound itemNBT = itemsList.get(i);
                                                        if (itemNBT != null) {
                                                            // Convert NBT item to Bukkit ItemStack
                                                            org.bukkit.inventory.ItemStack bukkitItem = org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asBukkitCopy(
                                                                net.minecraft.server.v1_7_R4.ItemStack.createStack(itemNBT)
                                                            );
                                                            
                                                            if (bukkitItem != null) {
                                                                int slot = itemNBT.getByte("Slot");
                                                                if (slot >= 0 && slot < chestInventory.getSize()) {
                                                                    chestInventory.setItem(slot, bukkitItem);
                                                                    itemsAdded++;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    

                                                    
                                                    // Force the chest to update
                                                    bukkitChest.update(true, true);
                                                    // Double-chest: also nudge adjacent chest state
                                                    try {
                                                        org.bukkit.block.BlockFace[] faces = new org.bukkit.block.BlockFace[] {
                                                            org.bukkit.block.BlockFace.NORTH,
                                                            org.bukkit.block.BlockFace.SOUTH,
                                                            org.bukkit.block.BlockFace.EAST,
                                                            org.bukkit.block.BlockFace.WEST
                                                        };
                                                        for (org.bukkit.block.BlockFace face : faces) {
                                                            Block adj = world.getBlockAt(x, y, z).getRelative(face);
                                                            if (adj.getType() == Material.CHEST || adj.getType() == Material.TRAPPED_CHEST) {
                                                                org.bukkit.block.Chest adjChest = (org.bukkit.block.Chest) adj.getState();
                                                                adjChest.update(true, true);
                                                            }
                                                        }
                                                    } catch (Throwable ignored) {}
                                                }
                                                

                                                // Delayed verify and reapply once if needed (some servers need another tick)
                                                int itemCount = 0;
                                                for (org.bukkit.inventory.ItemStack item : chestInventory.getContents()) {
                                                    if (item != null) {
                                                        itemCount++;
                                                    }
                                                }
                                                if (itemCount == 0 && blockData.getNBT().hasKey("Items")) {
                                                    Bukkit.getScheduler().runTaskLater(WorldCopyPaste.getInstance(), new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                org.bukkit.block.Chest c2 = (org.bukkit.block.Chest) world.getBlockAt(x, y, z).getState();
                                                                org.bukkit.inventory.Inventory inv2 = c2.getInventory();
                                                                if (inv2.firstEmpty() == 0 || inv2.getContents().length == 0) {
                                                                    net.minecraft.server.v1_7_R4.NBTTagList itemsList2 = blockData.getNBT().getList("Items", 10);
                                                                    for (int i = 0; i < itemsList2.size(); i++) {
                                                                        net.minecraft.server.v1_7_R4.NBTTagCompound itemNBT = itemsList2.get(i);
                                                                        if (itemNBT != null) {
                                                                            org.bukkit.inventory.ItemStack bukkitItem = org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asBukkitCopy(
                                                                                net.minecraft.server.v1_7_R4.ItemStack.createStack(itemNBT)
                                                                            );
                                                                            if (bukkitItem != null) {
                                                                                int slot = itemNBT.getByte("Slot");
                                                                                if (slot >= 0 && slot < inv2.getSize()) {
                                                                                    inv2.setItem(slot, bukkitItem);
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    c2.update(true, true);

                                                                }
                                                            } catch (Throwable ignored) {}
                                                        }
                                                    }, 2L);
                                                }
                                                

                                                
                                            } catch (Exception e) {
                                                // Silent error handling
                                            }
                                            
                                        } else if (blockData.getTypeId() == Material.DISPENSER.getId()) {
                                            // Dispenser: pure Bukkit inventory restore
                                            try {
                                                org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) world.getBlockAt(x, y, z).getState();
                                                org.bukkit.inventory.Inventory inv = dispenser.getInventory();
                                                inv.clear();
                                                if (blockData.getNBT().hasKey("Items")) {
                                                    net.minecraft.server.v1_7_R4.NBTTagList itemsList = blockData.getNBT().getList("Items", 10);
                                                    for (int i = 0; i < itemsList.size(); i++) {
                                                        net.minecraft.server.v1_7_R4.NBTTagCompound itemNBT = itemsList.get(i);
                                                        if (itemNBT != null) {
                                                            org.bukkit.inventory.ItemStack bukkitItem = org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asBukkitCopy(
                                                                net.minecraft.server.v1_7_R4.ItemStack.createStack(itemNBT)
                                                            );
                                                            if (bukkitItem != null) {
                                                                int slot = itemNBT.getByte("Slot");
                                                                if (slot >= 0 && slot < inv.getSize()) {
                                                                    inv.setItem(slot, bukkitItem);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    dispenser.update(true, true);
                                                }

                                            } catch (Exception e) {
                                                // Silent error handling
                                            }
                                            
                                        } else if (blockData.getTypeId() == Material.FURNACE.getId() ||
                                                   blockData.getTypeId() == Material.BURNING_FURNACE.getId()) {
                                            // Furnace: items and times via Bukkit
                                            try {
                                                org.bukkit.block.Furnace furnace = (org.bukkit.block.Furnace) world.getBlockAt(x, y, z).getState();
                                                org.bukkit.inventory.FurnaceInventory inv = furnace.getInventory();
                                                inv.setFuel(null);
                                                inv.setSmelting(null);
                                                inv.setResult(null);
                                                if (blockData.getNBT().hasKey("Items")) {
                                                    net.minecraft.server.v1_7_R4.NBTTagList itemsList = blockData.getNBT().getList("Items", 10);
                                                    for (int i = 0; i < itemsList.size(); i++) {
                                                        net.minecraft.server.v1_7_R4.NBTTagCompound itemNBT = itemsList.get(i);
                                                        if (itemNBT != null) {
                                                            org.bukkit.inventory.ItemStack bukkitItem = org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asBukkitCopy(
                                                                net.minecraft.server.v1_7_R4.ItemStack.createStack(itemNBT)
                                                            );
                                                            if (bukkitItem != null) {
                                                                int slot = itemNBT.getByte("Slot"); // 0=smelt,1=fuel,2=result
                                                                if (slot == 0) inv.setSmelting(bukkitItem);
                                                                else if (slot == 1) inv.setFuel(bukkitItem);
                                                                else if (slot == 2) inv.setResult(bukkitItem);
                                                            }
                                                        }
                                                    }
                                                }
                                                // Restore cook/burn times if present
                                                try {
                                                    if (blockData.getNBT().hasKey("BurnTime")) {
                                                        furnace.setBurnTime((short) blockData.getNBT().getShort("BurnTime"));
                                                    }
                                                    if (blockData.getNBT().hasKey("CookTime")) {
                                                        furnace.setCookTime((short) blockData.getNBT().getShort("CookTime"));
                                                    }
                                                } catch (Throwable t) {
                                                    // Ignore if API differences
                                                }
                                                furnace.update(true, true);

                                            } catch (Exception e) {
                                                // Silent error handling
                                            }
                                            
                                        } else if (blockData.getTypeId() == Material.HOPPER.getId()) {
                                            // Hopper: pure Bukkit inventory restore
                                            try {
                                                org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) world.getBlockAt(x, y, z).getState();
                                                org.bukkit.inventory.Inventory inv = hopper.getInventory();
                                                inv.clear();
                                                if (blockData.getNBT().hasKey("Items")) {
                                                    net.minecraft.server.v1_7_R4.NBTTagList itemsList = blockData.getNBT().getList("Items", 10);
                                                    for (int i = 0; i < itemsList.size(); i++) {
                                                        net.minecraft.server.v1_7_R4.NBTTagCompound itemNBT = itemsList.get(i);
                                                        if (itemNBT != null) {
                                                            org.bukkit.inventory.ItemStack bukkitItem = org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asBukkitCopy(
                                                                net.minecraft.server.v1_7_R4.ItemStack.createStack(itemNBT)
                                                            );
                                                            if (bukkitItem != null) {
                                                                int slot = itemNBT.getByte("Slot");
                                                                if (slot >= 0 && slot < inv.getSize()) {
                                                                    inv.setItem(slot, bukkitItem);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    hopper.update(true, true);
                                                }

                                            } catch (Exception e) {
                                                // Silent error handling
                                            }
                                            
                                        } else if (blockData.getTypeId() == Material.BREWING_STAND.getId()) {
                                            // Brewing stand: restore items only (skip brew time per user)
                                            try {
                                                org.bukkit.block.BrewingStand stand = (org.bukkit.block.BrewingStand) world.getBlockAt(x, y, z).getState();
                                                org.bukkit.inventory.BrewerInventory inv = stand.getInventory();
                                                inv.clear();
                                                if (blockData.getNBT().hasKey("Items")) {
                                                    net.minecraft.server.v1_7_R4.NBTTagList itemsList = blockData.getNBT().getList("Items", 10);
                                                    for (int i = 0; i < itemsList.size(); i++) {
                                                        net.minecraft.server.v1_7_R4.NBTTagCompound itemNBT = itemsList.get(i);
                                                        if (itemNBT != null) {
                                                            org.bukkit.inventory.ItemStack bukkitItem = org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asBukkitCopy(
                                                                net.minecraft.server.v1_7_R4.ItemStack.createStack(itemNBT)
                                                            );
                                                            if (bukkitItem != null) {
                                                                int slot = itemNBT.getByte("Slot");
                                                                if (slot >= 0 && slot < inv.getSize()) {
                                                                    inv.setItem(slot, bukkitItem);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    stand.update(true, true);
                                                }

                                            } catch (Exception e) {
                                                // Silent error handling
                                            }
                                            
                                        } else if (blockData.getTypeId() == Material.ENDER_CHEST.getId()) {
                                            // Ender chest: just ensure interactable (no persistent inventory per block)

                                            
                                        } else {
                                            // For other tile entities, use the original approach
                                            TileEntity tileEntity = nmsWorld.getTileEntity(x, y, z);
                                            if (tileEntity != null) {
                                                tileEntity.a(blockData.getNBT());
                                            }
                                        }
                                    } catch (Exception e) {
                                        // Silent error handling
                                    }
                                }
                            }, 3L); // Wait 3 ticks for block to be fully set
                            
                        } catch (Exception e) {
                            // Silent error handling
                        }
                    } catch (Exception e) {
                        // Silent error handling
                    }
                }
            }, 2L); // Wait 2 ticks to ensure block is fully set
        }
    }
    
    public void start() {
        if (totalBlocks == 0) {
            player.sendMessage("§cNo blocks to paste!");
            return;
        }
        
        player.sendMessage("§aStarting paste operation... Total blocks: " + totalBlocks);
        runTask(WorldCopyPaste.getInstance());
    }
}
