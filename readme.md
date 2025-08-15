# Ironman QoL RuneLite Plugin

A quality of life plugin for Old School RuneScape ironman accounts that reduces visual clutter by hiding items that ironmen cannot interact with.

## Features

### Ground Item Filtering

- Hides ground items dropped by other players or from monsters killed by other players
- Shows only items that ironmen can actually pick up:
  - Items dropped by the player themselves
  - Items from monsters killed solo (without assistance)
  - Static world spawns (naturally respawning items)

### Shop Item Filtering

- Hides items in shops that were sold by other players
- Shows only original shop stock that ironmen can purchase
- Removes inaccessible items from click options to prevent spam clicking

## Configuration Options

- **Hide Inaccessible Ground Items**: Toggle ground item filtering
- **Show Own Drops**: Always display items you dropped
- **Show Static Spawns**: Always display world spawn items
- **Hide Inaccessible Shop Items**: Toggle shop item filtering
- **Show Original Shop Stock**: Always display original shop inventory
- **Remove Click Options**: Remove inaccessible items from interaction menus

## Installation

1. Download the plugin jar file from the releases page
2. Place it in your RuneLite plugins folder
3. Restart RuneLite
4. Enable the plugin in the plugin hub

## Development

### Requirements

- Java 11 or higher
- Gradle

### Building

```bash
gradle build
```

### Project Structure

```
src/main/java/com/ironmanqol/
├── IronmanQolPlugin.java      # Main plugin class
├── IronmanQolConfig.java      # Configuration interface
└── (future implementation files)
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the BSD 2-Clause License - see the LICENSE file for details.

## Disclaimer

This plugin is not affiliated with Jagex or Old School RuneScape. Use at your own risk.
