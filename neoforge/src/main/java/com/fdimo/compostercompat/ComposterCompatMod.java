package com.fdimo.compostercompat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ComposterCompatMod {

    public ComposterCompatMod(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        CommonClass.init();

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.TagsUpdatedEvent event) -> {
            // Only rebuild when tags are actually loaded from server data.
            // The event also fires during CreateWorldScreen initialization before
            // item components are bound, which causes a NullPointerException.
            // Run on both SERVER_DATA_LOAD and CLIENT_PACKET_RECEIVED.
            // The try-catch in rebuildComposterMap will safely skip the early
            // CreateWorldScreen phase where components are not bound.
            CommonClass.rebuildComposterMap();
        });

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStartedEvent event) -> {
            CommonClass.populateComposterCache(event.getServer().getRecipeManager(), event.getServer().registryAccess());
        });
    }
}