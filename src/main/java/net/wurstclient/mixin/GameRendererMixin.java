/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.GameSettings;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent;
import net.wurstclient.events.HitResultRayTraceListener.HitResultRayTraceEvent;
import net.wurstclient.events.RenderListener.RenderEvent;
import net.wurstclient.hacks.FullbrightHack;
import net.wurstclient.mixinterface.IGameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
	implements AutoCloseable, IResourceManagerReloadListener, IGameRenderer
{

	@Redirect(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/GameRenderer;applyBobbing(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V",
		ordinal = 0),
		method = {
			"renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V"})
	private void onRenderWorldViewBobbing(GameRenderer gameRenderer,
		MatrixStack matrixStack, float partalTicks)
	{
		CameraTransformViewBobbingEvent event =
			new CameraTransformViewBobbingEvent();
		EventManager.fire(event);
		
		if(event.isCancelled())
			return;

		applyBobbing(matrixStack, partalTicks);
	}

	@Inject(
		at = {@At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z",
			opcode = Opcodes.GETFIELD,
			ordinal = 0)},
		method = {
			"renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V"})
	private void onRenderWorld(float partialTicks, long finishTimeNano,
		MatrixStack matrixStack, CallbackInfo ci)
	{
		RenderEvent event = new RenderEvent(partialTicks);
		EventManager.fire(event);
	}

	@Redirect(
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/GameSettings;fov:D",
			opcode = Opcodes.GETFIELD,
			ordinal = 0),
		method = {"getFOVModifier(Lnet/minecraft/client/renderer/ActiveRenderInfo;FZ)D"})
	private double getFov(GameSettings options)
	{
		return WurstClient.INSTANCE.getOtfs().zoomOtf
			.changeFovBasedOnZoom(options.fov);
	}

	@Inject(at = {@At(value = "INVOKE",
		target = "Lnet/minecraft/entity/Entity;getEyePosition(F)Lnet/minecraft/util/math/vector/Vector3d;",
		opcode = Opcodes.INVOKEVIRTUAL,
		ordinal = 0)}, method = {"getMouseOver(F)V"})
	private void onHitResultRayTrace(float float_1, CallbackInfo ci)
	{
		HitResultRayTraceEvent event = new HitResultRayTraceEvent(float_1);
		EventManager.fire(event);
	}


	@Redirect(
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F",
			ordinal = 0),
		method = {
			"renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V"})
	private float wurstNauseaLerp(float delta, float first, float second)
	{
		if(!WurstClient.INSTANCE.getHax().antiWobbleHack.isEnabled())
			return MathHelper.lerp(delta, first, second);
		
		return 0;
	}

	/*


	@Inject(at = {@At("HEAD")},
		method = {
			"bobViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"},
		cancellable = true)
	private void onBobViewWhenHurt(MatrixStack matrixStack, float f,
		CallbackInfo ci)
	{
		if(WurstClient.INSTANCE.getHax().noHurtcamHack.isEnabled())
			ci.cancel();
	}*/

	@Inject(at = {@At("HEAD")},
			method = {
					"getNightVisionBrightness(Lnet/minecraft/entity/LivingEntity;F)F"},
			cancellable = true)
	private static void onGetNightVisionStrength(LivingEntity livingEntity,
												 float f, CallbackInfoReturnable<Float> cir)
	{
		FullbrightHack fullbright =
				WurstClient.INSTANCE.getHax().fullbrightHack;

		if(fullbright.isNightVisionActive())
			cir.setReturnValue(fullbright.getNightVisionStrength());
	}

	@Shadow
	private void applyBobbing(MatrixStack matrixStack, float partalTicks)
	{
		
	}
	
	@Override
	public void loadWurstShader(ResourceLocation identifier)
	{
		loadShader(identifier);
	}
	
	@Shadow
	private void loadShader(ResourceLocation identifier)
	{
		
	}
}
