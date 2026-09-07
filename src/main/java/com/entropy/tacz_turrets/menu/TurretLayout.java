package com.entropy.tacz_turrets.menu;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;

public class TurretLayout {
    public static final int SLOT = 18;
    public static final int MARGIN = 8;
    public static final int BAR_HEIGHT = 5;
    public static final int BUTTON_HEIGHT = 20;

    public final int rows;
    public final int columns;
    public final int ammoSlots;
    public final int width;
    public final int height;
    public final int barWidth;
    public final int healthBarY;
    public final int energyBarY;
    public final int gunSlotX;
    public final int gunSlotY;
    public final int ammoStartX;
    public final int ammoStartY;
    public final int buttonsY;
    public final int secondButtonRowY;
    public final int inventoryLabelY;
    public final int playerInventoryX;
    public final int playerInventoryY;
    public final int hotbarY;

    public TurretLayout(int rows, int columns) {
        this.rows = Math.max(1, rows);
        this.columns = Math.max(1, columns);
        this.ammoSlots = this.rows * this.columns;

        int ammoWidth = this.columns * SLOT;
        this.width = Math.max(176, ammoWidth + MARGIN * 2 + 6);
        this.barWidth = width - MARGIN * 2;

        this.healthBarY = 18;
        this.energyBarY = healthBarY + BAR_HEIGHT + 3;
        this.gunSlotY = energyBarY + BAR_HEIGHT + 6;
        this.gunSlotX = (width - SLOT) / 2;
        this.ammoStartY = gunSlotY + SLOT + 4;
        this.ammoStartX = (width - ammoWidth) / 2;
        this.buttonsY = ammoStartY + this.rows * SLOT + 5;
        this.secondButtonRowY = buttonsY + BUTTON_HEIGHT + 3;
        this.inventoryLabelY = secondButtonRowY + BUTTON_HEIGHT + 4;
        this.playerInventoryX = (width - 9 * SLOT) / 2;
        this.playerInventoryY = inventoryLabelY + 12;
        this.hotbarY = playerInventoryY + 3 * SLOT + 4;
        this.height = hotbarY + SLOT + 7;
    }

    public static TurretLayout fromConfig() {
        return new TurretLayout(TACZTurretsConfig.turretSlotRows, TACZTurretsConfig.turretSlotLength);
    }

    public int ammoSlotX(int index) {
        return ammoStartX + (index % columns) * SLOT;
    }

    public int ammoSlotY(int index) {
        return ammoStartY + (index / columns) * SLOT;
    }
}
