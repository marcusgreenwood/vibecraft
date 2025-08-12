# Vibecraft Mod Commands

## Explosion Multiplier Commands

### Simple Command: `/boom`
- **Usage**: `/boom [multiplier]`
- **Range**: 0.1x to 50x
- **Examples**:
  - `/boom 5` - Set explosion multiplier to 5x
  - `/boom 0.5` - Set explosion multiplier to 0.5x (smaller explosions)
  - `/boom` - Show current explosion multiplier

### Advanced Commands: `/explosionmultiplier` (requires OP level 2)

#### Set Fixed Multiplier
- **Usage**: `/explosionmultiplier set <multiplier>`
- **Example**: `/explosionmultiplier set 10`

#### Set Random Range
- **Usage**: `/explosionmultiplier range <min> <max>`
- **Example**: `/explosionmultiplier range 5 15`

#### Reset to Default
- **Usage**: `/explosionmultiplier reset`
- **Default**: Random range 2x-20x

#### Show Current Setting
- **Usage**: `/explosionmultiplier show`

## TNT Launching

### Attack Button (Left Click)
- **Hold TNT in your main hand**
- **Press and hold left click (attack button)** - longer hold = more power
- **Release left click** - TNT launches with power based on hold duration
- **Power range**: 1x to 5x (1 second hold = 2x, 4+ seconds = 5x max)
- **Impact explosion**: Launched TNT explodes on impact with configurable multiplier
- **Feedback**: Shows launch power when TNT is launched

## Testing Commands

### Run All Tests
- **Usage**: `/runalltests`
- **Description**: Runs comprehensive integration tests

### Quit Client
- **Usage**: `/clientquit`
- **Description**: Cleanly shuts down the Minecraft client

## Notes

- The explosion multiplier affects **all explosions** in the game (TNT, Creepers, etc.)
- TNT launched with P key will use the current explosion multiplier setting
- All settings are temporary and reset when the world is reloaded