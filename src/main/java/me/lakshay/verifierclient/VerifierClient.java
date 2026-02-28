package me.lakshay.verifierclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class VerifierClient implements ClientModInitializer {

    // This must match your Paper plugin channel EXACTLY:
    // server uses: "lakshay:verify"
    public static final CustomPayload.Id<VerifyPayload> ID =
            new CustomPayload.Id<>(Identifier.of("lakshay", "verify"));

    public static final PacketCodec<RegistryByteBuf, VerifyPayload> CODEC =
            PacketCodec.ofStatic(
                    (buf, payload) -> {
                        byte[] data = payload.data.getBytes(StandardCharsets.UTF_8);
                        buf.writeVarInt(data.length);
                        buf.writeBytes(data);
                    },
                    buf -> {
                        int len = buf.readVarInt();
                        byte[] data = new byte[len];
                        buf.readBytes(data);
                        return new VerifyPayload(new String(data, StandardCharsets.UTF_8));
                    }
            );

    @Override
    public void onInitializeClient() {
        // Register payload type for both directions
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        PayloadTypeRegistry.playC2S().register(ID, CODEC);

        // Listen for server request
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            String msg = payload.data;

            if (!"REQ".equals(msg)) return;

            String mods = FabricLoader.getInstance().getAllMods().stream()
                    .map(m -> m.getMetadata().getId())
                    .collect(Collectors.joining(","));

            // Reply back to Paper server plugin via same channel
            ClientPlayNetworking.send(new VerifyPayload("MODS|" + mods));
        });
    }

    public record VerifyPayload(String data) implements CustomPayload {
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
