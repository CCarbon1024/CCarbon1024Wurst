/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.settings.KeyBinding;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags({"auto walk"})
public final class AutoWalkHack extends Hack implements UpdateListener
{
    public AutoWalkHack()
    {
        super("AutoWalk","Makes you walk automatically.");
        setCategory(Category.MOVEMENT);
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

        KeyBinding forwardKey = MC.gameSettings.keyBindForward;
        forwardKey.setPressed(false);
    }

    @Override
    public void onUpdate()
    {
        MC.gameSettings.keyBindForward.setPressed(true);
    }
}
