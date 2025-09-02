package com.yourname.myplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.WorldServer;

public class CommandHandler implements CommandExecutor {
    private final WorldCopyPaste plugin;
    private final Map<UUID, Location> pos1Map;
    private final Map<UUID, Location> pos2Map;
    private final Map<UUID, Clipboard> clipboardMap;
    private final ToolManager toolManager = new ToolManager();
    private final GridVisualizer gridVisualizer;
    
    public CommandHandler(WorldCopyPaste plugin) {
        this.plugin = plugin;
        this.pos1Map = new HashMap<>();
        this.pos2Map = new HashMap<>();
        this.clipboardMap = new HashMap<>();
        this.gridVisualizer = new GridVisualizer(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "pos1":
                handlePos1(player);
                break;
            case "pos2":
                handlePos2(player);
                break;
            case "copy":
                handleCopy(player);
                break;
            case "paste":
                handlePaste(player);
                break;
            case "tool":
                handleTool(player);
                break;
            case "save":
                handleSave(player, args);
                break;
            case "load":
                handleLoad(player, args);
                break;
            case "grid":
                handleGrid(player, args);
                break;
            case "clearselection":
                handleClearSelection(player);
                break;
            default:
                return false;
        }
        
        return true;
    }
    
    private void handlePos1(Player player) {
        Location location = player.getLocation();
        pos1Map.put(player.getUniqueId(), location);
        player.sendMessage("§aPosition 1 set to: §e" + location.getBlockX() + ", " + 
                         location.getBlockY() + ", " + location.getBlockZ());
        // If both positions exist, auto-show persistent grid
        Location p2 = pos2Map.get(player.getUniqueId());
        if (p2 != null) {
            gridVisualizer.startPersistentOutline(player, location, p2, 0, false);
        }
    }
    
    private void handlePos2(Player player) {
        Location location = player.getLocation();
        pos2Map.put(player.getUniqueId(), location);
        player.sendMessage("§aPosition 2 set to: §e" + location.getBlockX() + ", " + 
                         location.getBlockY() + ", " + location.getBlockZ());
        // If both positions exist, auto-show persistent grid
        Location p1 = pos1Map.get(player.getUniqueId());
        if (p1 != null) {
            gridVisualizer.startPersistentOutline(player, p1, location, 0, false);
        }
    }

    // Exposed for tool listener
    public void setPos1(Player player, Location location) {
        pos1Map.put(player.getUniqueId(), location);
        // If both positions exist, auto-show persistent grid
        Location p2 = pos2Map.get(player.getUniqueId());
        if (p2 != null) {
            gridVisualizer.startPersistentOutline(player, location, p2, 0, false);
        }
    }

    public void setPos2(Player player, Location location) {
        pos2Map.put(player.getUniqueId(), location);
        // If both positions exist, auto-show persistent grid
        Location p1 = pos1Map.get(player.getUniqueId());
        if (p1 != null) {
            gridVisualizer.startPersistentOutline(player, p1, location, 0, false);
        }
    }
    
    private void handleCopy(Player player) {
        UUID playerId = player.getUniqueId();
        Location pos1 = pos1Map.get(playerId);
        Location pos2 = pos2Map.get(playerId);
        
        if (pos1 == null || pos2 == null) {
            player.sendMessage("§cPlease set both positions first with /pos1 and /pos2!");
            return;
        }
        
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage("§cBoth positions must be in the same world!");
            return;
        }
        
        // Calculate region bounds
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        
        player.sendMessage("§aCopying region: §e" + width + "x" + height + "x" + depth + 
                         " (" + (width * height * depth) + " blocks)");
        
        // Copy blocks
        List<BlockData> blocks = new ArrayList<>();
        World world = pos1.getWorld();
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        int offsetX = x - minX;
                        int offsetY = y - minY;
                        int offsetZ = z - minZ;
                        
                        NBTTagCompound nbt = null;
                        try {
                            WorldServer nmsWorld = ((CraftWorld) world).getHandle();
                            TileEntity tileEntity = nmsWorld.getTileEntity(x, y, z);
                            
                            if (tileEntity != null) {
                                nbt = new NBTTagCompound();
                                // Export NBT data - this preserves all tile entity information
                                tileEntity.b(nbt);
                                

                            }
                        } catch (Exception e) {
                            // Silent error handling for NBT copying
                        }
                        
