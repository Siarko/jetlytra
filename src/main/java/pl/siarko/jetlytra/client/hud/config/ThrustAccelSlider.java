package pl.siarko.jetlytra.client.hud.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ThrustAccelSlider extends AbstractSliderButton {

    private final double min;
    private final double max;
    private final Consumer<Double> onChange;

    public ThrustAccelSlider(int x, int y, int w, int h, double min, double max, double initial, Consumer<Double> onChange) {
        super(x, y, w, h, Component.empty(), (initial - min) / (max - min));
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(String.format("%.2f", min + value * (max - min))));
    }

    @Override
    protected void applyValue() {
        onChange.accept(min + value * (max - min));
    }
}
