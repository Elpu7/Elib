package dev.elpu7.elib.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ElibModsScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.elib.title");
    private static final Component NO_MODS = Component.translatable("screen.elib.no_mods");
    private static final int MOD_BUTTON_WIDTH = 200;
    private static final int MOD_BUTTON_HEIGHT = 30;
    private static final int ICON_SIZE = MOD_BUTTON_HEIGHT;

    private final Screen parent;
    private final Screen rootParent;
    private final List<IconPlacement> icons = new ArrayList<>();

    public ElibModsScreen(Screen parent, Screen rootParent) {
        super(TITLE);
        this.parent = parent;
        this.rootParent = rootParent;
    }

    @Override
    protected void init() {
        icons.clear();
        StringWidget heading = new StringWidget(TITLE, font);
        heading.setPosition((width - heading.getWidth()) / 2, 10);
        addRenderableOnly(heading);

        var entries = ElibConfigRegistry.entries();
        if (entries.isEmpty()) {
            StringWidget noMods = new StringWidget(NO_MODS, font);
            noMods.setPosition((width - noMods.getWidth()) / 2, 45);
            addRenderableOnly(noMods);
        } else {
            int buttonX = (width - MOD_BUTTON_WIDTH) / 2;
            int iconX = buttonX - ICON_SIZE - 12;
            for (int index = 0; index < entries.size(); index++) {
                ElibConfigRegistry.Entry entry = entries.get(index);
                int y = 38 + index * 36;
                if (entry.icon() != null) {
                    icons.add(new IconPlacement(entry.icon(), iconX, y));
                }
                Component label = entry.version().isBlank() ? entry.name() : Component.empty()
                    .append(entry.name())
                    .append(Component.literal("  " + entry.version()).withStyle(ChatFormatting.GRAY));
                addRenderableWidget(Button.builder(label, button ->
                    minecraft.gui.setScreen(entry.createScreen(this)))
                    .bounds(buttonX, y, MOD_BUTTON_WIDTH, MOD_BUTTON_HEIGHT)
                    .build());
            }
        }

        int footerY = ElibScreenStyle.footerButtonY(height);
        addRenderableWidget(Button.builder(TITLE, button -> onClose())
            .bounds(ElibScreenStyle.leftButtonX(width), footerY,
                ElibScreenStyle.BUTTON_WIDTH, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button ->
            minecraft.gui.setScreen(rootParent))
            .bounds(ElibScreenStyle.rightButtonX(width), footerY,
                ElibScreenStyle.BUTTON_WIDTH, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ElibScreenStyle.drawBackdrop(graphics, width, height);
        for (IconPlacement icon : icons) {
            graphics.outline(icon.x() - 1, icon.y() - 1, ICON_SIZE + 2, ICON_SIZE + 2, 0xFF777777);
            // Minecraft 26.3 expects UVs in u0, u1, v0, v1 order.
            graphics.blit(icon.texture(), icon.x(), icon.y(),
                icon.x() + ICON_SIZE, icon.y() + ICON_SIZE,
                0.0F, 1.0F, 0.0F, 1.0F);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private record IconPlacement(Identifier texture, int x, int y) {
    }
}
