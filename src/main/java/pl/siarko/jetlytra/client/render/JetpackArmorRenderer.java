package pl.siarko.jetlytra.client.render;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import pl.siarko.jetlytra.item.JetlytraItem;

public class JetpackArmorRenderer extends GeoArmorRenderer<JetlytraItem> {

    public JetpackArmorRenderer() {
        super(new JetpackGeoModel());
    }
}
