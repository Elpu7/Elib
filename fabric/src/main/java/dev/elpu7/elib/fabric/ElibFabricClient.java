package dev.elpu7.elib.fabric;

import dev.elpu7.elib.client.ElibSettingsManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class ElibFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ElibSettingsManager.initialize(FabricLoader.getInstance().getConfigDir());
    }
}
