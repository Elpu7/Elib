package dev.elpu7.elib.quilt.mixin.client;

import dev.elpu7.elib.client.ElibSettingsManager;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "<init>", at = @At("HEAD"))
    private void elib$initializeSettings(Minecraft minecraft, File gameDirectory, CallbackInfo callbackInfo) {
        ElibSettingsManager.initialize(QuiltLoader.getConfigDir());
    }
}
