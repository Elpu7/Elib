package dev.elpu7.elib.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ElibConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.elib.title");
    private static final Component MODS_BUTTON = Component.translatable("screen.elib.mods_button");
    private static final Component RESTART_NOTE = Component.translatable("screen.elib.modmenu_restart");
    private static final Component KEEP_ENTRYPOINT = Component.translatable("screen.elib.keep_entrypoint");
    private static final Component MODMENU_UNAVAILABLE = Component.translatable("screen.elib.modmenu_unavailable");

    private final Screen parent;

    public ElibConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        StringWidget heading = new StringWidget(TITLE, font);
        heading.setPosition((width - heading.getWidth()) / 2, 10);
        addRenderableOnly(heading);

        Button optionsButton = addRenderableWidget(Button.builder(optionsButtonText(), button -> {
            ElibSettingsManager.setShowOptionsButton(!ElibSettingsManager.get().showOptionsButton);
            refreshButtons();
        }).bounds(ElibScreenStyle.leftButtonX(width), 38,
            ElibScreenStyle.BUTTON_WIDTH, 20).build());

        Button modMenuButton = addRenderableWidget(Button.builder(modMenuButtonText(), button -> {
            ElibSettingsManager.setModMenuIntegration(!ElibSettingsManager.get().modMenuIntegration);
            refreshButtons();
        }).bounds(ElibScreenStyle.rightButtonX(width), 38,
            ElibScreenStyle.BUTTON_WIDTH, 20).build());

        addRenderableWidget(Button.builder(MODS_BUTTON, button ->
            minecraft.gui.setScreen(new ElibModsScreen(this, parent)))
            .bounds((width - 200) / 2, 66, 200, 20).build());

        optionsButton.active = !ElibSettingsManager.get().showOptionsButton
            || ElibSettingsManager.canHideOptionsButton();
        modMenuButton.active = ElibSettingsManager.isModMenuAvailable()
            && (!ElibSettingsManager.get().modMenuIntegration
                || ElibSettingsManager.canDisableModMenuIntegration());
        if (!optionsButton.active) {
            optionsButton.setTooltip(Tooltip.create(KEEP_ENTRYPOINT));
        }
        if (!modMenuButton.active) {
            modMenuButton.setTooltip(Tooltip.create(ElibSettingsManager.isModMenuAvailable()
                ? KEEP_ENTRYPOINT : MODMENU_UNAVAILABLE));
        } else {
            modMenuButton.setTooltip(Tooltip.create(RESTART_NOTE));
        }

        int footerY = ElibScreenStyle.footerButtonY(height);
        addRenderableWidget(Button.builder(Component.translatable("screen.elib.reset_defaults"), button -> {
            ElibSettingsManager.resetToDefaults();
            refreshButtons();
        }).bounds(ElibScreenStyle.leftButtonX(width), footerY,
            ElibScreenStyle.BUTTON_WIDTH, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
            .bounds(ElibScreenStyle.rightButtonX(width), footerY,
                ElibScreenStyle.BUTTON_WIDTH, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ElibScreenStyle.drawBackdrop(graphics, width, height);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void refreshButtons() {
        minecraft.gui.setScreen(new ElibConfigScreen(parent));
    }

    private static Component optionsButtonText() {
        return Component.translatable("screen.elib.options_button", onOff(ElibSettingsManager.get().showOptionsButton));
    }

    private static Component modMenuButtonText() {
        return Component.translatable("screen.elib.modmenu_integration", onOff(ElibSettingsManager.get().modMenuIntegration));
    }

    private static Component onOff(boolean enabled) {
        return Component.translatable(enabled ? "screen.elib.on" : "screen.elib.off");
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
