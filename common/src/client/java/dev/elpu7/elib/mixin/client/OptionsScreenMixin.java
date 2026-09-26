package dev.elpu7.elib.mixin.client;

import dev.elpu7.elib.client.ElibFaceButton;
import dev.elpu7.elib.client.ElibConfigScreen;
import dev.elpu7.elib.client.ElibSettingsManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    @Unique
    private ElibFaceButton elib$openButton;

    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void elib$addOpenButton(CallbackInfo callbackInfo) {
        elib$openButton = null;
        if (!ElibSettingsManager.shouldShowOptionsButton()) {
            return;
        }
        Button doneButton = elib$findDoneButton();
        if (doneButton == null) {
            return;
        }

        elib$openButton = addRenderableWidget(new ElibFaceButton(0, 0, () ->
            minecraft.gui.setScreen(new ElibConfigScreen((Screen) (Object) this))));
        elib$positionOpenButton(doneButton);
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void elib$repositionOpenButton(CallbackInfo callbackInfo) {
        if (elib$openButton != null) {
            Button doneButton = elib$findDoneButton();
            if (doneButton != null) {
                elib$positionOpenButton(doneButton);
            }
        }
    }

    @Unique
    private Button elib$findDoneButton() {
        for (var child : children()) {
            if (child instanceof Button button && button.getMessage().equals(CommonComponents.GUI_DONE)) {
                return button;
            }
        }
        return null;
    }

    @Unique
    private void elib$positionOpenButton(Button doneButton) {
        int x = doneButton.getRight() + 4;
        if (x + 20 > width) {
            x = doneButton.getX() - 24;
        }
        elib$openButton.setPosition(x, doneButton.getY());
    }
}
