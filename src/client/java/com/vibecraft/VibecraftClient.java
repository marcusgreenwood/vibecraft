package com.vibecraft;

import com.vibecraft.net.LaunchTntPayload;
import com.vibecraft.net.QuitClientPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class VibecraftClient implements ClientModInitializer {

    private long attackButtonPressedTime = 0L;
    private boolean wasAttackPressed = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null)
                return;

            // Use the attack button (left click) for TNT launching
            boolean isAttackPressed = client.options.attackKey.isPressed();

            if (isAttackPressed && client.player.getMainHandStack().isOf(Items.TNT)) {
                if (attackButtonPressedTime == 0L) {
                    attackButtonPressedTime = System.currentTimeMillis();
                }
                wasAttackPressed = true;
            } else if (!isAttackPressed && attackButtonPressedTime > 0L && wasAttackPressed) {
                // Attack button was just released while holding TNT
                long holdDuration = System.currentTimeMillis() - attackButtonPressedTime;
                float power = Math.min(1.0f + (holdDuration / 1000.0f), 5.0f); // Power increases with hold time, max 5x
                ClientPlayNetworking.send(new LaunchTntPayload(power));
                attackButtonPressedTime = 0L;
                wasAttackPressed = false;

                // Show power feedback to player
                client.player.sendMessage(Text.literal(String.format("🚀 TNT launched with %.1fx power!", power)),
                        true);
            } else if (!isAttackPressed) {
                // Reset if attack button not pressed
                attackButtonPressedTime = 0L;
                wasAttackPressed = false;
            }
        });

        // Receive quit signal from server and close the client cleanly
        ClientPlayNetworking.registerGlobalReceiver(QuitClientPayload.ID, (payload, context) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
                mc.scheduleStop();
            }
        });
    }
}