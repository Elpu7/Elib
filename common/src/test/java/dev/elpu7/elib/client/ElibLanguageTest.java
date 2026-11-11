package dev.elpu7.elib.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Pattern;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElibLanguageTest {
    @Test
    void automaticFollowsSupportedClientLanguagesAndOtherwiseUsesEnglish() {
        assertEquals("fi_fi", ElibLanguage.resolve("auto", "fi_fi"));
        assertEquals("zh_tw", ElibLanguage.resolve("auto", "zh_tw"));
        assertEquals("en_us", ElibLanguage.resolve("auto", "en_gb"));
        assertEquals("en_us", ElibLanguage.resolve("auto", "eo_uy"));
        assertEquals("en_us", ElibLanguage.resolve("auto", null));
    }

    @Test
    void explicitLanguageOverridesClientAndInvalidPreferencesAreRepaired() {
        assertEquals("ja_jp", ElibLanguage.resolve("ja_jp", "fi_fi"));
        assertEquals("fi_fi", ElibLanguage.normalizePreference("FI-FI"));
        assertEquals("auto", ElibLanguage.normalizePreference("../../other"));
        assertEquals("auto", ElibLanguage.normalizePreference(null));
    }

    @Test
    void missingKeysAndMissingLocaleFilesFallBackToEnglish() {
        var partial = ElibLanguage.translations("elib-test", "fi_fi");
        // The central file takes priority over the test mod's own Finnish file.
        assertEquals("Suomi", partial.get("screen.elib-test.title"));
        assertEquals("English fallback", partial.get("screen.elib-test.fallback"));
        assertEquals("English title", ElibLanguage.translations("elib-test", "ja_jp")
            .get("screen.elib-test.title"));
        assertFalse(partial.containsKey("gui.done"), "A mod must not replace Minecraft's own keys");
    }

    @Test
    void onlyModTextChangesAndWrappersDoNotAccumulate() {
        Language base = new Language() {
            @Override public String getOrDefault(String key, String fallback) { return "vanilla:" + key; }
            @Override public boolean has(String key) { return key.equals("gui.done"); }
            @Override public boolean isDefaultRightToLeft() { return false; }
            @Override public FormattedCharSequence getVisualOrder(FormattedText text) {
                return FormattedCharSequence.EMPTY;
            }
        };
        Language finnish = ElibLanguage.wrap(base, "auto", "fi_fi");
        assertEquals("Elibin asetukset", finnish.getOrDefault("screen.elib.title"));
        assertEquals("vanilla:gui.done", finnish.getOrDefault("gui.done"));
        assertFalse(finnish.isDefaultRightToLeft());
        Language english = ElibLanguage.wrap(finnish, "en_us", "fi_fi");
        assertEquals("Elib Config", english.getOrDefault("screen.elib.title"));
        assertTrue(english.has("screen.elib.language"));
        assertFalse(ElibLanguage.wrap(base, "ar_sa", "fi_fi").isDefaultRightToLeft(),
            "The rest of Minecraft must retain its own layout direction");
    }

    @Test
    void sharedLanguagePreservesMinecraftTextOrdering() {
        Language base = new Language() {
            @Override public String getOrDefault(String key, String fallback) { return fallback; }
            @Override public boolean has(String key) { return false; }
            @Override public boolean isDefaultRightToLeft() { return false; }
            @Override public FormattedCharSequence getVisualOrder(FormattedText text) {
                return FormattedCharSequence.EMPTY;
            }
        };
        Language arabic = ElibLanguage.wrap(base, "ar_sa", "en_us");
        assertSame(FormattedCharSequence.EMPTY, arabic.getVisualOrder(FormattedText.of("مرحبا")),
            "Changing Elib's language must not override Minecraft's own text renderer");
    }

    @Test
    void cachedComponentsUpdateWhenTheSharedLanguageChanges() {
        Language original = Language.getInstance();
        try {
            Language.inject(ElibLanguage.wrap(original, "en_us", "en_us"));
            Component title = Component.translatable("screen.elib.title");
            assertEquals("Elib Config", title.getString());
            Language.inject(ElibLanguage.wrap(Language.getInstance(), "fi_fi", "en_us"));
            assertEquals("Elibin asetukset", title.getString());
        } finally {
            Language.inject(original);
        }
    }

    @Test
    void elibLoadsOtherModsTranslationsFromItsOwnResources() {
        assertEquals("Vaihda Freecamin tilaa", ElibLanguage.translations("easy-freecam", "fi_fi")
            .get("key.easy-freecam.toggle_freecam"));
        assertEquals("Esikatselu", ElibLanguage.translations("piechart", "fi_fi")
            .get("screen.piechart.preview"));
        assertEquals("ライブプレビュー", ElibLanguage.translations("piechart", "ja_jp")
            .get("screen.piechart.preview"));
    }

    @Test
    void centralizedModCatalogsHaveAllLanguagesAndMatchingFormatArguments() throws Exception {
        for (String namespace : new String[] {"easy-freecam", "piechart"}) {
            String root = "/assets/elib/translations/" + namespace + "/";
            assertNull(getClass().getResourceAsStream(root + "en_us.json"),
                "English remains in the owning mod");
            JsonObject reference = readResource(root + "fi_fi.json");
            assertFalse(reference.keySet().isEmpty());
            Pattern arguments = Pattern.compile("%(?:[0-9]+\\$)?s");
            int translatedLanguages = 0;
            for (ElibLanguage.Choice choice : ElibLanguage.choices()) {
                if (choice.code().equals("en_us")) {
                    continue;
                }
                JsonObject translated = readResource(root + choice.code() + ".json");
                assertEquals(reference.keySet(), translated.keySet(), namespace + "/" + choice.code());
                for (String key : reference.keySet()) {
                    String value = translated.get(key).getAsString();
                    assertFalse(value.isBlank(), namespace + "/" + choice.code() + ": " + key);
                    assertEquals(arguments.matcher(reference.get(key).getAsString()).results().count(),
                        arguments.matcher(value).results().count(), namespace + "/" + choice.code() + ": " + key);
                }
                translatedLanguages++;
            }
            assertEquals(19, translatedLanguages);
        }
    }

    @Test
    void everyListedLanguageHasAllKeysAndCorrectFormatArguments() throws Exception {
        JsonObject english = read("en_us");
        var codes = new HashSet<String>();
        Pattern arguments = Pattern.compile("%(?:[0-9]+\\$)?s");
        for (ElibLanguage.Choice choice : ElibLanguage.choices()) {
            assertTrue(codes.add(choice.code()), "Duplicate language: " + choice.code());
            JsonObject translated = read(choice.code());
            assertEquals(english.keySet(), translated.keySet(), choice.code());
            for (String key : english.keySet()) {
                String value = translated.get(key).getAsString();
                assertFalse(value.isBlank(), choice.code() + ": " + key);
                assertEquals(arguments.matcher(english.get(key).getAsString()).results().count(),
                    arguments.matcher(value).results().count(), choice.code() + ": " + key);
            }
        }
        assertEquals(20, codes.size());
    }

    private JsonObject read(String code) throws Exception {
        return readResource("/assets/elib/lang/" + code + ".json");
    }

    private JsonObject readResource(String path) throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream(path), path);
             var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
