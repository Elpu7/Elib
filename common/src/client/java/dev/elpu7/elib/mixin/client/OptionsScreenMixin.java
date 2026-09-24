package dev.elpu7.elib.mixin.client;

import dev.elpu7.elib.client.ElibFaceButton;
import dev.elpu7.elib.client.ElibInfoScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void elib$addOpenButton(CallbackInfo callbackInfo) {
        Button doneButton = null;
        for (var child : children()) {
            if (child instanceof Button button && button.getMessage().equals(CommonComponents.GUI_DONE)) {
                doneButton = button;
                break;
            }
        }

        if (doneButton == null) {
            return;
        }

        int x = doneButton.getRight() + 4;
        if (x + 20 > width) {
            x = doneButton.getX() - 24;
        }
        addRenderableWidget(new ElibFaceButton(x, doneButton.getY(), () ->
            minecraft.gui.setScreen(new ElibInfoScreen((Screen) (Object) this))));
    }
}
