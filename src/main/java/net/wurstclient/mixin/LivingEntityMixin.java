/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package net.wurstclient.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "travel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isPotionActive(Lnet/minecraft/potion/Effect;)Z"))
    private boolean travelHasStatusEffectProxy(LivingEntity self, Effect statusEffect) {
        if (statusEffect == Effects.LEVITATION && WurstClient.INSTANCE.getHax().antiLevitationHack.isEnabled()) {
            return false;
        }
        return self.isPotionActive(statusEffect);
    }

    @Redirect(method = "travel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;hasNoGravity()Z"))
    private boolean travelHasNoGravityProxy(LivingEntity self) {
        if (self.isPotionActive(Effects.LEVITATION) && WurstClient.INSTANCE.getHax().antiLevitationHack.isEnabled()) {
            return !WurstClient.INSTANCE.getHax().antiLevitationHack.isApplyGravity();
        }
        return self.hasNoGravity();
    }

}
