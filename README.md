# WorldCopyPaste Plugin

A comprehensive Minecraft plugin for CraftBukkit 1.7.10 that allows players to copy and paste world regions while preserving all block NBT data, including tile entities like chests, furnaces, signs, and more.

## Features

### Core Functionality
- **Copy/Paste Regions**: Select and copy any cuboid region of the world
- **NBT Preservation**: Maintains all block data including tile entity information
- **Batch Processing**: Handles large regions without server freezing
- **Interactive Selection**: Visual grid overlay for easy region selection

### Supported Block Types
- **Chests & Trapped Chests**: Preserves inventory contents
- **Furnaces**: Maintains items, burn time, and cook time
- **Dispensers**: Preserves inventory contents
- **Hoppers**: Maintains inventory contents
- **Brewing Stands**: Preserves inventory contents
- **Signs**: Maintains all text content
- **Ender Chests**: Ensures interactability
- **All Other Tile Entities**: Generic NBT preservation

### Selection Tools
- **Command Selection**: Use `/pos1` and `/pos2` commands
- **Tool Binding**: Bind any item to right-click selection
- **Visual Grid**: Blue particle outline showing selected region
- **Persistent Selection**: Grid stays visible until operation completes

### Schematic System
- **Save/Load**: Save selections to files for later use
- **Compressed Storage**: Efficient NBT-based file format
- **Cross-Session**: Load saved schematics in different sessions

## Commands

### Basic Selection
- `/pos1` - Set first corner of selection at your location
- `/pos2` - Set second corner of selection at your location
- `/clearselection` - Clear current selection and grid

### Copy/Paste Operations
- `/copy` - Copy the selected region to clipboard
- `/paste` - Paste the clipboard at your location

### Tool System
- `/tool` - Bind the item in your hand for right-click selection
  - First right-click sets pos1
  - Second right-click sets pos2
  - Continues alternating for multiple selections

### Schematic Management
- `/save <name>` - Save current clipboard as a schematic
- `/load <name>` - Load a schematic into your clipboard

### Visual Controls
- `/grid` - Show persistent grid outline
- `/grid stop` - Hide grid outline
- `/grid <ticks>` - Show grid for specified number of ticks

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. The plugin will create a `plugins/WorldCopyPaste/` folder for schematics

## Usage Examples

### Basic Copy/Paste
```
/pos1
/pos2
/copy
/paste
```

### Using Tool Selection
```
/tool
[Right-click block] - Sets pos1
[Right-click block] - Sets pos2
/copy
/paste
```

### Saving and Loading Schematics
```
/pos1
/pos2
/copy
/save myhouse
/load myhouse
/paste
```

### Working with Large Regions
```
/pos1
/pos2
/grid
/copy
[Move to new location]
/paste
```

## Technical Details

### Performance
- **Batch Processing**: Processes 1000 blocks per tick to prevent lag
- **Progress Updates**: Shows paste progress for large regions
- **Memory Efficient**: Uses streaming approach for large selections

### NBT Handling
- **Complete Preservation**: Maintains all tile entity data
- **Bukkit Integration**: Uses both NMS and Bukkit APIs for compatibility
- **Error Recovery**: Robust error handling with fallback mechanisms

### File Format
- **Compressed NBT**: Uses Minecraft's native NBT compression
- **Cross-Platform**: Works across different server setups
- **Version Safe**: Designed for CraftBukkit 1.7.10 compatibility

## Permissions

- `worldcopypaste.pos` - Allows setting selection corners (default: op)
- `worldcopypaste.copy` - Allows copying regions (default: op)
- `worldcopypaste.paste` - Allows pasting regions (default: op)
- `worldcopypaste.*` - All permissions (default: op)

## Configuration

The plugin uses default settings optimized for CraftBukkit 1.7.10. No configuration file is required.

## Troubleshooting

### Common Issues
- **Chests not opening**: Ensure you're using the latest version with the improved chest handling
- **Signs not showing text**: The plugin uses reflection-based tile entity creation for signs
- **Large regions causing lag**: The plugin automatically batches operations to prevent server freezing

### Performance Tips
- Use `/grid stop` to hide the outline when not needed
- Clear selections with `/clearselection` when done
- Save frequently used regions as schematics

## Version Information

- **Target Version**: CraftBukkit 1.7.10
- **API Version**: 1.7.10
- **Java Version**: Java 7+

## Support

This plugin is designed for CraftBukkit 1.7.10 servers. For issues or questions, please ensure you're using the correct server version and Java version.

## License

This plugin is provided as-is for educational and server use purposes.