package com.fdimo.compostercompat.client;

import com.fdimo.compostercompat.CommonClass;
import com.fdimo.compostercompat.network.SyncCompostablesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;

public class ComposterCompatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(SyncCompostablesPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                CommonClass.applySyncedCompostables(payload.entries());
            });
        });
    }
}
