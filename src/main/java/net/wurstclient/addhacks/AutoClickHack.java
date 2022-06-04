/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.addhacks;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Hand;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.DeathListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;

@SearchTags({"Click"})
public final class AutoClickHack extends Hack implements UpdateListener {

    private final CheckboxSetting MainHand = new CheckboxSetting(
            "main hand", "If true use main hand.", true);

    public AutoClickHack() {
        super("AutoClick", "Just auto click");
        setCategory(Category.OTHER);

    }

    @Override
    public void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    public void onDisable() {
        EVENTS.remove(UpdateListener.class, this);

        KeyBinding forwardKey = MC.gameSettings.keyBindUseItem;
        forwardKey.setPressed(false);
    }

    @Override
    public void onUpdate() {
        MC.gameSettings.keyBindUseItem.setPressed(true);
        MC.playerController.processRightClick(MC.player, MC.world,
                MainHand.isChecked() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }
}
