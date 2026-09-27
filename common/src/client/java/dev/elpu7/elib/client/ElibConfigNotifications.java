package dev.elpu7.elib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/** Shared, opt-in UI feedback for configuration actions in Elib-based mods. */
public final class ElibConfigNotifications {
    private static final SystemToast.SystemToastId RESET = new SystemToast.SystemToastId();

    private ElibConfigNotifications() {
    }

    /** Retained for binary compatibility; saving no longer shows a notification. */
    @Deprecated
    public static void saved() {
    }

    /** Shows Elib's optional shared reset toast, if enabled in Elib settings. */
    public static void reset() {
        show(RESET, "toast.elib.reset");
    }

    /**
     * Retained for binary compatibility; saving no longer shows a notification.
     *
     * @param ignoredModName ignored
     */
    @Deprecated
    public static void saved(Component ignoredModName) {
    }

    /**
     * Kept for mods built against the earlier Elib API.
     *
     * @param ignoredModName ignored
     */
    @Deprecated
    public static void reset(Component ignoredModName) {
        reset();
    }

    private static void show(SystemToast.SystemToastId id, String textKey) {
        if (!ElibSettingsManager.get().configNotifications) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        SystemToast.addOrUpdate(minecraft.gui.toastManager(), id,
            Component.translatable(textKey), null);
    }
}
