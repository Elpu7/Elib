package dev.elpu7.elib.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class JsonConfigStoreTest {
    @TempDir
    Path directory;

    @Test
    void createsMissingConfigWithDefaults() throws IOException {
        Path path = directory.resolve("settings.json");
        JsonConfigStore<TestConfig> store = createStore(path);

        store.load();

        assertEquals(5.0D, store.get().speed);
        assertTrue(Files.isRegularFile(path));
        assertTrue(Files.readString(path).contains("\"speed\": 5.0"));
    }

    @Test
    void sanitizesLoadedAndSavedValues() throws IOException {
        Path path = directory.resolve("settings.json");
        Files.writeString(path, "{\"speed\": 99.0}");
        JsonConfigStore<TestConfig> store = createStore(path);

        store.load();
        assertEquals(10.0D, store.get().speed);

        store.get().speed = -5.0D;
        assertTrue(store.save());
        assertEquals(1.0D, store.get().speed);
        assertTrue(Files.readString(path).contains("\"speed\": 1.0"));
    }

    @Test
    void backsUpMalformedConfigBeforeWritingDefaults() throws IOException {
        Path path = directory.resolve("settings.json");
        Files.writeString(path, "{invalid json");
        JsonConfigStore<TestConfig> store = createStore(path);

        store.load();

        assertEquals(5.0D, store.get().speed);
        assertTrue(Files.readString(path).contains("\"speed\": 5.0"));
        try (Stream<Path> files = Files.list(directory)) {
            Path backup = files.filter(file -> file.getFileName().toString().startsWith("settings.json.broken-"))
                .findFirst().orElseThrow();
            assertEquals("{invalid json", Files.readString(backup));
        }
    }

    @Test
    void doesNotOverwriteUnreadableConfig() {
        Path path = directory.resolve("settings.json");
        assertTrue(path.toFile().mkdir());
        JsonConfigStore<TestConfig> store = createStore(path);

        store.load();

        assertEquals(5.0D, store.get().speed);
        assertTrue(Files.isDirectory(path));
        assertFalse(store.save());
        assertTrue(Files.isDirectory(path));
    }

    private static JsonConfigStore<TestConfig> createStore(Path path) {
        return new JsonConfigStore<>(
            path,
            TestConfig.class,
            TestConfig::new,
            config -> config.speed = Math.clamp(config.speed, 1.0D, 10.0D),
            LoggerFactory.getLogger(JsonConfigStoreTest.class)
        );
    }

    private static final class TestConfig {
        double speed = 5.0D;
    }
}
