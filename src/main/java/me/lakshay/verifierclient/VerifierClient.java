package me.lakshay.verifierclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.stream.Collectors;

public class VerifierClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Receive request from server
        ClientPlayNetworking.registerGlobalReceiver(VerifyRequestPayload.ID, (payload, context) -> {
            // Build mod list
            String mods = FabricLoader.getInstance().getAllMods().stream()
                    .map(m -> m.getMetadata().getId())
                    .collect(Collectors.joining(","));

            // Send response
            ClientPlayNetworking.send(new VerifyModsPayload(mods));
        });
    }

    /**
     * Server -> Client payload: just a request "REQ"
     */
    public record VerifyRequestPayload() implements CustomPayload {
        public static final Id<VerifyRequestPayload> ID =
                new Id<>(Identifier.of("lakshay", "verify_req"));

        public static final PacketCodec<RegistryByteBuf, VerifyRequestPayload> CODEC =
                PacketCodec.ofStatic((buf, value) -> {}, buf -> new VerifyRequestPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * Client -> Server payload: "MODS|<comma-separated-mod-ids>"
     */
    public record VerifyModsPayload(String mods) implements CustomPayload {
        public static final Id<VerifyModsPayload> ID =
                new Id<>(Identifier.of("lakshay", "verify_mods"));

        public static final PacketCodec<RegistryByteBuf, VerifyModsPayload> CODEC =
                PacketCodec.ofStatic(
                        (buf, value) -> buf.writeString(value.mods),
                        buf -> new VerifyModsPayload(buf.readString())
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
