package me.gamercoder215.mobchip.abstraction.v1_18_R1;

import com.google.common.collect.ImmutableList;
import me.gamercoder215.mobchip.abstraction.ChipUtil1_18_R1;
import me.gamercoder215.mobchip.ai.schedule.Activity;
import me.gamercoder215.mobchip.ai.schedule.EntityScheduleManager;
import me.gamercoder215.mobchip.ai.schedule.Schedule;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public final class EntityScheduleManager1_18_R1 implements EntityScheduleManager {

    private final net.minecraft.world.entity.Mob nmsMob;
    private final Mob m;

    public EntityScheduleManager1_18_R1(Mob m) {
        this.m = m;
        this.nmsMob = ChipUtil1_18_R1.toNMS(m);
    }


    @Override
    public @Nullable Schedule getCurrentSchedule() {
        return ChipUtil1_18_R1.fromNMS(nmsMob.getBrain().getSchedule());
    }

    @Override
    public void setSchedule(@NotNull Schedule s) {
        nmsMob.getBrain().setSchedule(ChipUtil1_18_R1.toNMS(s));
    }

    @Override
    public @NotNull Set<Activity> getActiveActivities() {
        return nmsMob.getBrain().getActiveActivities().stream().map(ChipUtil1_18_R1::fromNMS).collect(Collectors.toSet());
    }

    @Override
    public void setDefaultActivity(@NotNull Activity a) {
        nmsMob.getBrain().setDefaultActivity(ChipUtil1_18_R1.toNMS(a));
    }

    @Override
    public void useDefaultActivity() {
        nmsMob.getBrain().useDefaultActivity();
    }

    @Override
    public void setRunningActivity(@NotNull Activity a) {
        nmsMob.getBrain().setActiveActivityIfPossible(ChipUtil1_18_R1.toNMS(a));
    }

    @Override
    public @Nullable Activity getRunningActivity() {
        return nmsMob.getBrain().getActiveNonCoreActivity().isPresent() ? ChipUtil1_18_R1.fromNMS(nmsMob.getBrain().getActiveNonCoreActivity().get()) : null;
    }

    @Override
    public boolean isRunning(@NotNull Activity a) {
        return nmsMob.getBrain().isActive(ChipUtil1_18_R1.toNMS(a));
    }

    @Override
    public int size() {
        return nmsMob.getBrain().getRunningBehaviors().size();
    }

    @Override
    public boolean isEmpty() {
        return nmsMob.getBrain().getRunningBehaviors().isEmpty();
    }

    @Nullable
    @Override
    public Consumer<Mob> put(@NotNull Activity key, Consumer<Mob> value) {
        nmsMob.getBrain().addActivity(ChipUtil1_18_R1.toNMS(key), 0, ImmutableList.of(ChipUtil1_18_R1.toNMS(value)));
        return value;
    }

    @Override
    public void clear() {
        nmsMob.getBrain().removeAllBehaviors();
    }

}
