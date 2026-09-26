package dev.elpu7.elib.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElibSettingsManagerTest {
    @TempDir
    Path directory;

    @Test
    void keepsAtLeastOneAvailableWayToOpenSettings() throws Exception {
        Files.writeString(directory.resolve("elib.json"),
            "{\"showOptionsButton\":false,\"modMenuIntegration\":false}");
        ElibSettingsManager.initialize(directory);

        assertTrue(ElibSettingsManager.shouldShowOptionsButton());
        assertTrue(ElibSettingsManager.get().showOptionsButton);
        assertFalse(ElibSettingsManager.setShowOptionsButton(false));

        ElibSettingsManager.markModMenuAvailable();
        assertTrue(ElibSettingsManager.setModMenuIntegration(true));
        ElibSettingsManager.markModMenuRegistered();
        assertTrue(ElibSettingsManager.setShowOptionsButton(false));
        assertFalse(ElibSettingsManager.setModMenuIntegration(false));
        assertFalse(ElibSettingsManager.shouldShowOptionsButton());

        assertTrue(ElibSettingsManager.setShowOptionsButton(true));
        assertTrue(ElibSettingsManager.setModMenuIntegration(false));
        assertFalse(ElibSettingsManager.setShowOptionsButton(false));
        assertTrue(Files.readString(directory.resolve("elib.json"))
            .contains("\"modMenuIntegration\": false"));

        assertTrue(ElibSettingsManager.resetToDefaults());
        assertTrue(ElibSettingsManager.get().showOptionsButton);
        assertTrue(ElibSettingsManager.get().modMenuIntegration);
    }
}
