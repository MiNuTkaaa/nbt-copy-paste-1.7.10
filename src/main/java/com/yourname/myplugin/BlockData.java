package com.yourname.myplugin;

import net.minecraft.server.v1_7_R4.NBTTagCompound;

/**
 * Represents a single block with its data and NBT information
 */
public class BlockData {
    private final int typeId;
    private final byte data;
    private final NBTTagCompound nbt;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;
    
    public BlockData(int typeId, byte data, NBTTagCompound nbt, int offsetX, int offsetY, int offsetZ) {
        this.typeId = typeId;
        this.data = data;
        this.nbt = nbt;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }
    
    public int getTypeId() {
        return typeId;
    }
    
    public byte getData() {
        return data;
    }
    
    public NBTTagCompound getNBT() {
        return nbt;
    }
    
    public int getOffsetX() {
        return offsetX;
    }
    
    public int getOffsetY() {
        return offsetY;
    }
    
    public int getOffsetZ() {
        return offsetZ;
    }
    
    public boolean hasNBT() {
        return nbt != null;
    }
}
