/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.addhacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"levitation","AntiLevitation"})
public final class AntiLevitationHack extends Hack
{
	private final CheckboxSetting applyGravity =
		new CheckboxSetting("Gravity",
				"Applies gravity.",
				true);

	public AntiLevitationHack()
	{
		super("AntiLevitation",
			"Prevents the levitation effect from working.");
		setCategory(Category.MOVEMENT);
		addSetting(applyGravity);
	}
	public boolean isApplyGravity() {
		return applyGravity.isChecked();
	}

	//see LivingEntityMixin.travelHasStatusEffectProxy and
	//LivingEntity.travelHasNoGravityProxy
}
