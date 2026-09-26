package dev.elpu7.elib.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

final class ElibScreenStyle {
    static final int BUTTON_WIDTH = 150;
    static final int BUTTON_GAP = 8;
    static final int HEADER_HEIGHT = 33;
    static final int FOOTER_HEIGHT = 33;

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
        graphics.fill(0, 0, width, height, 0x66000000);
        graphics.fill(0, 0, width, HEADER_HEIGHT, 0x44000000);
        graphics.horizontalLine(0, width, HEADER_HEIGHT, 0xFF555555);
        graphics.fill(0, height - FOOTER_HEIGHT, width, height, 0x44000000);
        graphics.horizontalLine(0, width, height - FOOTER_HEIGHT, 0xFF555555);
    }
}
