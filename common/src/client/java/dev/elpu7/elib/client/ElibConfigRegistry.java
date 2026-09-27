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

    /**
     * Registers a config screen without displaying a version number.
     *
     * @param modId the registering mod's unique ID
     * @param name display name in Elib's mod list
     * @param icon optional icon resource, or {@code null}
     * @param screenFactory creates a config screen from its parent screen
     */
    public static synchronized void register(String modId, Component name, Identifier icon,
                                             Function<Screen, Screen> screenFactory) {
        register(modId, name, "", icon, screenFactory);
    }

    /**
     * Registers or replaces a config-screen entry for a mod.
     *
     * @param modId the registering mod's unique ID
     * @param name display name in Elib's mod list
     * @param version version displayed beside the mod name
     * @param icon optional icon resource, or {@code null}
     * @param screenFactory creates a config screen from its parent screen
     */
    public static synchronized void register(String modId, Component name, String version, Identifier icon,
                                             Function<Screen, Screen> screenFactory) {
        Objects.requireNonNull(modId, "modId");
        if (modId.isBlank()) {
            throw new IllegalArgumentException("modId must not be blank");
        }
        ENTRIES.put(modId, new Entry(modId, Objects.requireNonNull(name, "name"),
            Objects.requireNonNull(version, "version"), icon,
            Objects.requireNonNull(screenFactory, "screenFactory")));
    }

    /**
     * Lists the currently registered mod config screens.
     *
     * @return an immutable snapshot of registered config screens
     */
    public static synchronized List<Entry> entries() {
        return List.copyOf(ENTRIES.values());
    }

    /**
     * A mod's entry in Elib's config-screen list.
     *
     * @param modId the mod's unique ID
     * @param name display name
     * @param version displayed mod version
     * @param icon optional icon resource
     * @param screenFactory creates a config screen from its parent
     */
    public record Entry(String modId, Component name, String version, Identifier icon,
                        Function<Screen, Screen> screenFactory) {
        /**
         * Creates the mod's screen with the given parent.
         *
         * @param parent screen to return to when the config screen closes
         * @return the mod's config screen
         */
        public Screen createScreen(Screen parent) {
            return screenFactory.apply(parent);
        }
    }
}
