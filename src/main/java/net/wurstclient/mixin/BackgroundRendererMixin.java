/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import net.wurstclient.WurstClient;

@Mixin(FogRenderer.class)
public class BackgroundRendererMixin
{
    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;isPotionActive(Lnet/minecraft/potion/Effect;)Z",
            ordinal = 0),
            method = "updateFogColor(Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/world/ClientWorld;IF)V")
    private static boolean hasStatusEffectRender(LivingEntity entity, Effect effect)
    {
        if(effect == Effects.BLINDNESS
                && WurstClient.INSTANCE.getHax().antiBlindHack.isEnabled())
            return false;

        return entity.isPotionActive(effect);
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;isPotionActive(Lnet/minecraft/potion/Effect;)Z",
            ordinal = 1),
            method = "setupFog(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/FogRenderer$FogType;FZF)V",
            require = 0)
    private static boolean hasStatusEffectApplyFog(LivingEntity entity, Effect effect)
    {
        if(effect == Effects.BLINDNESS
                && WurstClient.INSTANCE.getHax().antiBlindHack.isEnabled())
            return false;

        return entity.isPotionActive(effect);
    }
}
