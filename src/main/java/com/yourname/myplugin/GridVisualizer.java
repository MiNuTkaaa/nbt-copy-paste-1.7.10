package com.yourname.myplugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GridVisualizer {
    private final WorldCopyPaste plugin;
    private final Map<UUID, Integer> playerIdToTaskId = new HashMap<UUID, Integer>();

    public GridVisualizer(WorldCopyPaste plugin) {
        this.plugin = plugin;
    }

    public void showOutline(Player player, Location a, Location b, long ticksVisible) {
        // Backwards-compat: draw particle outline for given ticks
        startPersistentOutline(player, a, b, ticksVisible, true);
    }

    public void startPersistentOutline(final Player player, Location a, Location b, long durationTicks, boolean autoStop) {
        if (a == null || b == null) return;
        if (a.getWorld() != b.getWorld()) return;
        final World world = a.getWorld();
        final int minX = Math.min(a.getBlockX(), b.getBlockX());
        final int maxX = Math.max(a.getBlockX(), b.getBlockX());
        final int minY = Math.min(a.getBlockY(), b.getBlockY());
        final int maxY = Math.max(a.getBlockY(), b.getBlockY());
        final int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        final int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());

        // Cancel previous outline for player
        stop(player);

        // Precompute edge points for cuboid outline
        final Set<Location> edgePoints = new HashSet<Location>();
        
        // Bottom and top faces
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                edgePoints.add(new Location(world, x + 0.5, minY + 0.1, z + 0.5));
                edgePoints.add(new Location(world, x + 0.5, maxY + 0.9, z + 0.5));
            }
        }
        
        // Front and back faces
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                edgePoints.add(new Location(world, x + 0.5, y + 0.5, minZ + 0.1));
                edgePoints.add(new Location(world, x + 0.5, y + 0.5, maxZ + 0.9));
            }
        }
        
        // Left and right faces
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                edgePoints.add(new Location(world, minX + 0.1, y + 0.5, z + 0.5));
                edgePoints.add(new Location(world, maxX + 0.9, y + 0.5, z + 0.5));
            }
        }

        // Schedule repeating particle draw
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Location loc : edgePoints) {
                    // Send blue particles (redstone with blue color)
                    ParticleUtil.sendRedstoneParticle(player, (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0.0f, 0.0f, 1.0f, 1.0f, 0);
                }
            }
        }, 0L, 5L); // Every 5 ticks for smooth outline
        playerIdToTaskId.put(player.getUniqueId(), taskId);

        if (autoStop && durationTicks > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    stop(player);
                }
            }, durationTicks);
        }
    }

    public void stop(Player player) {
        // Cancel any running tasks
        Integer prev = playerIdToTaskId.remove(player.getUniqueId());
        if (prev != null) {
            Bukkit.getScheduler().cancelTask(prev);
        }
    }
}


