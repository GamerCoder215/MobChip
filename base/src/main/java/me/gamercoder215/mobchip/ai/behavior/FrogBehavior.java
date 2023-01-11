package me.gamercoder215.mobchip.ai.behavior;

import me.gamercoder215.mobchip.ai.memories.EntityMemory;
import me.gamercoder215.mobchip.ai.schedule.Updatable;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/**
 * Represents Behaviors for a Frog
 */
public interface FrogBehavior extends CreatureBehavior, Updatable {

    /**
     * Makes this frog shoot its tongue.
     * <p>This behavior requires {@link EntityMemory#WALKING_TARGET} and {@link EntityMemory#IS_PANICKING} to be absent, {@link EntityMemory#LOOKING_TARGET} to be registered, and {@link EntityMemory#ATTACK_TARGET} to be present.</p>
     * @param tongueSound Sound to make when tongue is shot
     * @param eatSound Sound to make when a frog eats something
     * @return Result of Behavior
     */
    @NotNull
    BehaviorResult shootTongue(Sound tongueSound, Sound eatSound);

    /**
     * Makes this frog Croak.
     * <p>This behavior requires {@link EntityMemory#WALKING_TARGET} to be absent.</p>
     * @return Result of Behavior
     */
    @NotNull
    BehaviorResult croak();
}
