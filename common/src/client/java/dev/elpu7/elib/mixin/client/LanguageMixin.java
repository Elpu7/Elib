package dev.elpu7.elib.mixin.client;

import dev.elpu7.elib.client.ElibLanguage;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Language.class)
public abstract class LanguageMixin {
    @ModifyVariable(method = "inject", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Language elib$applySharedLanguage(Language language) {
        return ElibLanguage.wrap(language);
    }
}
