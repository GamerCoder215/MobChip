package me.gamercoder215.mobchip.bukkit.events.pathfinder;

import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.Pathfinder;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a Pathfinder is removed
 */
public class PathfinderRemoveEvent extends PathfinderEvent {

    /**
     * Construct a PathfinderRemoveEvent.
     * @param ai EntityAI involved
     * @param pathfinder Pathfinder involved
     * @param target true if AI is target, else false
     */
    public PathfinderRemoveEvent(@Nullable EntityAI ai, @Nullable Pathfinder pathfinder, boolean target) {
        super(ai, pathfinder, target);
    }
}
