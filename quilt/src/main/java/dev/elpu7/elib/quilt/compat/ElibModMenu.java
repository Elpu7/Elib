package dev.elpu7.elib.quilt.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.elpu7.elib.client.ElibInfoScreen;

public final class ElibModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ElibInfoScreen::new;
    }
}
