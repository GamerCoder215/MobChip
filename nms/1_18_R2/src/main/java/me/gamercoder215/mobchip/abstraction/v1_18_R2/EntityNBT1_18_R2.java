package me.gamercoder215.mobchip.abstraction.v1_18_R2;

import me.gamercoder215.mobchip.abstraction.ChipUtil1_18_R2;
import me.gamercoder215.mobchip.nbt.EntityNBT;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public final class EntityNBT1_18_R2 extends NBTSection1_18_R2 implements EntityNBT {

    private final Mob mob;
    private final net.minecraft.world.entity.Mob handle;

    private final CompoundTag root;

    public EntityNBT1_18_R2(Mob m) {
        super(m);
        this.mob = m;
        this.handle = ChipUtil1_18_R2.toNMS(m);
        this.root = new CompoundTag();
        handle.saveWithoutId(root);
    }

    @Override
    public @NotNull Mob getEntity() {
        return mob;
    }
}
