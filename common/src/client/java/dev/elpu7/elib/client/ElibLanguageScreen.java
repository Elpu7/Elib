package dev.elpu7.elib.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Scrollable native-name language picker for Elib and its registered mods. */
final class ElibLanguageScreen extends Screen {
    private final Screen parent;
    private LanguageList languages;
    private boolean saveFailed;

    ElibLanguageScreen(Screen parent) {
        super(Component.translatable("screen.elib.language.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        languages = addRenderableWidget(new LanguageList());
        addRenderableWidget(Button.builder(Component.translatable("screen.elib.done"), button -> apply())
            .bounds((width - 200) / 2, ElibScreenStyle.footerButtonY(height), 200, 20).build());
        setInitialFocus(languages);
    }

    private void apply() {
        LanguageEntry selected = languages.getSelected();
        if (selected == null || selected.code.equals(ElibSettingsManager.languagePreference())) {
            onClose();
        } else if (ElibSettingsManager.setLanguage(selected.code)) {
            ElibLanguage.refresh();
            onClose();
        } else {
            saveFailed = true;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ElibScreenStyle.drawBackdrop(graphics, width, height);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 10, 0xFFFFFFFF);
        if (saveFailed) {
            graphics.centeredText(font, Component.translatable("screen.elib.language.save_failed"),
                width / 2, 29, 0xFFFF5555);
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private final class LanguageList extends ObjectSelectionList<LanguageEntry> {
        LanguageList() {
            super(ElibLanguageScreen.this.minecraft, ElibLanguageScreen.this.width,
                Math.max(24, ElibLanguageScreen.this.height - 82), 46, 24);
            addChoice(ElibLanguage.AUTOMATIC, Component.translatable("screen.elib.language.auto"));
            for (ElibLanguage.Choice choice : ElibLanguage.choices()) {
                addChoice(choice.code(), Component.literal(choice.name()));
            }
            if (getSelected() != null) {
                centerScrollOn(getSelected());
            }
        }

        private void addChoice(String code, Component name) {
            LanguageEntry entry = new LanguageEntry(code, name);
            addEntry(entry);
            if (code.equals(ElibSettingsManager.languagePreference())) {
                setSelected(entry);
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(300, ElibLanguageScreen.this.width - 40);
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
            // Elib's screen already draws the shared list background.
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
        }
    }

    private final class LanguageEntry extends ObjectSelectionList.Entry<LanguageEntry> {
        private final String code;
        private final Component name;

        LanguageEntry(String code, Component name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float partialTick) {
            graphics.centeredText(font, name, getContentXMiddle(), getContentYMiddle() - 4, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
                return false;
            }
            languages.setSelected(this);
            if (doubleClick) {
                apply();
            }
            return true;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER
                    || event.key() == InputConstants.KEY_SPACE) {
                languages.setSelected(this);
                apply();
                return true;
            }
            return super.keyPressed(event);
        }

        @Override
        public Component getNarration() {
            return name;
        }
    }
}
