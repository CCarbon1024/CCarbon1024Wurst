/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.addhacks;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags({"VehicleSpeed", "Speed", "Vehicle"})
public final class VehicleSpeedHack extends Hack implements UpdateListener {

    private final SliderSetting Speed =
            new SliderSetting("Move speed", "Horizontal movement factor.", 10, 0,
                    50, 0.1, SliderSetting.ValueDisplay.DECIMAL);

    private final SliderSetting verticalSpeed =
            new SliderSetting("vertical-speed", "Vertical speed in blocks.", 6, 0,
                    20, 0.01, SliderSetting.ValueDisplay.DECIMAL);

    private final SliderSetting fallSpeed = new SliderSetting("Fall speed",
            0.1, 0, 1, 0.005,
            v -> v == 0 ? "disabled" : SliderSetting.ValueDisplay.DECIMAL.getValueString(v));

    public VehicleSpeedHack() {
        super("VehicleSpeed", "A better Boat Fly");
        setCategory(Category.MOVEMENT);
        addSetting(verticalSpeed);
        addSetting(Speed);
        addSetting(fallSpeed);
    }

    @Override
    public void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    public void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        // check if in boat
        if (MC.player.getRidingEntity() == null) {
            return;
        }

        MC.player.getRidingEntity().rotationYaw = MC.player.rotationYaw;
        Entity vehicle = MC.player.getRidingEntity();
        Vector3d vel = vehicle.getMotion();

        double motionX = MC.gameSettings.keyBindForward.isKeyDown() ? Speed.getValue() / 20 * vel.getX() : 0;
        double motionZ = MC.gameSettings.keyBindForward.isKeyDown() ? Speed.getValue() / 20 * vel.getZ() : 0;

        double motionY = 0;
        if (MC.gameSettings.keyBindJump.isPressed()) motionY += verticalSpeed.getValue() / 20;
        if (MC.gameSettings.keyBindSneak.isPressed()) motionY -= verticalSpeed.getValue() / 20;
        else motionY -= fallSpeed.getValue() / 20;

        // speed
        vehicle.setVelocity(motionX, motionY, motionZ);
    }
}
