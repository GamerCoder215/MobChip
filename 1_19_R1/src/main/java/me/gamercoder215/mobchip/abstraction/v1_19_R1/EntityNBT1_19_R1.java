package me.gamercoder215.mobchip.abstraction.v1_19_R1;

import me.gamercoder215.mobchip.abstraction.ChipUtil1_19_R1;
import me.gamercoder215.mobchip.nbt.EntityNBT;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public final class EntityNBT1_19_R1 extends NBTSection1_19_R1 implements EntityNBT {

    private final Mob mob;
    private final net.minecraft.world.entity.Mob handle;

    private final CompoundTag root;

    public EntityNBT1_19_R1(Mob m) {
        super(m);
        this.mob = m;
        this.handle = ChipUtil1_19_R1.toNMS(m);
        this.root = new CompoundTag();
        handle.saveWithoutId(root);
    }

    @Override
    public @NotNull Mob getEntity() {
        return mob;
    }
}
