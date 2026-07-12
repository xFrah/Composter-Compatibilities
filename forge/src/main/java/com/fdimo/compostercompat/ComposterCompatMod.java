package com.fdimo.compostercompat;

import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ComposterCompatMod {

    public ComposterCompatMod() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        CommonClass.init();
        
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TagsUpdatedEvent event) -> {
            CommonClass.rebuildComposterMap();
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStartedEvent event) -> {
            CommonClass.populateComposterCache(event.getServer().getRecipeManager(), event.getServer().registryAccess());
        });
    }
}