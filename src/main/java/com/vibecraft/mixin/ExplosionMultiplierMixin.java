package com.vibecraft.mixin;

import com.vibecraft.config.ExplosionConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Explosion.class)
public class ExplosionMultiplierMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Vibecraft");

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static float modifyExplosionPower(float power, World world, Entity entity) {
        if (world.isClient) {
            return power;
        }

        float multiplier = ExplosionConfig.computeMultiplier();
        if (multiplier == 1.0f) {
            return power;
        }

        float newPower = power * multiplier;

        String kind = "Explosion"; // Default
        if (entity instanceof TntEntity) {
            kind = "TNT";
        } else if (entity instanceof CreeperEntity) {
            kind = "Creeper";
        }

        // Log and message for explosions
        String message = String.format("💥 %s explosion: %.1fx → %.1fx (%.2fx multiplier)", kind, power, newPower,
                multiplier);
        LOGGER.info(message);
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).getServer().getPlayerManager().broadcast(Text.literal(message), false);
        }

        // Record for testing
        try {
            Class<?> testCommandClass = Class.forName("com.vibecraft.command.TestCommand");
            java.lang.reflect.Method recordMethod = testCommandClass.getMethod("recordExplosion", String.class,
                    float.class);
            recordMethod.invoke(null, kind, multiplier);
        } catch (Exception e) {
            // Ignore reflection errors - tests may not be running
        }

        return newPower;
    }
}