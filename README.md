# Vibecraft - Explosive Minecraft Mod 💥

A Minecraft Fabric mod that enhances explosions with configurable multipliers and adds TNT launching mechanics. Built with comprehensive automated testing to enable reliable AI-assisted development.

## Table of Contents
- [Features](#features)
- [How It Works](#how-it-works)
- [Installation](#installation)
- [Commands](#commands)
- [Architecture](#architecture)
- [Adding New Functionality](#adding-new-functionality)
- [Automated Testing](#automated-testing)
- [AI-Assisted Development](#ai-assisted-development)
- [Development Setup](#development-setup)
- [Building](#building)

## Features

### 🧨 Explosion Multipliers
- **Configurable explosion sizes** from 0.1x to 50x
- **Chat commands** for easy configuration (`/boom`, `/explosionmultiplier`)
- **Random multiplier ranges** for dynamic gameplay
- **Test explosions** to preview effects (`/testexplosion`)

### 🚀 TNT Launching
- **Attack button (left-click) launching** - hold longer for more power
- **Power-based trajectory** - 1x to 5x launch power based on hold duration
- **Impact explosions** - launched TNT explodes on impact with multiplied power
- **Visual feedback** - shows launch power when TNT is fired

### 🧪 Comprehensive Testing
- **Automated integration tests** that spawn entities and create real explosions
- **Detailed test feedback** with step-by-step progress reporting
- **Cross-platform test runner** that validates mod functionality
- **AI development support** - enables reliable iterative development

## How It Works

### Core Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client Side   │    │   Server Side    │    │   Configuration │
│                 │    │                  │    │                 │
│ • Key Binding   │◄──►│ • Command System │◄──►│ • Explosion     │
│ • UI Feedback   │    │ • Entity Logic   │    │   Multipliers   │
│ • Network Send  │    │ • Explosion Gen  │    │ • Power Settings│
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                       ┌──────────────────┐
                       │      Mixins      │
                       │                  │
                       │ • TNT Impact     │
                       │ • Launch System  │
                       │ • Custom Logic   │
                       └──────────────────┘
```

### Key Components

1. **Client-Server Communication**
   - `LaunchTntPayload` - Sends launch power from client to server
   - `QuitClientPayload` - Allows server to cleanly quit client (for testing)

2. **Command System**
   - `ConfigCommand` - Handles explosion multiplier configuration
   - `TestCommand` - Provides automated testing and debugging commands

3. **Mixin System**
   - `LaunchedTntImpactMixin` - Makes launched TNT explode on impact with custom power
   - Intercepts TNT tick events to detect impact conditions

4. **Configuration**
   - `ExplosionConfig` - Manages explosion multiplier settings
   - Supports both fixed and random multiplier ranges

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.8
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Place both Fabric API and Vibecraft JAR in your `mods/` folder
4. Launch Minecraft

## Commands

### Simple Commands
- `/boom [multiplier]` - Set explosion multiplier (0.1x to 50x)
- `/testexplosion` - Create test explosion with current multiplier

### Advanced Commands (OP level 2 required)
- `/explosionmultiplier set <value>` - Set fixed multiplier
- `/explosionmultiplier range <min> <max>` - Set random range
- `/explosionmultiplier show` - Display current settings
- `/explosionmultiplier reset` - Reset to defaults (2x-20x random)

### Testing Commands
- `/runalltests` - Execute comprehensive mod testing
- `/clientquit` - Cleanly quit the client (for automation)

### TNT Launching
- **Hold TNT in main hand**
- **Press and hold left-click** - longer hold = more power (1x-5x)
- **Release to launch** - TNT flies with trajectory and explodes on impact

## Architecture

### Project Structure
```
src/
├── main/java/com/vibecraft/
│   ├── Vibecraft.java              # Main mod class, command registration
│   ├── command/
│   │   ├── ConfigCommand.java      # Explosion multiplier commands
│   │   └── TestCommand.java        # Automated testing commands
│   ├── config/
│   │   └── ExplosionConfig.java    # Multiplier configuration logic
│   ├── mixin/
│   │   └── LaunchedTntImpactMixin.java  # TNT impact explosion logic
│   └── net/
│       ├── LaunchTntPayload.java   # Client→Server TNT launch packet
│       └── QuitClientPayload.java  # Server→Client quit packet
├── client/java/com/vibecraft/
│   └── VibecraftClient.java        # Client-side input handling
├── test/java/com/vibecraft/
│   └── automated/
│       └── VibecraftTestRunner.java # Automated UI testing framework
└── main/resources/
    ├── fabric.mod.json             # Mod metadata
    ├── vibecraft.mixins.json       # Mixin configuration
    └── assets/vibecraft/lang/
        └── en_us.json              # Translations
```

### Key Design Patterns

1. **Command Pattern** - All functionality exposed via commands for testability
2. **Event-Driven Architecture** - Client input → Network packet → Server action
3. **Mixin Injection** - Non-invasive modification of game behavior
4. **Configuration Management** - Centralized explosion settings

## Adding New Functionality

### 1. Adding a New Command

```java
// In ConfigCommand.java or create new command class
dispatcher.register(literal("mycommand")
    .then(argument("parameter", StringArgumentType.string())
        .executes(context -> {
            String param = StringArgumentType.getString(context, "parameter");
            // Your logic here
            context.getSource().sendFeedback(
                () -> Text.literal("Command executed with: " + param), 
                false
            );
            return 1;
        })));
```

### 2. Adding Client-Server Communication

```java
// 1. Create payload record
public record MyPayload(String data) implements CustomPayload {
    public static final Id<MyPayload> ID = new Id<>(Identifier.of("vibecraft", "my_payload"));
    public static final PacketCodec<RegistryByteBuf, MyPayload> CODEC = 
        PacketCodec.tuple(PacketCodecs.STRING, MyPayload::data, MyPayload::new);
    
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}

// 2. Register in Vibecraft.java
PayloadTypeRegistry.playC2S().register(MyPayload.ID, MyPayload.CODEC);

// 3. Handle on server
ServerPlayNetworking.registerGlobalReceiver(MyPayload.ID, (payload, context) -> {
    // Handle payload data
});

// 4. Send from client
ClientPlayNetworking.send(new MyPayload("data"));
```

### 3. Adding a Mixin

```java
@Mixin(TargetClass.class)
public class MyMixin {
    
    @Inject(method = "targetMethod", at = @At("HEAD"))
    private void onTargetMethod(CallbackInfo ci) {
        // Your logic before the method executes
    }
    
    @ModifyVariable(method = "targetMethod", at = @At("HEAD"))
    private float modifyParameter(float original) {
        return original * 2.0f; // Example modification
    }
}
```

### 4. Adding Tests

```java
// In TestCommand.java, add to runAllTests()
// Test X: Your new functionality
testsRun++;
source.sendFeedback(() -> Text.literal("🧪 Testing new feature..."), false);
if (testNewFeature(source, player, world)) {
    testsPassed++;
    source.sendFeedback(() -> Text.literal("✅ New feature test passed"), false);
} else {
    source.sendFeedback(() -> Text.literal("❌ New feature test failed"), false);
}

// Then implement the test method
private static boolean testNewFeature(ServerCommandSource source, 
                                     ServerPlayerEntity player, 
                                     ServerWorld world) {
    try {
        // Test your functionality
        // Return true if test passes, false if it fails
        return true;
    } catch (Exception e) {
        source.sendFeedback(() -> Text.literal("Error: " + e.getMessage()), false);
        return false;
    }
}
```

## Automated Testing

### Why Automated Testing Matters

This mod includes comprehensive automated testing for several critical reasons:

#### 🤖 **AI-Assisted Development Support**
Modern development increasingly relies on AI coding assistants like Cursor, GitHub Copilot, and Claude. These tools can generate code rapidly, but they can't verify that the code actually works in the complex Minecraft environment. Automated tests solve this by:

- **Validating AI-generated code** - Ensuring suggestions actually work as intended
- **Enabling rapid iteration** - AI can make changes and immediately test them
- **Catching integration issues** - AI might generate syntactically correct code that breaks game mechanics
- **Building confidence** - Developers can trust AI suggestions when tests pass

#### 🔄 **Iterative Development**
Minecraft modding involves complex interactions between:
- Client and server environments
- Game engine internals (mixins)
- Multi-threaded systems
- Network protocols
- UI automation

Manual testing of these interactions is time-consuming and error-prone. Automated tests enable:
- **Rapid feedback loops** - Know immediately if changes break functionality
- **Regression prevention** - Ensure new features don't break existing ones
- **Cross-platform validation** - Tests run consistently across different environments

#### 🎯 **"Vibe Coding" Validation**
"Vibe coding" refers to the intuitive, experimental approach many developers use when building creative projects. While this approach fosters innovation, it can lead to:
- Code that "feels right" but doesn't work correctly
- Hidden bugs that only appear in specific game scenarios
- Integration issues between different mod components

Automated tests provide a safety net that allows developers to code by intuition while ensuring functionality remains solid.

### Test Architecture

#### **Integration Testing Approach**
Rather than unit tests, this mod uses integration tests that:

1. **Launch actual Minecraft** with the mod installed
2. **Simulate real player interactions** using Java Robot for UI automation
3. **Execute commands in the game world** to test functionality
4. **Spawn entities and create explosions** to verify game mechanics
5. **Parse game logs** to detect success/failure conditions

#### **Test Runner Components**

```java
VibecraftTestRunner {
    // 1. Environment Setup
    launchMinecraft()           // Start game with mod
    ensureWindowedMode()        // Prevent fullscreen issues
    setupTestEnvironment()      // Creative mode, clear area
    
    // 2. Test Execution
    typeCommand("/runalltests")  // Execute test suite
    monitorLogs()               // Watch for completion
    
    // 3. Validation
    checkResults()              // Parse success/failure
    cleanupAndExit()            // Terminate cleanly
}
```

#### **Test Categories**

1. **Entity Spawning Tests**
   ```java
   testCreeperExplosion() {
       // Spawn creeper → Create explosion → Verify multiplier applied
   }
   ```

2. **Mechanics Tests**
   ```java
   testTntLaunching() {
       // Create TNT → Add tags → Verify velocity → Test impact
   }
   ```

3. **Configuration Tests**
   ```java
   testConfigurationSystem() {
       // Test fixed/random multipliers → Verify persistence
   }
   ```

4. **Integration Tests**
   ```java
   testTntExplosion() {
       // End-to-end: spawn → explode → verify power calculation
   }
   ```

### Running Tests

```bash
# Run automated test suite
./run-test.sh

# Manual test execution (in-game)
/runalltests

# Test specific functionality
/testexplosion
/boom 5.0
```

### Test Output Example
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
  → Launched TNT test completed
✅ TNT launching test passed
✅ All tests passed (4/4)
```

## AI-Assisted Development

### Why This Architecture Enables AI Development

1. **Immediate Feedback** - AI can propose changes and immediately verify they work
2. **Safe Experimentation** - Tests catch breaking changes before they reach players
3. **Comprehensive Coverage** - Tests verify both simple logic and complex game interactions
4. **Documentation Through Tests** - Tests serve as executable documentation of expected behavior

### Best Practices for AI-Assisted Modding

1. **Always Add Tests** - When adding features, implement corresponding tests
2. **Run Tests Frequently** - Execute `./run-test.sh` after significant changes
3. **Test Edge Cases** - Include tests for error conditions and boundary values
4. **Keep Tests Fast** - Optimize test execution to maintain rapid feedback loops

### Example AI Development Workflow

```
1. AI suggests new explosion type feature
2. Developer reviews and accepts
3. AI implements feature code
4. AI adds corresponding test
5. Run ./run-test.sh
6. If tests pass → feature is ready
7. If tests fail → AI debugs and fixes
8. Repeat until tests pass
```

## Development Setup

### Prerequisites
- Java 21+
- Gradle 8.0+
- IntelliJ IDEA or VS Code with Java extensions

### Setup Steps
```bash
# Clone repository
git clone <repository-url>
cd creeper-explosions

# Build project
./gradlew build

# Run in development
./gradlew runClient

# Run tests
./run-test.sh
```

### Development Commands
```bash
# Build mod
./gradlew build

# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer

# Clean build artifacts
./gradlew clean

# Generate IDE project files
./gradlew idea
```

## Building

### Create Release JAR
```bash
./gradlew build
# Output: build/libs/vibecraft-1.0.0.jar
```

### Build Configuration
The mod uses meaningful JAR names instead of random names for each build, as configured in `build.gradle`:

```gradle
jar {
    archiveBaseName = 'vibecraft'
    archiveVersion = project.version
    // Results in: vibecraft-1.0.0.jar
}
```

### Dependencies
- **Minecraft**: 1.21.8
- **Fabric Loader**: 0.16.14+
- **Fabric API**: 0.129.0+
- **Java**: 21+

## Contributing

1. **Add functionality** following the architecture patterns
2. **Write comprehensive tests** for all new features
3. **Run test suite** to ensure no regressions
4. **Update documentation** as needed
5. **Test with AI assistants** to verify AI-compatibility

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Why This Approach Matters

This mod demonstrates how to build Minecraft mods that are **AI-development ready**. By providing comprehensive automated testing, clear architecture, and detailed documentation, we enable:

- **Rapid prototyping** with AI assistance
- **Reliable iteration** on complex game mechanics  
- **Confident deployment** of AI-generated code
- **Educational value** for understanding mod development

The testing framework proves that **"vibe coding" can be both creative and reliable** when supported by proper validation infrastructure.

**Happy modding! 💥🚀**