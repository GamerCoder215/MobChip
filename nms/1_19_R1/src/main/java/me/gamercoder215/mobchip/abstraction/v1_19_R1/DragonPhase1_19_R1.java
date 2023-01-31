package me.gamercoder215.mobchip.abstraction.v1_19_R1;

import me.gamercoder215.mobchip.abstraction.ChipUtil1_19_R1;
import me.gamercoder215.mobchip.ai.enderdragon.DragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderDragon;
import org.jetbrains.annotations.NotNull;

public final class DragonPhase1_19_R1 implements DragonPhase {

    private final EnderDragon dragon;
    private final DragonPhaseInstance handle;

    public DragonPhase1_19_R1(EnderDragon dragon, DragonPhaseInstance handle) {
        this.dragon = dragon;
        this.handle = handle;
    }

    @Override
    public @NotNull EnderDragon getDragon() {
        return this.dragon;
    }

    @Override
    public @NotNull Location getTargetLocation() {
        return ChipUtil1_19_R1.fromNMS(handle.getFlyTargetLocation(), dragon.getWorld());
    }

    @Override
    public void start() {
        handle.begin();
    }

    @Override
    public void stop() {
        handle.end();
    }

    @Override
    public void clientTick() {
        handle.doClientTick();
    }

    @Override
    public void serverTick() {
        handle.doServerTick();
    }

    @Override
    public boolean isSitting() {
        return handle.isSitting();
    }

    @Override
    public float getFlyingSpeed() {
        return handle.getFlySpeed();
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return NamespacedKey.minecraft(handle.toString().split(" ")[0].toLowerCase());
    }
}
