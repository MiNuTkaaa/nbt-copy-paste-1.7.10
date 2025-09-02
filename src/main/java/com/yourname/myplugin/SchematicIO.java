package com.yourname.myplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;

public class SchematicIO {
    public static void saveClipboard(File folder, String name, Clipboard clipboard) throws Exception {
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, name + ".wcp");

        NBTTagCompound root = new NBTTagCompound();
        root.setInt("originX", clipboard.getOrigin().getBlockX());
        root.setInt("originY", clipboard.getOrigin().getBlockY());
        root.setInt("originZ", clipboard.getOrigin().getBlockZ());

        NBTTagList blocksList = new NBTTagList();
        for (BlockData bd : clipboard.getBlocks()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInt("id", bd.getTypeId());
            tag.setByte("data", bd.getData());
            tag.setInt("dx", bd.getOffsetX());
            tag.setInt("dy", bd.getOffsetY());
            tag.setInt("dz", bd.getOffsetZ());
            if (bd.hasNBT()) {
                tag.set("nbt", bd.getNBT());
            }
            blocksList.add(tag);
        }
        root.set("blocks", blocksList);

        FileOutputStream fos = new FileOutputStream(file);
        NBTCompressedStreamTools.a(root, fos);
        fos.close();
    }

    public static java.util.List<BlockData> loadClipboard(File folder, String name) throws Exception {
        File file = new File(folder, name + ".wcp");
        FileInputStream fis = new FileInputStream(file);
        NBTTagCompound root = NBTCompressedStreamTools.a(fis);
        fis.close();

        List<BlockData> list = new ArrayList<BlockData>();
        NBTTagList blocks = (NBTTagList) root.get("blocks");
        for (int i = 0; i < blocks.size(); i++) {
            NBTTagCompound tag = (NBTTagCompound) blocks.get(i);
            int id = tag.getInt("id");
            byte data = tag.getByte("data");
            int dx = tag.getInt("dx");
            int dy = tag.getInt("dy");
            int dz = tag.getInt("dz");
            NBTTagCompound nbt = null;
            if (tag.hasKey("nbt")) nbt = tag.getCompound("nbt");
            list.add(new BlockData(id, data, nbt, dx, dy, dz));
        }

        return list;
    }
}


