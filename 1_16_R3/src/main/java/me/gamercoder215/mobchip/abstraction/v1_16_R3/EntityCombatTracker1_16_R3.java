package me.gamercoder215.mobchip.abstraction.v1_16_R3;

import me.gamercoder215.mobchip.abstraction.ChipUtil1_16_R3;
import me.gamercoder215.mobchip.combat.CombatEntry;
import me.gamercoder215.mobchip.combat.EntityCombatTracker;
import net.minecraft.server.v1_16_R3.CombatTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class EntityCombatTracker1_16_R3 implements EntityCombatTracker {

    private final CombatTracker handle;
    private final Mob m;

    public EntityCombatTracker1_16_R3(Mob m) {
        this.m = m;
        this.handle = ChipUtil1_16_R3.toNMS(m).getCombatTracker();
    }

    @Override
    public @NotNull String getCurrentDeathMessage() {
        return handle.getDeathMessage().getString();
    }

    @Override
    public @Nullable CombatEntry getLatestEntry() {
        List<CombatEntry> l = getCombatEntries();
        return l.isEmpty() ? null : l.get(l.size() - 1);
    }

    @Override
    public @NotNull List<CombatEntry> getCombatEntries() {
        List<CombatEntry> entries = new ArrayList<>();
        try {
            Field f = CombatTracker.class.getDeclaredField("a");
            f.setAccessible(true);
            ((List<net.minecraft.server.v1_16_R3.CombatEntry>) f.get(handle)).stream().map(en -> ChipUtil1_16_R3.fromNMS(m, en)).forEach(entries::add);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getClass().getSimpleName());
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
        }
        return entries;
    }

    @Override
    public void recordEntry(@NotNull CombatEntry entry) {
        if (entry == null) return;
        try {
            Field f = CombatTracker.class.getDeclaredField("c");
            f.setAccessible(true);
            Object entries = f.get(handle);

            Method m = List.class.getMethod("add", Object.class);
            m.invoke(entries, ChipUtil1_16_R3.toNMS(entry));
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getClass().getSimpleName());
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
        }
    }

    @Override
    public int getCombatDuration() {
        return handle.f();
    }

    @Override
    public boolean isTakingDamage() {
        handle.g();
        try {
            Field damage = CombatTracker.class.getDeclaredField("g");
            damage.setAccessible(true);
            return damage.getBoolean(handle);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getClass().getSimpleName());
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
        }
        return false;
    }

    @Override
    public boolean isInCombat() {
        handle.g();
        try {
            Field combat = CombatTracker.class.getDeclaredField("f");
            combat.setAccessible(true);
            return combat.getBoolean(handle);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getClass().getSimpleName());
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
        }
        return false;
    }

    @Override
    public boolean hasLastDamageCancelled() {
        return ChipUtil1_16_R3.toNMS(m).forceExplosionKnockback;
    }
}
