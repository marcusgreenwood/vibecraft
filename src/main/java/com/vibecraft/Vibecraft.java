package com.vibecraft;

import com.vibecraft.command.ConfigCommand;
import com.vibecraft.net.LaunchTntPayload;
import com.vibecraft.net.QuitClientPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.TntEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vibecraft implements ModInitializer {
    public static final String MOD_ID = "vibecraft";
    public static final Identifier QUIT_PACKET_ID = Identifier.of(MOD_ID, "quit_client");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Vibecraft mod loaded!");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ConfigCommand.register(dispatcher);
            // Register automated test command in runtime too (used by test runner)
            com.vibecraft.command.TestCommand.register(dispatcher);
        });
        PayloadTypeRegistry.playC2S().register(LaunchTntPayload.ID, LaunchTntPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(QuitClientPayload.ID, QuitClientPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(LaunchTntPayload.ID, (payload, context) -> {
            context.server().execute(() -> handleLaunchTnt(context.player(), payload.power()));
        });
    }

    public static void handleLaunchTnt(ServerPlayerEntity player, float power) {
        if (player == null || player.isSpectator())
            return;
        if (!player.getMainHandStack().isOf(Items.TNT))
            return;

        player.getMainHandStack().decrement(1);

        Vec3d eye = player.getEyePos();
        Vec3d dir = player.getRotationVector();

        double spawnX = eye.x + dir.x * 0.8;
        double spawnY = eye.y + dir.y * 0.8;
        double spawnZ = eye.z + dir.z * 0.8;

        TntEntity tnt = new TntEntity(player.getWorld(), spawnX, spawnY, spawnZ, player);

        Vec3d velocity = new Vec3d(dir.x, Math.max(dir.y + 0.6, 0.35), dir.z).multiply(1.2 * power);
        tnt.setVelocity(velocity);
        tnt.setFuse(200);
        tnt.addCommandTag("mod:launched_tnt");

        player.getWorld().spawnEntity(tnt);
    }
}