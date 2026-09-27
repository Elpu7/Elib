package dev.elpu7.elib.client.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Small option helpers; individual mods still own their screen layouts. */
public final class ConfigWidgets {
    private ConfigWidgets() {
    }

    /**
     * Creates a Minecraft boolean option from the caller's current value.
     *
     * @param translationKey translation key for the option label
     * @param tooltipKey translation key for the tooltip
     * @param currentValue supplies the initial value
     * @param onChanged handles changes; the caller decides when to save
     * @return a boolean option ready to add to an options screen
     */
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
