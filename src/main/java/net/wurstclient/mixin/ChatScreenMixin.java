/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen
{
    @Shadow
    protected TextFieldWidget inputField;

    private ChatScreenMixin(WurstClient wurst, ITextComponent text_1)
    {
        super(text_1);
    }

    @Inject(at = {@At("TAIL")}, method = {"init()V"})
    protected void onInit(CallbackInfo ci)
    {
        if(WurstClient.INSTANCE.getHax().infiniChatHack.isEnabled())
            inputField.setMaxStringLength(Integer.MAX_VALUE);
    }
}
