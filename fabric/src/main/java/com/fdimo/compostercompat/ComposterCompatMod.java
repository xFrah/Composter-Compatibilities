package com.fdimo.compostercompat;

import net.fabricmc.api.ModInitializer;

public class ComposterCompatMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        CommonClass.init();
        
        net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            CommonClass.rebuildComposterMap();
        });
        
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            CommonClass.populateComposterCache(server.getRecipeManager(), server.registryAccess());
        });
    }
}
