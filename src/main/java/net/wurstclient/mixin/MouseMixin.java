/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import net.minecraft.client.MouseHelper;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.MouseScrollListener.MouseScrollEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MouseMixin
{
	@Inject(at = {@At("RETURN")}, method = {"scrollCallback(JDD)V"})
	private void onOnMouseScroll(long long_1, double double_1, double double_2,
		CallbackInfo ci)
	{
		MouseScrollEvent event = new MouseScrollEvent(double_2);
		EventManager.fire(event);
	}
}
