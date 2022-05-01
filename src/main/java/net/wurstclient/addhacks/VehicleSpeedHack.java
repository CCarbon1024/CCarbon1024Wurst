/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.addhacks;

import net.minecraft.entity.Entity;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags({"VehicleSpeed", "Speed", "Vehicle"})
public final class VehicleSpeedHack extends Hack implements UpdateListener {

    private final SliderSetting Speed =
            new SliderSetting("Move speed", "Horizontal movement factor.", 2, 1,
                    10, 0.01, SliderSetting.ValueDisplay.DECIMAL);

    private final SliderSetting fallSpeed = new SliderSetting("Fall speed",
            0.1, 0, 1, 0.005,
            v -> v == 0 ? "disabled" : SliderSetting.ValueDisplay.DECIMAL.getValueString(v));

    public VehicleSpeedHack() {
        super("VehicleSpeed", "A better Boat Fly");
        setCategory(Category.MOVEMENT);
        addSetting(fallSpeed);
        addSetting(Speed);
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

        float yaw = MC.player.rotationYaw;
        MC.player.getRidingEntity().rotationYaw = yaw;

        // speed
        Entity vehicle = MC.player.getRidingEntity();
        double motionY = MC.gameSettings.keyBindJump.isKeyDown() ? 0.3 : 0;
        vehicle.setVelocity(Speed.getValue() * vehicle.getMotion().getX(),
                -fallSpeed.getValue() + motionY,
                Speed.getValue() * vehicle.getMotion().getZ());
    }
}
