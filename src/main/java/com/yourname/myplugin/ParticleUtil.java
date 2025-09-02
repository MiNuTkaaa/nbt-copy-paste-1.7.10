package com.yourname.myplugin;

import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ParticleUtil {
    // Sends a redstone-style particle at a position to one player.
    public static void sendRedstoneParticle(Player player, float x, float y, float z,
                                            float offsetX, float offsetY, float offsetZ,
                                            float speed, int count) {
        // 1.7.10 particle name "reddust" (redstone), use color via offsets
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                "reddust", x, y, z, offsetX, offsetY, offsetZ, speed, count
        );
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}


