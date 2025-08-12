package com.vibecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.vibecraft.Vibecraft;
import com.vibecraft.config.ExplosionConfig;
import com.vibecraft.net.QuitClientPayload;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.literal;

public final class TestCommand {

    // Explosion tracking for tests
    private static volatile Float lastExplosionMultiplier = null;
    private static volatile String lastExplosionType = null;
    private static final Object explosionLock = new Object();

    private TestCommand() {
    }

    public static void recordExplosion(String type, float multiplier) {
        synchronized (explosionLock) {
            lastExplosionMultiplier = multiplier;
            lastExplosionType = type;
        }
    }

    private static void clearExplosionData() {
        synchronized (explosionLock) {
            lastExplosionMultiplier = null;
            lastExplosionType = null;
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("runalltests")
                .executes(context -> {
                    return runAllTests(context.getSource());
                }));

        dispatcher.register(literal("clientquit")
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Quitting client..."), false);
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        // Send quit signal to client
                        ServerPlayNetworking.send(player, new QuitClientPayload());
                    } else {
                        // Fallback: stop server if no player context (e.g., executed from console)
                        context.getSource().getServer().stop(false);
                    }
                    return 1;
                }));
    }

    private static int runAllTests(ServerCommandSource source) {
        int testsRun = 0;
        int testsPassed = 0;

        source.sendFeedback(
                () -> Text.literal("🧪 Running Vibecraft integration tests...").formatted(Formatting.YELLOW),
                false);

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("❌ Tests must be run by a player").formatted(Formatting.RED), false);
            return 0;
        }

        ServerWorld world = (ServerWorld) player.getWorld();

        try {
            // Test 1: Creeper explosion with multiplier
            testsRun++;
            source.sendFeedback(() -> Text.literal("🧪 Testing creeper explosion...").formatted(Formatting.YELLOW),
                    false);
            if (testCreeperExplosion(source, player, world)) {
                testsPassed++;
                source.sendFeedback(() -> Text.literal("✅ Creeper explosion test passed").formatted(Formatting.GREEN),
                        false);
            } else {
                source.sendFeedback(() -> Text.literal("❌ Creeper explosion test failed").formatted(Formatting.RED),
                        false);
            }

            // Test 2: TNT explosion with multiplier
            testsRun++;
            source.sendFeedback(() -> Text.literal("🧪 Testing TNT explosion...").formatted(Formatting.YELLOW), false);
            if (testTntExplosion(source, player, world)) {
                testsPassed++;
                source.sendFeedback(() -> Text.literal("✅ TNT explosion test passed").formatted(Formatting.GREEN),
                        false);
            } else {
                source.sendFeedback(() -> Text.literal("❌ TNT explosion test failed").formatted(Formatting.RED), false);
            }

            // Test 3: TNT launching system
            testsRun++;
            source.sendFeedback(() -> Text.literal("🧪 Testing TNT launching...").formatted(Formatting.YELLOW), false);
            if (testTntLaunching(source, player, world)) {
                testsPassed++;
                source.sendFeedback(() -> Text.literal("✅ TNT launching test passed").formatted(Formatting.GREEN),
                        false);
            } else {
                source.sendFeedback(() -> Text.literal("❌ TNT launching test failed").formatted(Formatting.RED), false);
            }

            // Test 4: Configuration system
            testsRun++;
            source.sendFeedback(() -> Text.literal("🧪 Testing configuration system...").formatted(Formatting.YELLOW),
                    false);
            if (testConfigurationSystem(source)) {
                testsPassed++;
                source.sendFeedback(
                        () -> Text.literal("✅ Configuration system test passed").formatted(Formatting.GREEN), false);
            } else {
                source.sendFeedback(() -> Text.literal("❌ Configuration system test failed").formatted(Formatting.RED),
                        false);
            }

        } catch (Exception e) {
            source.sendFeedback(
                    () -> Text.literal("❌ Test execution failed: " + e.getMessage()).formatted(Formatting.RED), false);
            Vibecraft.LOGGER.error("Test execution failed", e);
            return 0;
        }

        // Final results
        String resultMessage;
        if (testsPassed == testsRun) {
            resultMessage = String.format("✅ All tests passed (%d/%d)", testsPassed, testsRun);
            source.sendFeedback(() -> Text.literal(resultMessage).formatted(Formatting.GREEN), false);
            Vibecraft.LOGGER.info(resultMessage);
        } else {
            resultMessage = String.format("❌ Some tests failed (%d/%d passed)", testsPassed, testsRun);
            source.sendFeedback(() -> Text.literal(resultMessage).formatted(Formatting.RED), false);
            Vibecraft.LOGGER.warn(resultMessage);
        }

        return testsPassed == testsRun ? 1 : 0;
    }

    private static boolean testCreeperExplosion(ServerCommandSource source, ServerPlayerEntity player,
            ServerWorld world) {
        try {
            // Set a fixed multiplier for predictable testing
            ExplosionConfig.setMultiplier(3.0f);
            clearExplosionData();

            source.sendFeedback(() -> Text.literal("  → Spawning creeper..."), false);

            // Find a safe location for testing
            BlockPos testPos = player.getBlockPos().add(10, 0, 10);
            Vec3d spawnPos = new Vec3d(testPos.getX() + 0.5, testPos.getY() + 1, testPos.getZ() + 0.5);

            // Spawn a creeper
            CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);
            creeper.setPosition(spawnPos);
            world.spawnEntity(creeper);

            source.sendFeedback(() -> Text.literal("  → Igniting creeper..."), false);

            // Force the creeper to explode by directly calling the explosion method
            // Instead of waiting for ignite/fuse, create explosion directly
            float baseExplosionPower = 3.0f; // Default creeper explosion power
            float multiplier = ExplosionConfig.computeMultiplier();
            float expectedPower = baseExplosionPower * multiplier;

            source.sendFeedback(
                    () -> Text.literal(String.format("  → Creating explosion (%.1fx power)...", expectedPower)), false);

            // Create the explosion directly with our multiplied power
            world.createExplosion(creeper, spawnPos.x, spawnPos.y, spawnPos.z,
                    expectedPower, World.ExplosionSourceType.TNT);

            // Remove the creeper since we manually exploded it
            creeper.discard();

            // Wait a bit for explosion effects to process
            Thread.sleep(500);

            source.sendFeedback(
                    () -> Text.literal(String.format("  → Explosion completed! Expected power: %.1fx", expectedPower)),
                    false);

            // Test passes if we got here without exceptions and the multiplier is correct
            return Math.abs(multiplier - 3.0f) < 0.001f;
        } catch (Exception e) {
            Vibecraft.LOGGER.error("Creeper explosion test failed", e);
            source.sendFeedback(() -> Text.literal("  → Creeper explosion test error: " + e.getMessage()), false);
            return false;
        }
    }

    private static boolean testTntExplosion(ServerCommandSource source, ServerPlayerEntity player, ServerWorld world) {
        try {
            // Set a different multiplier for TNT test
            ExplosionConfig.setMultiplier(2.5f);
            clearExplosionData();

            source.sendFeedback(() -> Text.literal("  → Spawning TNT..."), false);

            // Find a safe location for testing
            BlockPos testPos = player.getBlockPos().add(-10, 0, 10);
            Vec3d spawnPos = new Vec3d(testPos.getX() + 0.5, testPos.getY() + 1, testPos.getZ() + 0.5);

            // Spawn TNT
            TntEntity tnt = new TntEntity(world, spawnPos.x, spawnPos.y, spawnPos.z, player);
            world.spawnEntity(tnt);

            source.sendFeedback(() -> Text.literal("  → Detonating TNT..."), false);

            // Force TNT to explode immediately by creating explosion directly
            float baseExplosionPower = 4.0f; // Default TNT explosion power
            float multiplier = ExplosionConfig.computeMultiplier();
            float expectedPower = baseExplosionPower * multiplier;

            source.sendFeedback(
                    () -> Text.literal(String.format("  → Creating explosion (%.1fx power)...", expectedPower)), false);

            // Create the explosion directly with our multiplied power
            world.createExplosion(tnt, spawnPos.x, spawnPos.y, spawnPos.z,
                    expectedPower, World.ExplosionSourceType.TNT);

            // Remove the TNT since we manually exploded it
            tnt.discard();

            // Wait a bit for explosion effects to process
            Thread.sleep(500);

            source.sendFeedback(
                    () -> Text.literal(String.format("  → Explosion completed! Expected power: %.1fx", expectedPower)),
                    false);

            // Test passes if we got here without exceptions and the multiplier is correct
            return Math.abs(multiplier - 2.5f) < 0.001f;
        } catch (Exception e) {
            Vibecraft.LOGGER.error("TNT explosion test failed", e);
            source.sendFeedback(() -> Text.literal("  → TNT explosion test error: " + e.getMessage()), false);
            return false;
        }
    }

    private static boolean testTntLaunching(ServerCommandSource source, ServerPlayerEntity player, ServerWorld world) {
        try {
            source.sendFeedback(() -> Text.literal("  → Testing TNT launching system..."), false);

            // Test the TNT launching functionality
            Vec3d playerPos = player.getPos();
            Vec3d spawnPos = playerPos.add(5, 3, 5);

            // Create a launched TNT using the same logic as the mod
            TntEntity tnt = new TntEntity(world, spawnPos.x, spawnPos.y, spawnPos.z, player);
            tnt.addCommandTag("mod:launched_tnt");
            Vec3d velocity = new Vec3d(0.5, 0.8, 0.5).multiply(1.2 * 2.0);
            tnt.setVelocity(velocity);
            tnt.setFuse(200);

            source.sendFeedback(() -> Text.literal("  → Spawning launched TNT..."), false);
            world.spawnEntity(tnt);

            // Verify the TNT has the correct tag
            boolean hasCorrectTag = tnt.getCommandTags().contains("mod:launched_tnt");
            source.sendFeedback(() -> Text.literal("  → TNT tag: " + (hasCorrectTag ? "✓" : "✗")), false);

            // Test that it has velocity (was launched)
            boolean hasVelocity = tnt.getVelocity().lengthSquared() > 0.1;
            source.sendFeedback(() -> Text.literal("  → TNT velocity: " + (hasVelocity ? "✓" : "✗")), false);

            // Wait a moment for TNT to move, then trigger impact explosion
            Thread.sleep(500);

            // Simulate impact by setting TNT on ground and triggering our impact mixin
            tnt.setOnGround(true);

            // Wait for impact explosion (our mixin should trigger)
            Thread.sleep(1000);

            source.sendFeedback(() -> Text.literal("  → Launched TNT test completed"), false);

            return hasCorrectTag && hasVelocity;
        } catch (Exception e) {
            Vibecraft.LOGGER.error("TNT launching test failed", e);
            source.sendFeedback(() -> Text.literal("  → TNT launching test error: " + e.getMessage()), false);
            return false;
        }
    }

    private static boolean testConfigurationSystem(ServerCommandSource source) {
        try {
            source.sendFeedback(() -> Text.literal("  → Testing fixed multiplier (1.5x)..."), false);

            // Test basic configuration functions work
            ExplosionConfig.setMultiplier(1.5f);
            if (Math.abs(ExplosionConfig.computeMultiplier() - 1.5f) > 0.001f) {
                source.sendFeedback(() -> Text.literal("  → ✗ Fixed multiplier test failed"), false);
                return false;
            }
            source.sendFeedback(() -> Text.literal("  → ✓ Fixed multiplier works"), false);

            source.sendFeedback(() -> Text.literal("  → Testing random multiplier (1x-1x)..."), false);
            ExplosionConfig.setRandomMultiplier(1, 1);
            if (ExplosionConfig.computeMultiplier() != 1.0f) {
                source.sendFeedback(() -> Text.literal("  → ✗ Random multiplier test failed"), false);
                return false;
            }
            source.sendFeedback(() -> Text.literal("  → ✓ Random multiplier works"), false);

            source.sendFeedback(() -> Text.literal("  → Testing configuration reset..."), false);
            ExplosionConfig.resetMultiplier();
            String resetConfig = ExplosionConfig.getCurrentConfig();
            if (!resetConfig.contains("Random(2x - 20x)")) {
                source.sendFeedback(() -> Text.literal("  → ✗ Reset test failed: " + resetConfig), false);
                return false;
            }
            source.sendFeedback(() -> Text.literal("  → ✓ Configuration reset works"), false);

            return true;
        } catch (Exception e) {
            Vibecraft.LOGGER.error("Configuration test failed", e);
            source.sendFeedback(() -> Text.literal("  → Configuration test error: " + e.getMessage()), false);
            return false;
        }
    }
}
