package dev.elpu7.elib.quilt.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.elpu7.elib.client.ElibConfigScreen;
import dev.elpu7.elib.client.ElibSettingsManager;
import org.quiltmc.loader.api.QuiltLoader;

public final class ElibModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        ElibSettingsManager.initialize(QuiltLoader.getConfigDir());
        ElibSettingsManager.markModMenuAvailable();
        if (!ElibSettingsManager.get().modMenuIntegration) {
            return ModMenuApi.super.getModConfigScreenFactory();
        }
        ElibSettingsManager.markModMenuRegistered();
        return ElibConfigScreen::new;
    }
}
