package me.gamercoder215.mobchip.bukkit;

import me.gamercoder215.mobchip.ai.behavior.BehaviorResult;
import me.gamercoder215.mobchip.ai.behavior.VillagerBehavior;
import me.gamercoder215.mobchip.ai.memories.EntityMemory;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

class BukkitVillagerBehavior extends BukkitCreatureBehavior implements VillagerBehavior {

    final Villager m;

    BukkitVillagerBehavior(Villager v) {
        super(v);
        this.m = v;
    }

    @Override
    public @NotNull BehaviorResult harvestFarmland() {
        return run("BehaviorFarm");
    }

    @Override
    public @NotNull BehaviorResult showTrades(int minDuration, int maxDuration) {
        return run("BehaviorTradePlayer", minDuration, maxDuration);
    }

    @Override
    public @NotNull BehaviorResult resetProfession() {
        wrapper.removeMemory(m, EntityMemory.JOB_SITE);

        return run("BehaviorProfession");
    }

    @Override
    public @NotNull BehaviorResult giftHero(int duration) {
        return run("BehaviorVillageHeroGift", duration);
    }

    @Override
    public @NotNull BehaviorResult celebrateSurvivedRaid(int minDuration, int maxDuration) {
        return run("BehaviorCelebrate", minDuration, maxDuration);
    }

    @Override
    public @NotNull BehaviorResult findJobSite(float speedMod) {
        return run("BehaviorPotentialJobSite", speedMod);
    }

    @Override
    public @NotNull BehaviorResult findNearestVillage(int minDistance, float speedMod) {
        wrapper.removeMemory(m, EntityMemory.WALKING_TARGET);

        return run("BehaviorNearestVillage", speedMod, minDistance);
    }

    @Override
    public @NotNull BehaviorResult workAtJob() {
        return run("BehaviorWork");
    }

    @Override
    public @NotNull BehaviorResult useBonemeal() {
        wrapper.removeMemory(m, EntityMemory.WALKING_TARGET);
        wrapper.removeMemory(m, EntityMemory.LOOKING_TARGET);

        return run("BehaviorBonemeal");
    }
}
