package com.vibecraft.mixin;

import com.vibecraft.config.ExplosionConfig;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public abstract class LaunchedTntImpactMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Vibecraft");

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        TntEntity self = (TntEntity) (Object) this;
        if (self.getCommandTags().contains("mod:launched_tnt")) {
            if (self.isOnGround() || self.isTouchingWater() || self.getVelocity().lengthSquared() < 0.01) {
                // Create custom explosion with multiplier instead of setting fuse to 0
                World world = self.getWorld();
                if (!world.isClient) {
                    float baseExplosionPower = 4.0f; // Default TNT explosion power
                    float multiplier = ExplosionConfig.computeMultiplier();
                    float explosionPower = baseExplosionPower * multiplier;

                    // Log the explosion
                    String message = String.format("💥 Launched TNT explosion: %.1fx → %.1fx (%.2fx multiplier)",
                            baseExplosionPower, explosionPower, multiplier);
                    LOGGER.info(message);
                    if (world instanceof ServerWorld) {
                        ((ServerWorld) world).getServer().getPlayerManager().broadcast(Text.literal(message), false);
                    }

                    // Create the explosion with the multiplied power
                    world.createExplosion(self, self.getX(), self.getY() + (double) (self.getHeight() / 16.0F),
                            self.getZ(),
                            explosionPower, World.ExplosionSourceType.TNT);

                    // Remove the TNT entity
                    self.discard();
                }
            }
        }
    }
}