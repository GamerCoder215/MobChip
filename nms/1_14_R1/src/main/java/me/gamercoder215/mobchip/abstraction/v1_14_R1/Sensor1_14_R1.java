package me.gamercoder215.mobchip.abstraction.v1_14_R1;

import me.gamercoder215.mobchip.abstraction.ChipUtil1_14_R1;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.MemoryModuleType;
import net.minecraft.server.v1_14_R1.Sensor;
import net.minecraft.server.v1_14_R1.WorldServer;

import java.util.Set;
import java.util.stream.Collectors;

public final class Sensor1_14_R1 extends Sensor<EntityLiving> {

    private final me.gamercoder215.mobchip.ai.sensing.Sensor<?> s;

    public Sensor1_14_R1(me.gamercoder215.mobchip.ai.sensing.Sensor<?> s) {
        this.s = s;
    }

    @Override
    protected void a(WorldServer level, EntityLiving en) {
        s.run(ChipUtil1_14_R1.fromNMS(level), ChipUtil1_14_R1.fromNMS(en));
    }

    @Override
    public Set<MemoryModuleType<?>> a() {
        return s.required().stream().map(ChipUtil1_14_R1::toNMS).collect(Collectors.toSet());
    }

    public me.gamercoder215.mobchip.ai.sensing.Sensor<?> getSensor() {
        return s;
    }

}
