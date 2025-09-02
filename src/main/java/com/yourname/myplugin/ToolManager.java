package com.yourname.myplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;

public class ToolManager {
    public static class ToolBinding {
        public final Material material;
        public final short durability;
        public boolean nextIsPos1 = true;

        public ToolBinding(Material material, short durability) {
            this.material = material;
            this.durability = durability;
        }

        public boolean matches(Material material, short durability) {
            return this.material == material && this.durability == durability;
        }
    }

    private final Map<UUID, ToolBinding> playerIdToBinding = new HashMap<UUID, ToolBinding>();

    public void bind(UUID playerId, Material material, short durability) {
        playerIdToBinding.put(playerId, new ToolBinding(material, durability));
    }

    public ToolBinding get(UUID playerId) {
        return playerIdToBinding.get(playerId);
    }

    public void clear(UUID playerId) {
        playerIdToBinding.remove(playerId);
    }
}


