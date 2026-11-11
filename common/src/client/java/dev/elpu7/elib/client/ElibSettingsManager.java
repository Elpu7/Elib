package dev.elpu7.elib.client;

import dev.elpu7.elib.config.JsonConfigStore;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ElibSettingsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("elib");

    private static JsonConfigStore<ElibSettings> store;
    private static boolean modMenuAvailable;
    private static boolean modMenuRegistered;

    private ElibSettingsManager() {
    }

    public static void initialize(Path configDirectory) {
        if (store != null) {
            return;
        }

        Path configPath = Objects.requireNonNull(configDirectory, "configDirectory").resolve("elib.json");
        store = new JsonConfigStore<>(configPath, ElibSettings.class, ElibSettings::new,
            ElibSettingsManager::sanitize, LOGGER);
        store.load();
    }

    public static ElibSettings get() {
        return Objects.requireNonNull(store, "Elib settings have not been initialized").get();
    }

    public static boolean save() {
        return Objects.requireNonNull(store, "Elib settings have not been initialized").save();
    }

    public static void markModMenuAvailable() {
        modMenuAvailable = true;
    }

    public static boolean isModMenuAvailable() {
        return modMenuAvailable;
    }

    public static void markModMenuRegistered() {
        modMenuRegistered = true;
    }

    public static boolean shouldShowOptionsButton() {
        ElibSettings settings = get();
        return settings.showOptionsButton || !modMenuRegistered || !settings.modMenuIntegration;
    }

    public static boolean canHideOptionsButton() {
        return modMenuRegistered && get().modMenuIntegration;
    }

    public static boolean canDisableModMenuIntegration() {
        return get().showOptionsButton;
    }

    public static boolean setShowOptionsButton(boolean enabled) {
        if (!enabled && !canHideOptionsButton()) {
            return false;
        }
        get().showOptionsButton = enabled;
        return save();
    }

    public static boolean setModMenuIntegration(boolean enabled) {
        if (!enabled && !canDisableModMenuIntegration()) {
            return false;
        }
        get().modMenuIntegration = enabled;
        return save();
    }

    public static boolean setConfigNotifications(boolean enabled) {
        get().configNotifications = enabled;
        return save();
    }

    /**
     * Gets the shared language preference without requiring settings to be initialized.
     * @return the saved locale code, or {@code auto} before client settings have loaded
     */
    public static String languagePreference() {
        return store == null ? ElibLanguage.AUTOMATIC : get().language;
    }

    /**
     * Saves the language shared by Elib and registered mods. Call {@link ElibLanguage#refresh()}
     * on the client thread after a successful change.
     *
     * @param language a supported locale code or {@code auto}
     * @return whether the preference was saved
     */
    public static boolean setLanguage(String language) {
        String previous = get().language;
        get().language = ElibLanguage.normalizePreference(language);
        if (save()) {
            return true;
        }
        get().language = previous;
        return false;
    }

    public static boolean resetToDefaults() {
        get().showOptionsButton = true;
        get().modMenuIntegration = true;
        get().configNotifications = true;
        get().language = ElibLanguage.AUTOMATIC;
        return save();
    }

    private static void sanitize(ElibSettings settings) {
        settings.language = ElibLanguage.normalizePreference(settings.language);
        if (!settings.showOptionsButton && !settings.modMenuIntegration) {
            settings.showOptionsButton = true;
        }
    }
}
