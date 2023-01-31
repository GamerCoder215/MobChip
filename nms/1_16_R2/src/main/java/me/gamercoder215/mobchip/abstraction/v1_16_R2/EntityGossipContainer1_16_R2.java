package me.gamercoder215.mobchip.abstraction.v1_16_R2;

import me.gamercoder215.mobchip.abstraction.ChipUtil;
import me.gamercoder215.mobchip.abstraction.ChipUtil1_16_R2;
import me.gamercoder215.mobchip.ai.gossip.EntityGossipContainer;
import me.gamercoder215.mobchip.ai.gossip.GossipType;
import net.minecraft.server.v1_16_R2.Reputation;
import net.minecraft.server.v1_16_R2.ReputationType;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class EntityGossipContainer1_16_R2 implements EntityGossipContainer {
    private final Reputation handle;
    private final Villager entity;

    public EntityGossipContainer1_16_R2(Villager v) {
        this.entity = v;
        this.handle = ((CraftVillager) v).getHandle().fj();
    }

    @Override
    public @NotNull Villager getEntity() {
        return entity;
    }

    @Override
    public void decay() {
        handle.b();
    }

    @Override
    public int getReputation(@NotNull Entity en, @Nullable GossipType... types) throws IllegalArgumentException {
        return handle.a(en.getUniqueId(), g -> Arrays.asList(types).contains(ChipUtil1_16_R2.fromNMS(g)));
    }

    @Override
    public void put(@NotNull Entity en, @NotNull GossipType type, int maxCap) throws IllegalArgumentException {
        handle.a(en.getUniqueId(), ChipUtil1_16_R2.toNMS(type), maxCap);
    }

    @Override
    public void remove(@NotNull Entity en, @NotNull GossipType type) throws IllegalArgumentException {
        try {
            Field map = Reputation.class.getDeclaredField("a");
            map.setAccessible(true);
            Map<UUID, ?> data = new HashMap<>((Map<UUID, ?>) map.get(handle));
            data.remove(en.getUniqueId());
            map.set(handle, data);
        } catch (Exception e) {
            ChipUtil.printStackTrace(e);
        }
    }

    @Override
    public void removeAll(@NotNull GossipType type) throws IllegalArgumentException {
        try {
            Field map = Reputation.class.getDeclaredField("a");
            map.setAccessible(true);
            Map<UUID, ?> data = new HashMap<>((Map<UUID, ?>) map.get(handle));

            for (UUID uuid : data.keySet()) {
                Object o = data.get(uuid);

                Method m = o.getClass().getDeclaredMethod("b", ReputationType.class);
                m.setAccessible(true);
                m.invoke(o, ChipUtil1_16_R2.toNMS(type));

                Method empty = o.getClass().getDeclaredMethod("b");
                empty.setAccessible(true);
                if ((boolean) empty.invoke(o)) data.remove(uuid);
            }
            map.set(handle, data);
        } catch (Exception e) {
            ChipUtil.printStackTrace(e);
        }
    }
}
