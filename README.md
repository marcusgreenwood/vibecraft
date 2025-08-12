# Vibecraft - AI-Powered Minecraft Mod Development Framework 🤖💥

**A comprehensive framework for AI-assisted "vibe coding" of Minecraft mods with automated RPA testing validation.**

This project demonstrates how to build Minecraft mods using AI coding assistants (like Cursor, Claude, GitHub Copilot) with confidence, backed by automated testing that verifies functionality actually works in the game environment.

## Table of Contents
- [Framework Overview](#framework-overview)
- [AI Vibe Coding Support](#ai-vibe-coding-support)
- [RPA-Powered Test Framework](#rpa-powered-test-framework)
- [Architecture](#architecture)
- [Adding New Functionality](#adding-new-functionality)
- [Example Implementation](#example-implementation)
- [Development Setup](#development-setup)
- [Testing System](#testing-system)
- [Building](#building)
- [Mod Features](#mod-features)

## Framework Overview

### 🎯 **Primary Goals**

1. **AI Vibe Coding Framework** - Enable rapid, intuitive development with AI coding assistants
2. **RPA Testing Validation** - Automated Robot-powered testing that verifies code actually works in Minecraft
3. **Example Implementation** - Demonstrate the framework with working mod features

### 🤖 **Why This Matters**

Modern AI coding assistants can generate Minecraft mod code rapidly, but they cannot verify that the code works in the complex game environment. This framework bridges that gap by providing:

- **Immediate validation** of AI-generated code
- **Automated regression testing** for iterative development  
- **Real game environment testing** beyond unit tests
- **Confidence in AI suggestions** through comprehensive verification

## AI Vibe Coding Support

### 🧠 **What is "Vibe Coding"?**

"Vibe coding" refers to the intuitive, experimental approach where developers:
- Code by intuition and feel rather than rigid specifications
- Rapidly prototype and iterate on ideas
- Use AI assistants to generate code based on natural language descriptions
- Focus on creative exploration over upfront planning

### 🔄 **The AI Development Loop**

```
1. 💭 Describe desired functionality to AI
2. 🤖 AI generates mod code 
3. 🧪 Automated tests validate in real Minecraft
4. ✅ Tests pass → Feature ready
5. ❌ Tests fail → AI debugs and fixes
6. 🔁 Repeat until working
```

### 🎯 **Framework Benefits for AI Development**

- **Rapid Feedback** - Know immediately if AI code works
- **Safe Experimentation** - Test crazy ideas without breaking anything
- **Iterative Refinement** - AI can continuously improve based on test results
- **Complex Validation** - Tests verify game mechanics, not just syntax
- **Documentation Through Tests** - Tests serve as executable specifications

## RPA-Powered Test Framework

### 🤖 **Robot Process Automation (RPA) Testing**

This framework uses Java's `Robot` class to perform **actual UI automation** of Minecraft:

```java
// Real UI automation - not mocked!
Robot robot = new Robot();
pressKey(robot, KeyEvent.VK_T);           // Open chat
typeString(robot, "/runalltests");        // Type command  
pressKey(robot, KeyEvent.VK_ENTER);       // Execute
monitorGameLogs();                        // Validate results
```

### 🎮 **Why RPA Instead of Unit Tests?**

Minecraft mods involve complex interactions that unit tests can't capture:

- **Game Engine Integration** - Mixins, entity spawning, world interaction
- **Client-Server Communication** - Network packets, synchronization
- **UI Interactions** - Key bindings, chat commands, player input
- **Real-Time Systems** - Entity ticking, explosion mechanics, physics

### 🔧 **Test Architecture**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Test Runner   │    │   Minecraft      │    │   Validation    │
│                 │    │                  │    │                 │
│ • Launch Game   │───▶│ • Load Mod       │───▶│ • Parse Logs    │
│ • Type Commands │    │ • Execute Tests  │    │ • Check Results │
│ • Monitor Logs  │    │ • Real Entities  │    │ • Report Status │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### ⚡ **Automated Test Flow**

1. **Environment Setup**
   ```java
   launchMinecraft()           // Start game with mod
   waitForWorldLoad()          // Ensure game is ready
   setupTestEnvironment()      // Creative mode, clear area
   ```

2. **Test Execution**
   ```java
   typeCommand("/runalltests")  // Execute comprehensive tests
   monitorGameLogs()           // Watch for success/failure
   parseTestResults()          // Extract validation data
   ```

3. **Real Game Validation**
   ```java
   spawnCreeper()              // Create actual entities
   triggerExplosion()          // Test real game mechanics
   measureExplosionRadius()    // Verify functionality works
   ```

### 🧪 **Test Categories**

- **Entity Integration** - Spawn mobs, verify behavior
- **Game Mechanics** - Test explosions, physics, interactions
- **Command System** - Validate chat commands work correctly
- **Client-Server** - Test networking and synchronization
- **Configuration** - Verify settings persistence and application

## Adding New Functionality

Follow the [.cursorrules](.cursorrules) for the complete workflow. Key steps:

1. **Write tests first** in `TestCommand.java`
2. **Implement the feature** 
3. **Run tests** with `./run-test.sh`
4. **Fix until passing**
5. **Update documentation**

### Quick Examples

**New Command:**
```java
dispatcher.register(literal("mycommand")
    .executes(context -> {
        // Your logic here
        return 1;
    }));
```

**New Test:**
```java
private static boolean testMyFeature(ServerCommandSource source, ServerPlayerEntity player, ServerWorld world) {
    try {
        // Test your functionality
        return true; // or false if validation fails
    } catch (Exception e) {
        return false;
    }
}
```

## Testing System

### 🎯 **Core Testing Philosophy**

This framework prioritizes **integration testing over unit testing** because Minecraft mods require validation of:

- **Real game interactions** - Not just isolated functions
- **Complex state management** - Entities, world state, networking
- **Performance under load** - Actual game conditions
- **Cross-system integration** - Client ↔ Server ↔ Game Engine

### ⚡ **Test Execution**

```bash
# Run full automated test suite
./run-test.sh

# Manual test execution (in-game)
/runalltests

# Test specific functionality  
/testexplosion
/boom 5.0
```

### 📊 **Test Output Example**
```
🧪 Running Vibecraft integration tests...
🧪 Testing creeper explosion...
  → Spawning creeper...
  → Igniting creeper...
  → Creating explosion (9.0x power)...
  → Explosion completed! Expected power: 9.0x
✅ Creeper explosion test passed
🧪 Testing TNT launching...
  → Testing TNT launching system...
  → Spawning launched TNT...
  → TNT tag: ✓
  → TNT velocity: ✓
✅ TNT launching test passed
✅ All tests passed (4/4)
```

### ⚡ **Quick Test Execution**

```bash
./run-test.sh              # Full automated test suite  
/runalltests               # Manual in-game testing
```

Results show step-by-step validation:
```
🧪 Running Vibecraft integration tests...
✅ Creeper explosion test passed
✅ TNT launching test passed
✅ All tests passed (4/4)
```

## Development Setup

```bash
# Prerequisites: Java 21+, Gradle 8.0+
git clone <repository-url>
cd creeper-explosions

# Build and test
./gradlew build
./run-test.sh

# Development 
./gradlew runClient    # Launch Minecraft with mod
```

## Building

```bash
./gradlew build
# Output: build/libs/vibecraft-1.0.0.jar
```

## Contributing

Follow the [.cursorrules](.cursorrules) workflow: write tests first, implement features, run tests, update docs.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Mod Features

### 🧨 Explosion Multipliers (Example Implementation)
- **Configurable explosion sizes** from 0.1x to 50x
- **Chat commands** for easy configuration (`/boom`, `/explosionmultiplier`)
- **Random multiplier ranges** for dynamic gameplay
- **Test explosions** to preview effects (`/testexplosion`)

### 🚀 TNT Launching (Example Implementation)
- **Attack button (left-click) launching** - hold longer for more power
- **Power-based trajectory** - 1x to 5x launch power based on hold duration
- **Impact explosions** - launched TNT explodes on impact with multiplied power
- **Visual feedback** - shows launch power when TNT is fired

### 📋 Commands Reference

#### Simple Commands
- `/boom [multiplier]` - Set explosion multiplier (0.1x to 50x)
- `/testexplosion` - Create test explosion with current multiplier

#### Advanced Commands (OP level 2 required)
- `/explosionmultiplier set <value>` - Set fixed multiplier
- `/explosionmultiplier range <min> <max>` - Set random range
- `/explosionmultiplier show` - Display current settings
- `/explosionmultiplier reset` - Reset to defaults (2x-20x random)

#### Testing Commands
- `/runalltests` - Execute comprehensive mod testing
- `/clientquit` - Cleanly quit the client (for automation)

#### TNT Launching Usage
- **Hold TNT in main hand**
- **Press and hold left-click** - longer hold = more power (1x-5x)
- **Release to launch** - TNT flies with trajectory and explodes on impact

### 🎮 Installation (For Playing)

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.8
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Place both Fabric API and Vibecraft JAR in your `mods/` folder
4. Launch Minecraft

---

## Why This Framework Matters

This project demonstrates how to build Minecraft mods that are **AI-development ready**. By providing comprehensive automated testing, clear architecture, and detailed documentation, we enable:

- **Rapid prototyping** with AI assistance
- **Reliable iteration** on complex game mechanics  
- **Confident deployment** of AI-generated code
- **Educational value** for understanding mod development

The RPA testing framework proves that **"vibe coding" can be both creative and reliable** when supported by proper validation infrastructure.

**Build the future of AI-assisted modding! 🤖💥🚀**