package com.yourname.myplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;

public class WorldCopyPaste extends JavaPlugin {
    
    private static WorldCopyPaste instance;
    private CommandHandler commandHandler;
    private ToolManager toolManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize systems
        commandHandler = new CommandHandler(this);
        toolManager = new ToolManager();
        
        // Register commands
        getCommand("pos1").setExecutor(commandHandler);
        getCommand("pos2").setExecutor(commandHandler);
        getCommand("copy").setExecutor(commandHandler);
        getCommand("paste").setExecutor(commandHandler);
        getCommand("tool").setExecutor(commandHandler);
        getCommand("save").setExecutor(commandHandler);
        getCommand("load").setExecutor(commandHandler);
        getCommand("grid").setExecutor(commandHandler);
        getCommand("clearselection").setExecutor(commandHandler);
        
        getLogger().info("WorldCopyPaste plugin enabled successfully!");
        getLogger().info("Commands: /pos1, /pos2, /copy, /paste, /tool, /save, /load, /grid, /clearselection");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("WorldCopyPaste plugin disabled!");
        instance = null;
    }
    
    public static WorldCopyPaste getInstance() {
        return instance;
    }
    
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
}
