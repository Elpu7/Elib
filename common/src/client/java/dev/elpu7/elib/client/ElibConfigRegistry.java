package dev.elpu7.elib.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Client-side entry points for mods that want a shortcut in Elib's config screen. */
public final class ElibConfigRegistry {
    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    private ElibConfigRegistry() {
    }

    public static synchronized void register(String modId, Component name, Identifier icon,
                                             Function<Screen, Screen> screenFactory) {
        Objects.requireNonNull(modId, "modId");
        if (modId.isBlank()) {
            throw new IllegalArgumentException("modId must not be blank");
        }
        ENTRIES.put(modId, new Entry(modId, Objects.requireNonNull(name, "name"), icon,
            Objects.requireNonNull(screenFactory, "screenFactory")));
    }

    public static synchronized List<Entry> entries() {
        return List.copyOf(ENTRIES.values());
    }

    public record Entry(String modId, Component name, Identifier icon,
                        Function<Screen, Screen> screenFactory) {
        public Screen createScreen(Screen parent) {
            return screenFactory.apply(parent);
        }
    }
}
