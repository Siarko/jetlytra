package pl.siarko.jetlytra.client.hud.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import pl.siarko.jetlytra.config.JetlytraClientConfig;

public class WarningLevelSlider extends AbstractSliderButton
{
    public WarningLevelSlider(int x, int y, int w, int h, int initialValue) {
        super(x, y, w, h, label(initialValue), initialValue / 100.0);
    }

    private static Component label(int value) {
        return Component.translatable("config.jetlytra.fuel_warning_level", value);
    }

    @Override
    protected void updateMessage() {
        int v = currentValue();
        setMessage(label(v));
    }

    @Override
    protected void applyValue() {
        int v = currentValue();
        JetlytraClientConfig.FUEL_WARNING_LEVEL.set(v);
        JetlytraClientConfig.SPEC.save();
    }

    private int currentValue() {
        return (int)Math.round(value * 100);
    }
}
