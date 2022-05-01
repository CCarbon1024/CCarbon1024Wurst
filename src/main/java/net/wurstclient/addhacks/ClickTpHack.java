/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.addhacks;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags({"BoatFlight", "boat fly", "boat flight"})
public final class ClickTpHack extends Hack implements UpdateListener
{
    private final SliderSetting maxDistance =
            new SliderSetting("max-distance", "The maximum distance you can teleport.", 5, 1,
                    10, 1, SliderSetting.ValueDisplay.PERCENTAGE);

    public ClickTpHack()
    {
        super("ClickTp","Teleports you to the block you click on.");
        setCategory(Category.MOVEMENT);
        addSetting(maxDistance);
    }

    @Override
    public void onEnable()
    {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    public void onDisable()
    {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate()
    {

        if(MC.objectMouseOver.getType() == RayTraceResult.Type.BLOCK
                || MC.gameSettings.keyBindUseItem.isPressed()) {
            BlockPos pos = ((BlockRayTraceResult) MC.objectMouseOver).getPos();
            Direction side = ((BlockRayTraceResult) MC.objectMouseOver).getFace();
            BlockState state = MC.world.getBlockState(pos);

            VoxelShape shape = state.getCollisionShape(MC.world, pos);
            if (shape.isEmpty()) shape = state.getShape(MC.world, pos);

            double height = shape.isEmpty() ? 1 : shape.getStart(Direction.Axis.Y);
            MC.player.setPosition(pos.getX() + 0.5 + side.getXOffset(),
                    pos.getY() + height,
                    pos.getZ() + 0.5 + side.getYOffset());

        }
    }
}
