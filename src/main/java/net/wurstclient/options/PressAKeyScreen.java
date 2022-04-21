/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;


public class PressAKeyScreen extends Screen
{
	private PressAKeyCallback prevScreen;
	
	public PressAKeyScreen(PressAKeyCallback prevScreen)
	{
		super(new StringTextComponent(""));
		
		if(!(prevScreen instanceof Screen))
			throw new IllegalArgumentException("prevScreen is not a screen");
		
		this.prevScreen = prevScreen;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int int_3)
	{
		if(keyCode != GLFW.GLFW_KEY_ESCAPE)
			prevScreen.setKey(getKeyName(keyCode, scanCode));
		
		minecraft.displayGuiScreen((Screen)prevScreen);
		return super.keyPressed(keyCode, scanCode, int_3);
	}
	
	private String getKeyName(int keyCode, int scanCode)
	{
		return InputMappings.getInputByCode(keyCode, scanCode).getTranslationKey();
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY,
					   float partialTicks)
	{
		renderBackground(matrixStack);
		drawCenteredString(matrixStack, font, "Press a key", width / 2,
			height / 4 + 48, 16777215);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
