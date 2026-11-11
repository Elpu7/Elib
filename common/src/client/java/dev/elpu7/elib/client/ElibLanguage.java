package dev.elpu7.elib.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shares Elib's language preference with registered mods without changing Minecraft's language. */
public final class ElibLanguage {
    /** Preference that follows Minecraft, falling back to English when unsupported. */
    public static final String AUTOMATIC = "auto";
    private static final String ENGLISH = "en_us";
    private static final Logger LOGGER = LoggerFactory.getLogger("elib");
    private static final List<Choice> CHOICES = List.of(
        new Choice(ENGLISH, "English"), new Choice("es_es", "Español"),
        new Choice("fr_fr", "Français"), new Choice("de_de", "Deutsch"),
        new Choice("zh_cn", "简体中文"), new Choice("zh_tw", "繁體中文"),
        new Choice("ja_jp", "日本語"), new Choice("pt_br", "Português (Brasil)"),
        new Choice("ru_ru", "Русский"), new Choice("ko_kr", "한국어"),
        new Choice("it_it", "Italiano"), new Choice("nl_nl", "Nederlands"),
        new Choice("pl_pl", "Polski"), new Choice("tr_tr", "Türkçe"),
        new Choice("sv_se", "Svenska"), new Choice("fi_fi", "Suomi"),
        new Choice("ar_sa", "العربية"), new Choice("hi_in", "हिन्दी"),
        new Choice("id_id", "Bahasa Indonesia"), new Choice("uk_ua", "Українська")
    );
    private static final Map<String, Class<?>> NAMESPACES = new LinkedHashMap<>(Map.of("elib", ElibLanguage.class));
    private static final Map<String, Map<String, String>> CACHE = new LinkedHashMap<>();

    private ElibLanguage() {
    }

    /**
     * Lists the languages available in Elib's picker.
     * @return supported Minecraft locale codes and native display names, in menu order
     */
    public static List<Choice> choices() {
        return CHOICES;
    }

    /**
     * Registers a mod's English fallback with Elib's language setting.
     * Translations bundled by Elib are read from
     * {@code assets/elib/translations/<namespace>/<locale>.json} first; a mod's own
     * standard language files are used when no central translation exists.
     * Config-screen registration already calls this automatically.
     *
     * @param namespace resource namespace owned by the mod
     * @param resourceOwner a class from the mod JAR containing its English language file
     */
    public static synchronized void register(String namespace, Class<?> resourceOwner) {
        if (namespace == null || !namespace.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("Invalid translation namespace: " + namespace);
        }
        if (resourceOwner == null) {
            throw new IllegalArgumentException("resourceOwner must not be null");
        }
        if (NAMESPACES.put(namespace, resourceOwner) != resourceOwner) {
            CACHE.keySet().removeIf(key -> key.startsWith(namespace + "/"));
            refresh();
        }
    }

    /** Refreshes translated components after changing Elib's language preference. Call on the client thread. */
    public static void refresh() {
        // A new Language identity also invalidates Minecraft's cached translatable components.
        Language.inject(wrap(Language.getInstance()));
    }

    /**
     * Wraps a newly loaded Minecraft language. Used by Elib's client mixin on every language reload.
     *
     * @param language Minecraft's original language, or a previous Elib wrapper
     * @return an overlay which only replaces keys supplied by registered mods
     */
    public static Language wrap(Language language) {
        Minecraft client = Minecraft.getInstance();
        String clientLanguage = client != null && client.options != null ? client.options.languageCode : ENGLISH;
        return wrap(language, ElibSettingsManager.languagePreference(), clientLanguage);
    }

    static synchronized Language wrap(Language language, String preference, String clientLanguage) {
        Language base = language instanceof Overlay overlay ? overlay.base : language;
        String selected = resolve(preference, clientLanguage);
        Map<String, String> translations = new LinkedHashMap<>();
        for (String namespace : NAMESPACES.keySet()) {
            translations.putAll(translations(namespace, selected));
        }
        return new Overlay(base, Map.copyOf(translations));
    }

    static Map<String, String> translations(String namespace, String code) {
        Map<String, String> result = new LinkedHashMap<>(load(namespace, ENGLISH));
        if (!ENGLISH.equals(code)) {
            result.putAll(load(namespace, code));
        }
        return result;
    }

    static String normalizePreference(String preference) {
        String normalized = normalizeCode(preference);
        return AUTOMATIC.equals(normalized) || supported(normalized) ? normalized : AUTOMATIC;
    }

    static String resolve(String preference, String clientLanguage) {
        String normalized = normalizePreference(preference);
        String candidate = AUTOMATIC.equals(normalized) ? normalizeCode(clientLanguage) : normalized;
        return supported(candidate) ? candidate : ENGLISH;
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private static boolean supported(String code) {
        return CHOICES.stream().anyMatch(choice -> choice.code().equals(code));
    }

    private static Map<String, String> load(String namespace, String code) {
        return CACHE.computeIfAbsent(namespace + "/" + code, ignored -> {
            try (InputStream input = openTranslations(namespace, code)) {
                if (input == null) {
                    return Map.of();
                }
                Map<String, String> translations = new LinkedHashMap<>();
                Language.loadFromJson(input, (key, value) -> {
                    // A mod must not override Minecraft's own keys through this API.
                    if (key.startsWith(namespace + ".") || key.contains("." + namespace + ".")
                            || key.endsWith("." + namespace)) {
                        translations.put(key, value);
                    }
                });
                return Map.copyOf(translations);
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Could not load translations for {}/{}; falling back to English",
                    namespace, code, exception);
                return Map.of();
            }
        });
    }

    private static InputStream openTranslations(String namespace, String code) {
        if (!ENGLISH.equals(code)) {
            InputStream bundled = ElibLanguage.class.getResourceAsStream(
                "/assets/elib/translations/" + namespace + "/" + code + ".json");
            if (bundled != null) {
                return bundled;
            }
        }
        Class<?> owner = NAMESPACES.getOrDefault(namespace, ElibLanguage.class);
        return owner.getResourceAsStream("/assets/" + namespace + "/lang/" + code + ".json");
    }

    /**
     * A supported locale and its native name.
     * @param code Minecraft locale code
     * @param name native display name
     */
    public record Choice(String code, String name) {
    }

    private static final class Overlay extends Language {
        private final Language base;
        private final Map<String, String> translations;

        private Overlay(Language base, Map<String, String> translations) {
            this.base = base;
            this.translations = translations;
        }

        @Override
        public String getOrDefault(String key, String fallback) {
            String translation = translations.get(key);
            return translation != null ? translation : base.getOrDefault(key, fallback);
        }

        @Override
        public boolean has(String key) {
            return translations.containsKey(key) || base.has(key);
        }

        @Override
        public boolean isDefaultRightToLeft() {
            return base.isDefaultRightToLeft();
        }

        @Override
        public FormattedCharSequence getVisualOrder(FormattedText text) {
            // Minecraft already shapes bidirectional text; keep its rendering behavior unchanged.
            return base.getVisualOrder(text);
        }
    }
}
