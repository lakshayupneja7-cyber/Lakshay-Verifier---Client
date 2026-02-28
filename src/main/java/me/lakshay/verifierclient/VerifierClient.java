package me.lakshay.verifierclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class VerifierClient implements ClientModInitializer {

    private static final Identifier CHANNEL = new Identifier("lakshay", "verify");

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            String msg = new String(bytes, StandardCharsets.UTF_8);
            if (!msg.equals("REQ")) return;

            String mods = FabricLoader.getInstance().getAllMods().stream()
                    .map(m -> m.getMetadata().getId())
                    .collect(Collectors.joining(","));

            String payload = "MODS|" + mods;

            PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
            out.writeBytes(payload.getBytes(StandardCharsets.UTF_8));
            ClientPlayNetworking.send(CHANNEL, out);
        });
    }
}
