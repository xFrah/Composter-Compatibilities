package com.fdimo.compostercompat;

import com.fdimo.compostercompat.network.SyncCompostablesPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class ComposterCompatMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            CommonClass.rebuildComposterMap();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommonClass.populateComposterCache(server.getRecipeManager(), server.registryAccess());
        });

        // Sync to client when player connects to the server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.player, SyncCompostablesPayload.ID)) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                CommonClass.createSyncPayload().write(buf);
                ServerPlayNetworking.send(handler.player, SyncCompostablesPayload.ID, buf);
            }
        });
    }
}
