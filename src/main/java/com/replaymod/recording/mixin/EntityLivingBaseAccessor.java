package com.replaymod.recording.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nonnull;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface EntityLivingBaseAccessor {
    //#if MC>=10904
    @Accessor("DATA_LIVING_ENTITY_FLAGS")
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    static EntityDataAccessor<Byte> getLivingFlags() { return null; }
    //#endif
}
