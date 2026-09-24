package dev.elpu7.elib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Stores one JSON configuration file. The caller owns the configuration type,
 * defaults, validation, and the point at which the configuration is saved.
 * Validation must modify the supplied object in place, so references held by
 * screens and controllers remain valid.
 */
public final class JsonConfigStore<T> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final Class<T> type;
    private final Supplier<T> defaults;
    private final Consumer<T> sanitize;
    private final Logger logger;
    private T config;

    public JsonConfigStore(
        Path path,
        Class<T> type,
        Supplier<T> defaults,
        Consumer<T> sanitize,
        Logger logger
    ) {
        this.path = Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
        this.type = Objects.requireNonNull(type, "type");
        this.defaults = Objects.requireNonNull(defaults, "defaults");
        this.sanitize = Objects.requireNonNull(sanitize, "sanitize");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.config = newDefaults();
    }

    public T get() {
        return config;
    }

    public Path path() {
        return path;
    }

    /** Loads the file, creating it with defaults only when it is missing. */
    public void load() {
        try (Reader reader = Files.newBufferedReader(path)) {
            T loaded = GSON.fromJson(reader, type);
            if (loaded == null) {
                throw new IllegalArgumentException("Config file contains null");
            }
            sanitize.accept(loaded);
            config = loaded;
        } catch (NoSuchFileException exception) {
            config = newDefaults();
            save();
        } catch (IOException exception) {
            logger.error("Failed to read config {}; using defaults without overwriting it", path, exception);
            config = newDefaults();
        } catch (RuntimeException exception) {
            logger.error("Failed to parse config {}; using defaults", path, exception);
            boolean backedUp = backupBrokenConfig();
            config = newDefaults();
            if (backedUp) {
                save();
            }
        }
    }

    /** Writes through a temporary file, leaving the previous file intact on failure. */
    public boolean save() {
        Path temporaryPath = null;

        try {
            sanitize.accept(config);
            Files.createDirectories(path.getParent());
            temporaryPath = Files.createTempFile(path.getParent(), path.getFileName() + "-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporaryPath)) {
                GSON.toJson(config, writer);
            }

            try {
                Files.move(temporaryPath, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
            }
            temporaryPath = null;
            return true;
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to save config {}", path, exception);
            return false;
        } finally {
            if (temporaryPath != null) {
                try {
                    Files.deleteIfExists(temporaryPath);
                } catch (IOException exception) {
                    logger.warn("Failed to remove temporary config {}", temporaryPath, exception);
                }
            }
        }
    }

    private T newDefaults() {
        T value = Objects.requireNonNull(defaults.get(), "defaults returned null");
        sanitize.accept(value);
        return value;
    }

    private boolean backupBrokenConfig() {
        Path backupPath = path.resolveSibling(path.getFileName() + ".broken-" + System.currentTimeMillis());
        try {
            Files.move(path, backupPath);
            logger.warn("Moved broken config {} to {}", path, backupPath);
            return true;
        } catch (IOException exception) {
            logger.error("Failed to back up broken config {}", path, exception);
            return false;
        }
    }
}
