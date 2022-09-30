package me.gamercoder215.mobchip.abstraction.v1_15_R1;

import com.google.common.base.Preconditions;
import me.gamercoder215.mobchip.ai.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_15_R1.attribute.CraftAttributeInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class AttributeInstance1_15_R1 implements me.gamercoder215.mobchip.ai.attribute.AttributeInstance {

    private final net.minecraft.server.v1_15_R1.AttributeInstance handle;
    private final Attribute a;

    public AttributeInstance1_15_R1(Attribute a, net.minecraft.server.v1_15_R1.AttributeInstance handle) {
        this.a = a;
        this.handle = handle;
    }

    @Override
    public @NotNull Attribute getGenericAttribute() {
        return this.a;
    }

    @Override
    public double getBaseValue() {
        return handle.getBaseValue();
    }

    @Override
    public void setBaseValue(double v) {
        handle.setValue(v);
    }

    @NotNull
    @Override
    public Collection<AttributeModifier> getModifiers() {
        return handle.getModifiers().stream().map(CraftAttributeInstance::convert).collect(Collectors.toSet());
    }

    @Override
    public void addModifier(@NotNull AttributeModifier mod) {
        Preconditions.checkArgument(mod != null, "modifier");
        handle.addModifier(CraftAttributeInstance.convert(mod));
    }

    @Override
    public void removeModifier(@NotNull AttributeModifier mod) {
        Preconditions.checkArgument(mod != null, "modifier");
        handle.removeModifier(CraftAttributeInstance.convert(mod));
    }

    @Override
    public double getValue() {
        return handle.getValue();
    }

    @Override
    public double getDefaultValue() {
        return handle.getAttribute().getDefault();
    }
}
