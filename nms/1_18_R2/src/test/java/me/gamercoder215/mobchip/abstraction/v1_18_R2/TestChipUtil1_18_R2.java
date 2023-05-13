package me.gamercoder215.mobchip.abstraction.v1_18_R2;

import me.gamercoder215.mobchip.abstraction.v1_18_R2.ChipUtil1_18_R2;
import org.bukkit.Difficulty;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestChipUtil1_18_R2 {

    @Test
    @DisplayName("Test Bukkit-NMS Conversion")
    public void testNMSConversion() {
        // Classes
        ChipUtil1_18_R2.toNMS(LivingEntity.class);
        ChipUtil1_18_R2.toNMS(Mob.class);
        ChipUtil1_18_R2.toNMS(AbstractVillager.class);
        ChipUtil1_18_R2.toNMS(AbstractHorse.class);

        ChipUtil1_18_R2.toNMS(IronGolem.class);
        ChipUtil1_18_R2.toNMS(Chicken.class);
        ChipUtil1_18_R2.toNMS(Cow.class);

        ChipUtil1_18_R2.toNMS(Player.class);
        ChipUtil1_18_R2.toNMS(HumanEntity.class);

        // Other
        for (Difficulty d : Difficulty.values()) Assertions.assertNotNull(ChipUtil1_18_R2.toNMS(d));
        for (EntityDamageEvent.DamageCause c : EntityDamageEvent.DamageCause.values()) Assertions.assertNotNull(ChipUtil1_18_R2.toNMS(c));
    }



}