package dev.elpu7.elib.neoforge;

import dev.elpu7.elib.client.ElibInfoScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "elib", dist = Dist.CLIENT)
public final class ElibNeoForge {
    public ElibNeoForge(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (ignored, parent) -> new ElibInfoScreen(parent));
    }
}