                        BlockData blockData = new BlockData(
                            block.getTypeId(),
                            block.getData(),
                            nbt,
                            offsetX,
                            offsetY,
                            offsetZ
                        );
                        blocks.add(blockData);
                    }
                }
            }
        }
        
        Location origin = new Location(world, minX, minY, minZ);
        Clipboard clipboard = new Clipboard(playerId, blocks, origin, width, height, depth);
        clipboardMap.put(playerId, clipboard);
        
        player.sendMessage("§aCopy completed! §e" + blocks.size() + " blocks copied to clipboard.");
        
        // Hide grid after copy
        gridVisualizer.stop(player);
        
        // Clear positions after copy so tool starts fresh
        pos1Map.remove(playerId);
        pos2Map.remove(playerId);
    }
    
    private void handlePaste(Player player) {
        UUID playerId = player.getUniqueId();
        Clipboard clipboard = clipboardMap.get(playerId);
        
        if (clipboard == null || clipboard.isEmpty()) {
            player.sendMessage("§cNo blocks in clipboard! Use /copy first.");
            return;
        }
        
        Location pasteLocation = player.getLocation();
        PasteTask pasteTask = new PasteTask(player, clipboard, pasteLocation);
        pasteTask.start();
        
        // Hide grid after paste
        gridVisualizer.stop(player);
    }

    private void handleTool(Player player) {
        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            player.sendMessage("§cHold a tool item in your hand.");
            return;
        }
        Material mat = player.getItemInHand().getType();
        short dur = player.getItemInHand().getDurability();
        toolManager.bind(player.getUniqueId(), mat, dur);
        player.sendMessage("§aTool bound. Right-click blocks: next sets pos1, then pos2, and so on.");
        // Register listener lazily
        player.getServer().getPluginManager().registerEvents(new ToolListener(plugin, toolManager, this), plugin);
    }

    private void handleSave(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /save <name>");
            return;
        }
        UUID playerId = player.getUniqueId();
        Clipboard clipboard = clipboardMap.get(playerId);
        if (clipboard == null || clipboard.isEmpty()) {
            player.sendMessage("§cNo clipboard to save. Use /copy first.");
            return;
        }
        try {
            SchematicIO.saveClipboard(new java.io.File(plugin.getDataFolder(), "schematics"), args[0], clipboard);
            player.sendMessage("§aSaved schematic as §e" + args[0]);
        } catch (Exception e) {
            player.sendMessage("§cError saving schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLoad(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /load <name>");
            return;
        }
        try {
            java.util.List<BlockData> loadedBlocks = SchematicIO.loadClipboard(new java.io.File(plugin.getDataFolder(), "schematics"), args[0]);
            // Associate with player and set origin as player's current block for offsets usage
            Clipboard associate = new Clipboard(player.getUniqueId(), loadedBlocks, player.getLocation());
            clipboardMap.put(player.getUniqueId(), associate);
            player.sendMessage("§aLoaded schematic §e" + args[0] + "§a. Use /paste to paste at your location.");
        } catch (Exception e) {
            player.sendMessage("§cError loading schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleGrid(Player player, String[] args) {
        Location p1 = pos1Map.get(player.getUniqueId());
        Location p2 = pos2Map.get(player.getUniqueId());
        if (p1 == null || p2 == null) {
            player.sendMessage("§cSet pos1 and pos2 first.");
            return;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("stop")) {
            gridVisualizer.stop(player);
            player.sendMessage("§aGrid stopped.");
            return;
        }
        long ticks = 0; // 0 = persistent until copy/paste
        if (args.length >= 1) {
            try { ticks = Long.parseLong(args[0]); } catch (Exception ignored) {}
        }
        if (ticks > 0) {
            gridVisualizer.showOutline(player, p1, p2, ticks);
            player.sendMessage("§aGrid shown for " + ticks + " ticks.");
        } else {
            gridVisualizer.startPersistentOutline(player, p1, p2, 0, false);
            player.sendMessage("§aPersistent grid shown. Use /grid stop to hide.");
        }
    }

    private void handleClearSelection(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Clear positions
        pos1Map.remove(playerId);
        pos2Map.remove(playerId);
        
        // Stop grid visualization
        gridVisualizer.stop(player);
        
        player.sendMessage("§aSelection cleared! Positions and grid removed.");
    }
}
