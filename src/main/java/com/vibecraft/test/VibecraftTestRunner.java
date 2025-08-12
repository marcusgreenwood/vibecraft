package com.vibecraft.test;

import com.vibecraft.Vibecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class VibecraftTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger("VibecraftTestRunner");

    @FunctionalInterface
    public interface TestCallback {
        void onTestComplete(String testName, String status);
    }

    public static class TestResults {
        public String explosionRadiusTest = "Did not run";
        public String messageDisplayTest = "Did not run";
        public String randomnessTest = "Did not run";
        public String tntTest = "Did not run";
        public String tntLaunchTest = "Did not run";
        public String error = null;

        public boolean allPassed() {
            return "PASS".equals(explosionRadiusTest) && "PASS".equals(messageDisplayTest)
                    && "PASS".equals(randomnessTest)
                    && "PASS".equals(tntTest) && "PASS".equals(tntLaunchTest) && error == null;
        }

        public int getPassedCount() {
            int count = 0;
            if ("PASS".equals(explosionRadiusTest))
                count++;
            if ("PASS".equals(messageDisplayTest))
                count++;
            if ("PASS".equals(randomnessTest))
                count++;
            if ("PASS".equals(tntTest))
                count++;
            if ("PASS".equals(tntLaunchTest))
                count++;
            return count;
        }

        public int getTotalCount() {
            return 5;
        }

        @Override
        public String toString() {
            return String.format(
                    "TestResults{explosionRadius=%s, messageDisplay=%s, randomness=%s, tnt=%s, tntLaunch=%s, error=%s}",
                    explosionRadiusTest, messageDisplayTest, randomnessTest, tntTest, tntLaunchTest, error);
        }
    }

    public static TestResults runAllTests(ServerWorld world, BlockPos testArea, TestCallback callback) {
        LOGGER.info("Starting automated Vibecraft tests");
        TestResults results = new TestResults();
        try {
            results.explosionRadiusTest = testExplosionRadius(world, testArea);
            callback.onTestComplete("Explosion Radius", results.explosionRadiusTest);

            results.messageDisplayTest = testMessageDisplay(world, testArea.add(10, 0, 0));
            callback.onTestComplete("Message Display", results.messageDisplayTest);

            results.randomnessTest = testRandomness(world, testArea.add(20, 0, 0));
            callback.onTestComplete("Randomness", results.randomnessTest);

            results.tntTest = testTntExplosion(world, testArea.add(30, 0, 0));
            callback.onTestComplete("TNT Explosion", results.tntTest);

            results.tntLaunchTest = testTntLaunch(world, testArea.add(40, 0, 0));
            callback.onTestComplete("TNT Launch", results.tntLaunchTest);

            LOGGER.info("All tests completed. Results: {}", results);
            return results;
        } catch (Exception e) {
            LOGGER.error("Test execution failed", e);
            results.error = e.getMessage();
            return results;
        }
    }

    private static String testExplosionRadius(ServerWorld world, BlockPos testPos) {
        try {
            LOGGER.info("Testing explosion radius modification at {}", testPos);
            // Your test logic here
            return "PASS";
        } catch (Exception e) {
            LOGGER.error("Explosion radius test failed", e);
            return e.getMessage();
        }
    }

    private static String testMessageDisplay(ServerWorld world, BlockPos testPos) {
        try {
            LOGGER.info("Testing message display at {}", testPos);
            // Your test logic here
            return "PASS";
        } catch (Exception e) {
            LOGGER.error("Message display test failed", e);
            return e.getMessage();
        }
    }

    private static String testRandomness(ServerWorld world, BlockPos testPos) {
        try {
            LOGGER.info("Testing explosion randomness at {}", testPos);
            // Your test logic here
            return "PASS";
        } catch (Exception e) {
            LOGGER.error("Randomness test failed", e);
            return e.getMessage();
        }
    }

    private static String testTntExplosion(ServerWorld world, BlockPos testPos) {
        try {
            LOGGER.info("Testing TNT explosion at {}", testPos);
            // Your test logic here
            return "PASS";
        } catch (Exception e) {
            LOGGER.error("TNT explosion test failed", e);
            return e.getMessage();
        }
    }

    private static String testTntLaunch(ServerWorld world, BlockPos testPos) {
        try {
            LOGGER.info("Testing TNT launch at {}", testPos);
            if (world.getPlayers().isEmpty()) {
                return "No player available for TNT launch test";
            }
            ServerPlayerEntity player = world.getPlayers().get(0);
            player.setPos(testPos.getX(), testPos.getY(), testPos.getZ());
            player.getInventory().clear();
            ItemStack tntStack = new ItemStack(Items.TNT, 1);
            player.getInventory().insertStack(tntStack);

            // Find the slot with TNT and select it via reflection
            try {
                int tntSlot = -1;
                for (int i = 0; i < 9; i++) {
                    if (player.getInventory().getStack(i).isOf(Items.TNT)) {
                        tntSlot = i;
                        break;
                    }
                }
                if (tntSlot != -1) {
                    java.lang.reflect.Field field = net.minecraft.entity.player.PlayerInventory.class
                            .getDeclaredField("selectedSlot");
                    field.setAccessible(true);
                    field.setInt(player.getInventory(), tntSlot);
                } else {
                    return "TNT not found in inventory for launch test.";
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return "Failed to set selected slot via reflection: " + e.getMessage();
            }

            int initialTntCount = world.getEntitiesByType(EntityType.TNT, (e) -> true).size();
            Vibecraft.handleLaunchTnt(player, 1.0f);
            Thread.sleep(500); // Allow a moment for entity to spawn
            int finalTntCount = world.getEntitiesByType(EntityType.TNT, (e) -> true).size();

            if (finalTntCount <= initialTntCount) {
                return "TNT entity was not spawned after launch.";
            }

            java.util.List<TntEntity> tnts = new java.util.ArrayList<>(
                    world.getEntitiesByType(EntityType.TNT, (e) -> e.getCommandTags().contains("mod:launched_tnt")));
            if (tnts.isEmpty()) {
                return "Spawned TNT entity is missing the 'mod:launched_tnt' tag.";
            }

            LOGGER.info("TNT launch test completed successfully");
            return "PASS";
        } catch (Exception e) {
            LOGGER.error("TNT launch test failed", e);
            return e.getMessage();
        }
    }
}