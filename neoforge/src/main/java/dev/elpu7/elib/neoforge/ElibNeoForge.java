package dev.elpu7.elib.neoforge;

import dev.elpu7.elib.client.ElibConfigScreen;
import dev.elpu7.elib.client.ElibSettingsManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value = "elib", dist = Dist.CLIENT)
public final class ElibNeoForge {
    public ElibNeoForge(IEventBus modBus, ModContainer container) {
        ElibSettingsManager.initialize(FMLPaths.CONFIGDIR.get());
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (ignored, parent) -> new ElibConfigScreen(parent));
    }
}
