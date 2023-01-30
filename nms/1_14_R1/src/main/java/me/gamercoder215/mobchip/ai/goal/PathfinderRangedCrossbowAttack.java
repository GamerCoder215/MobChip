package me.gamercoder215.mobchip.ai.goal;

import me.gamercoder215.mobchip.ai.SpeedModifier;
import org.bukkit.entity.Pillager;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Pathfinder for Any Crossbow-Specific Ranged Monster to shoot arrows from a crossbow
 */
public final class PathfinderRangedCrossbowAttack extends Pathfinder implements SpeedModifier, Ranged {

    private double speedMod;
    private float range;

    /**
     * Constructs a PathfinderRangedCrossbowAttack with the default speed modifier.
     * @param m Pillager to use
     */
    public PathfinderRangedCrossbowAttack(@NotNull Pillager m) {
        this(m, DEFAULT_SPEED_MODIFIER);
    }

    /**
     * Constructs a PathfinderRangedCrossbowAttack with the default range.
     * @param m Pillager to use
     * @param speedMod Speed Modifier while attacking
     */
    public PathfinderRangedCrossbowAttack(@NotNull Pillager m, double speedMod) {
        this(m, speedMod, DEFAULT_ATTACK_RANGE);
    }

    /**
     * Constructs a PathfinderRangedCrossbowAttack.
     * @param m Pillager to use
     * @param speedMod Speed Modifier while attacking
     * @param range Range of attack
     */
    public PathfinderRangedCrossbowAttack(@NotNull Pillager m, double speedMod, float range) {
        super(m);

        this.speedMod = speedMod;
        this.range = range;
    }

    @Override
    public float getRange() {
        return this.range;
    }

    @Override
    public void setRange(float range) {
        this.range = range;
    }

    @Override
    public double getSpeedModifier() {
        return this.speedMod;
    }

    @Override
    public void setSpeedModifier(double mod) {
        this.speedMod = mod;
    }

    @Override
    public @NotNull PathfinderFlag[] getFlags() {
        return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
    }

    @Override
    public String getInternalName() {
        return "PathfinderGoalCrossbowAttack";
    }
}
