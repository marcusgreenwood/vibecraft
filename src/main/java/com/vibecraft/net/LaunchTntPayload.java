package com.vibecraft.net;

import com.vibecraft.Vibecraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LaunchTntPayload(float power) implements CustomPayload {
    public static final CustomPayload.Id<LaunchTntPayload> ID = new CustomPayload.Id<>(
            Identifier.of(Vibecraft.MOD_ID, "launch_tnt"));
    public static final PacketCodec<RegistryByteBuf, LaunchTntPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeFloat(value.power),
            buf -> new LaunchTntPayload(buf.readFloat()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}