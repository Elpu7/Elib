package dev.elpu7.elib.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ElibInfoScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.elib.title");
    private static final Component DESCRIPTION = Component.translatable("screen.elib.description");
    private static final Component NO_SETTINGS = Component.translatable("screen.elib.no_settings");

    private final Screen parent;

    public ElibInfoScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(320, width - 32);
        int contentTop = Math.max(32, height / 2 - 42);

        StringWidget heading = new StringWidget(TITLE, font);
        heading.setPosition((width - heading.getWidth()) / 2, contentTop);
        addRenderableOnly(heading);

        MultiLineTextWidget description = new MultiLineTextWidget(DESCRIPTION, font)
            .setMaxWidth(contentWidth)
            .setCentered(true);
        description.setPosition((width - description.getWidth()) / 2, contentTop + 24);
        addRenderableOnly(description);

        MultiLineTextWidget noSettings = new MultiLineTextWidget(NO_SETTINGS, font)
            .setMaxWidth(contentWidth)
            .setCentered(true);
        noSettings.setPosition((width - noSettings.getWidth()) / 2, contentTop + 36 + description.getHeight());
        addRenderableOnly(noSettings);

        // Match OptionsScreen's footer layout, including its exact vertical centering.
        HeaderAndFooterLayout footerLayout = new HeaderAndFooterLayout(this, 61, 33);
        Button done = footerLayout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
            .width(200)
            .build());
        footerLayout.arrangeElements();
        addRenderableWidget(done);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
