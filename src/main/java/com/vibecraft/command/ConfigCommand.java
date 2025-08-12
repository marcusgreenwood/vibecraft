package com.vibecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.vibecraft.config.ExplosionConfig;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Simple explosion multiplier command for regular users
        dispatcher.register(literal("boom")
                .then(argument("multiplier", FloatArgumentType.floatArg(0.1f, 50.0f))
                        .executes(context -> {
                            float multiplier = FloatArgumentType.getFloat(context, "multiplier");
                            ExplosionConfig.setMultiplier(multiplier);
                            context.getSource()
                                    .sendFeedback(
                                            () -> Text.literal("💥 Explosion multiplier set to " + multiplier + "x")
                                                    .formatted(Formatting.GOLD),
                                            true);
                            return 1;
                        }))
                .executes(context -> {
                    // Show current multiplier when no argument provided
                    context.getSource().sendFeedback(
                            () -> Text.literal("💥 Current explosion multiplier: " + ExplosionConfig.getCurrentConfig())
                                    .formatted(Formatting.AQUA),
                            false);
                    return 1;
                }));

        // Test explosion command - spawn TNT that explodes with current multiplier
        dispatcher.register(literal("testexplosion")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        context.getSource().sendFeedback(
                                () -> Text.literal("❌ Must be executed by a player").formatted(Formatting.RED), false);
                        return 0;
                    }

                    World world = player.getWorld();
                    Vec3d playerPos = player.getPos();
                    Vec3d spawnPos = playerPos.add(player.getRotationVector().multiply(3.0));

                    float baseExplosionPower = 4.0f; // Default TNT explosion power
                    float multiplier = ExplosionConfig.computeMultiplier();
                    float explosionPower = baseExplosionPower * multiplier;

                    // Create explosion immediately at the target location
                    world.createExplosion(player, spawnPos.x, spawnPos.y, spawnPos.z,
                            explosionPower, World.ExplosionSourceType.TNT);

                    context.getSource().sendFeedback(() -> Text.literal(
                            String.format("💥 Test explosion created with %.1fx power (%.2fx multiplier)",
                                    explosionPower, multiplier))
                            .formatted(Formatting.GOLD), true);

                    return 1;
                }));

        dispatcher.register(literal("explosionmultiplier")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("set")
                        .then(argument("multiplier", FloatArgumentType.floatArg(0))
                                .executes(context -> {
                                    float multiplier = FloatArgumentType.getFloat(context, "multiplier");
                                    ExplosionConfig.setMultiplier(multiplier);
                                    context.getSource()
                                            .sendMessage(Text.literal("Explosion multiplier set to " + multiplier + "x")
                                                    .formatted(Formatting.GREEN));
                                    return 1;
                                })))
                .then(literal("range")
                        .then(argument("min", IntegerArgumentType.integer(0))
                                .then(argument("max", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            int min = IntegerArgumentType.getInteger(context, "min");
                                            int max = IntegerArgumentType.getInteger(context, "max");
                                            ExplosionConfig.setRandomMultiplier(min, max);
                                            context.getSource()
                                                    .sendMessage(Text
                                                            .literal("Explosion multiplier set to random range: " + min
                                                                    + "x - " + max + "x")
                                                            .formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
                .then(literal("reset")
                        .executes(context -> {
                            ExplosionConfig.resetMultiplier();
                            context.getSource().sendMessage(
                                    Text.literal("Explosion multiplier reset to default (2-20x)")
                                            .formatted(Formatting.YELLOW));
                            return 1;
                        }))
                .then(literal("show")
                        .executes(context -> {
                            context.getSource().sendMessage(
                                    Text.literal("Current multiplier: " + ExplosionConfig.getCurrentConfig())
                                            .formatted(Formatting.AQUA));
                            return 1;
                        })));
    }
}