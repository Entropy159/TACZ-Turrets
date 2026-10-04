package com.entropy.tacz_turrets.util;

import net.minecraft.util.Mth;
//? if forge {
import net.minecraftforge.energy.EnergyStorage;
//?} else {
/*import net.neoforged.neoforge.energy.EnergyStorage;
*///?}

public class TurretEnergyStorage extends EnergyStorage {
    public TurretEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0);
    }

    public void consume(int amount) {
        energy = Math.max(0, energy - amount);
    }

    public void setEnergy(int amount) {
        energy = Mth.clamp(amount, 0, capacity);
    }
}
