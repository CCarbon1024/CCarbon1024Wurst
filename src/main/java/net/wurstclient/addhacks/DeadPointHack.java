/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.addhacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.DeathListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.ChatUtils;

@SearchTags({"Death"})
public final class DeadPointHack extends Hack implements DeathListener {

    public DeadPointHack() {
        super("DeathPoint", "Show your Death Point");
        setCategory(Category.CHAT);
    }

    @Override
    public void onEnable() {
        EVENTS.add(DeathListener.class, this);
    }

    @Override
    public void onDisable() {
        EVENTS.remove(DeathListener.class, this);
    }

    @Override
    public void onDeath() {
        ChatUtils.message("Your death point at (\u00a7l"
                + (int) MC.player.getPosX() + ","
                + (int) MC.player.getPosY() + ","
                + (int) MC.player.getPosZ() + "\u00a7r). ");
    }
}
