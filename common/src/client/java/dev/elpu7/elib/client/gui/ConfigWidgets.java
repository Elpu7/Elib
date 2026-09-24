package dev.elpu7.elib.client.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Small option helpers; individual mods still own their screen layouts. */
public final class ConfigWidgets {
    private ConfigWidgets() {
    }

    public static OptionInstance<Boolean> booleanOption(
        String translationKey,
        String tooltipKey,
        BooleanSupplier currentValue,
        Consumer<Boolean> onChanged
    ) {
        return OptionInstance.createBoolean(
            translationKey,
            OptionInstance.cachedConstantTooltip(Component.translatable(tooltipKey)),
            currentValue.getAsBoolean(),
            onChanged::accept
        );
    }
}
