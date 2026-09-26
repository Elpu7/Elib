package dev.elpu7.elib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class ElibScreenStyle {
    static final int BUTTON_WIDTH = 150;
    static final int BUTTON_GAP = 8;
    static final int HEADER_HEIGHT = 33;
    static final int FOOTER_HEIGHT = 33;
    private static final Identifier MENU_LIST_BACKGROUND =
        Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");

    private ElibScreenStyle() {
    }

    static int leftButtonX(int width) {
        return (width - BUTTON_WIDTH * 2 - BUTTON_GAP) / 2;
    }

    static int rightButtonX(int width) {
        return leftButtonX(width) + BUTTON_WIDTH + BUTTON_GAP;
    }

    static int footerButtonY(int height) {
        return height - (FOOTER_HEIGHT + 20) / 2;
    }

    static void drawBackdrop(GuiGraphicsExtractor graphics, int width, int height) {
        boolean inWorld = Minecraft.getInstance().level != null;
        int listBottom = height - FOOTER_HEIGHT;
        Identifier listBackground = inWorld
            ? AbstractSelectionList.INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
        Identifier headerSeparator = inWorld ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        Identifier footerSeparator = inWorld ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;

        graphics.blit(RenderPipelines.GUI_TEXTURED, listBackground,
            0, HEADER_HEIGHT, (float) width, (float) listBottom,
            width, listBottom - HEADER_HEIGHT, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, headerSeparator,
            0, HEADER_HEIGHT - 2, 0.0F, 0.0F, width, 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, footerSeparator,
            0, listBottom, 0.0F, 0.0F, width, 2, 32, 2);
    }
}
