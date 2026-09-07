package com.entropy.tacz_turrets.client.renderer;

import com.entropy.tacz_turrets.client.model.TurretItemModel;
import com.entropy.tacz_turrets.item.TurretItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TurretItemRenderer extends GeoItemRenderer<TurretItem> {
    public TurretItemRenderer() {
        super(new TurretItemModel());
    }
}
