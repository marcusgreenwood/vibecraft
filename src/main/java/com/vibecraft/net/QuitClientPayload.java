package com.vibecraft.net;

import com.vibecraft.Vibecraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QuitClientPayload() implements CustomPayload {
    public static final CustomPayload.Id<QuitClientPayload> ID = new CustomPayload.Id<>(
            Identifier.of(Vibecraft.MOD_ID, "quit_client"));

    public static final PacketCodec<RegistryByteBuf, QuitClientPayload> CODEC = PacketCodec
            .unit(new QuitClientPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
