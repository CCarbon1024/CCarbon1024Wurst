/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.common.extensions.IForgeWorld;
import net.wurstclient.hacks.NoWeatherHack;
import net.wurstclient.mixinterface.IWorldWurst;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.wurstclient.WurstClient;

import javax.annotation.Nullable;

@Mixin(World.class)
public abstract class WorldMixin implements IWorld, AutoCloseable, IWorldWurst
{
    @Shadow
    @Final
    protected List<TileEntity> loadedTileEntityList;

    @Shadow public abstract DimensionType getDimensionType();

    @Shadow public abstract IWorldInfo getWorldInfo();

    @Inject(at = {@At("HEAD")},
            method = {"getRainStrength(F)F"},
            cancellable = true)
    private void onGetRainGradient(float f, CallbackInfoReturnable<Float> cir)
    {
        if(WurstClient.INSTANCE.getHax().noWeatherHack.isRainDisabled())
            cir.setReturnValue(0F);
    }

    @Override
    public float func_242415_f(float tickDelta)
    {
        NoWeatherHack noWeatherHack =
                WurstClient.INSTANCE.getHax().noWeatherHack;

        long timeOfDay =
                noWeatherHack.isTimeChanged() ? noWeatherHack.getChangedTime()
                        : getWorldInfo().getDayTime();

        return getDimensionType().getCelestrialAngleByTime(timeOfDay);
    }

    @Override
    public int getMoonPhase()
    {
        NoWeatherHack noWeatherHack =
                WurstClient.INSTANCE.getHax().noWeatherHack;

        if(noWeatherHack.isMoonPhaseChanged())
            return noWeatherHack.getChangedMoonPhase();

        return getDimensionType().getMoonPhase(func_241851_ab());
    }

    @Override
    public List<TileEntity> getBlockEntityTickers()
    {
        return loadedTileEntityList;
    }

    @Override
    public Stream<VoxelShape> getBlockCollisionsStream(@Nullable Entity entity,
                                                       AxisAlignedBB box)
    {
        return StreamSupport
                .stream(getBlockCollisionShapes(entity, box).spliterator(), false);
    }
}
