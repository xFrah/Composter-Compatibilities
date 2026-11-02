package com.fdimo.compostercompat;

import com.fdimo.compostercompat.network.SyncCompostablesPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

public class ComposterCompatMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();

        // Register custom payload type for configuration phase
        PayloadTypeRegistry.configurationS2C().register(SyncCompostablesPayload.TYPE, SyncCompostablesPayload.STREAM_CODEC);

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            CommonClass.rebuildComposterMap();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommonClass.populateComposterCache(server.getRecipeManager(), server.registryAccess());
        });

        // Sync to client during the configuration phase (before player enters world and before recipe viewers scan)
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, SyncCompostablesPayload.TYPE)) {
                ServerConfigurationNetworking.send(handler, CommonClass.createSyncPayload());
            }
        });
    }
}
