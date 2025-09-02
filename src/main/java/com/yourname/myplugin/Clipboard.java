package com.yourname.myplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

/**
 * Stores copied region data for a specific player
 */
public class Clipboard {
    private final UUID playerId;
    private final List<BlockData> blocks;
    private final Location origin;
    private final int width;
    private final int height;
    private final int depth;
    
    public Clipboard(UUID playerId, List<BlockData> blocks, Location origin, int width, int height, int depth) {
        this.playerId = playerId;
        this.blocks = new ArrayList<>(blocks);
        this.origin = origin.clone();
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    // Lightweight constructor for loading where dims are unknown/unused
    public Clipboard(UUID playerId, List<BlockData> blocks, Location origin) {
        this.playerId = playerId;
        this.blocks = new ArrayList<>(blocks);
        this.origin = origin.clone();
        this.width = 0;
        this.height = 0;
        this.depth = 0;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public List<BlockData> getBlocks() {
        return new ArrayList<>(blocks);
    }
    
    public Location getOrigin() {
        return origin.clone();
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public int getTotalBlocks() {
        return blocks.size();
    }
    
    public boolean isEmpty() {
        return blocks.isEmpty();
    }
}
