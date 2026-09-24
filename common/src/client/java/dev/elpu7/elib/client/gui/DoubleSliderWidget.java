package dev.elpu7.elib.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/** A range slider whose label and optional tooltip are supplied by the caller. */
public final class DoubleSliderWidget extends AbstractSliderButton {
    private final double min;
    private final double max;
    private final double step;
    private final DoubleFunction<Component> label;
    private final DoubleFunction<Tooltip> tooltip;
    private final DoubleConsumer onChanged;

    public DoubleSliderWidget(
        int x,
        int y,
        int width,
        int height,
        double min,
        double max,
        double step,
        double initialValue,
        DoubleFunction<Component> label,
        DoubleFunction<Tooltip> tooltip,
        DoubleConsumer onChanged
    ) {
        super(x, y, width, height, Component.empty(), normalize(initialValue, min, max));
        if (!Double.isFinite(step) || step < 0.0D) {
            throw new IllegalArgumentException("step must be finite and non-negative");
        }
        this.min = min;
        this.max = max;
        this.step = step;
        this.label = Objects.requireNonNull(label, "label");
        this.tooltip = tooltip;
        this.onChanged = Objects.requireNonNull(onChanged, "onChanged");
        updateMessage();
    }

    public double getActualValue() {
        double actual = min + value * (max - min);
        if (step > 0.0D) {
            actual = min + Math.round((actual - min) / step) * step;
        }
        return Math.clamp(actual, min, max);
    }

    /** Updates the widget after an external reset without calling onChanged. */
    public void syncFromValue(double actualValue) {
        value = normalize(actualValue, min, max);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (label == null) {
            return; // AbstractSliderButton may call this from its constructor.
        }
        double actual = getActualValue();
        setMessage(label.apply(actual));
        if (tooltip != null) {
            setTooltip(tooltip.apply(actual));
        }
    }

    @Override
    protected void applyValue() {
        onChanged.accept(getActualValue());
        updateMessage();
    }

    private static double normalize(double actualValue, double min, double max) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min >= max) {
            throw new IllegalArgumentException("min and max must be finite and min < max");
        }
        if (!Double.isFinite(actualValue)) {
            return 0.0D;
        }
        return Math.clamp((actualValue - min) / (max - min), 0.0D, 1.0D);
    }
}
