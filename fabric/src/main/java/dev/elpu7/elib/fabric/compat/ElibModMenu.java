package dev.elpu7.elib.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.elpu7.elib.client.ElibConfigScreen;
import dev.elpu7.elib.client.ElibSettingsManager;
import net.fabricmc.loader.api.FabricLoader;

public final class ElibModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        ElibSettingsManager.initialize(FabricLoader.getInstance().getConfigDir());
        ElibSettingsManager.markModMenuAvailable();
        if (!ElibSettingsManager.get().modMenuIntegration) {
            return ModMenuApi.super.getModConfigScreenFactory();
        }
        ElibSettingsManager.markModMenuRegistered();
        return ElibConfigScreen::new;
    }
}
