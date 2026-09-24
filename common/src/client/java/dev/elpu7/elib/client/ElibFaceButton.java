package dev.elpu7.elib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;

public final class ElibFaceButton extends AbstractButton {
    private static final int SIZE = 20;
    private static final Component LABEL = Component.translatable("screen.elib.title");

    private final Runnable onPress;
    private final ResolvableProfile accountProfile;

    public ElibFaceButton(int x, int y, Runnable onPress) {
        super(x, y, SIZE, SIZE, LABEL);
        this.onPress = onPress;
        this.accountProfile = ResolvableProfile.createResolved(Minecraft.getInstance().getGameProfile());
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractDefaultSprite(graphics);
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            PlayerFaceExtractor.extractRenderState(graphics, client.player.getSkin(), getX() + 2, getY() + 2, 16);
        } else {
            PlayerFaceExtractor.extractRenderState(graphics, accountProfile, getX() + 2, getY() + 2, 16);
        }
    }
}
