package com.fdimo.compostercompat.client;

import com.fdimo.compostercompat.CommonClass;
import com.fdimo.compostercompat.network.SyncCompostablesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ComposterCompatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncCompostablesPayload.ID, (client, handler, buf, responseSender) -> {
            SyncCompostablesPayload payload = new SyncCompostablesPayload(buf);
            client.execute(() -> {
                CommonClass.applySyncedCompostables(payload.entries());
            });
        });
    }
}

