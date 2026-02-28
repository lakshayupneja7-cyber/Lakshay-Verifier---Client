package me.lakshay.verifierclient;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class Payloads {
    private Payloads() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                VerifierClient.VerifyRequestPayload.ID,
                VerifierClient.VerifyRequestPayload.CODEC,
                (payload, context) -> {}
        );
        // NOTE: receiver actual handling is done in VerifierClient; this line just registers codec.
    }
}
