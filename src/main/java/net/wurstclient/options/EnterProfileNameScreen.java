/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public final class EnterProfileNameScreen extends Screen
{
	private final Screen prevScreen;
	private final Consumer<String> callback;
	
	private TextFieldWidget valueField;
	private Button doneButton;
	
	public EnterProfileNameScreen(Screen prevScreen, Consumer<String> callback)
	{
		super(new StringTextComponent(""));
		this.prevScreen = prevScreen;
		this.callback = callback;
	}
	
	@Override
	public void init()
	{
		int x1 = width / 2 - 100;
		int y1 = 60;
		int y2 = height / 3 * 2;
		
		FontRenderer tr = minecraft.fontRenderer;
		
		valueField =
			new TextFieldWidget(tr, x1, y1, 200, 20, new StringTextComponent(""));
		valueField.setText("");
		valueField.clampCursorPosition(0);
		
		children.add(valueField);
		setFocusedDefault(valueField);
		valueField.setFocused2(true);
		
		doneButton = new Button(x1, y2, 200, 20, new StringTextComponent("Done"),
			b -> done());
		addButton(doneButton);
	}
	
	private void done()
	{
		String value = valueField.getText();
		if(!value.isEmpty())
			callback.accept(value);
		
		minecraft.displayGuiScreen(prevScreen);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int int_3)
	{
		switch(keyCode)
		{
			case GLFW.GLFW_KEY_ENTER:
			done();
			break;
			
			case GLFW.GLFW_KEY_ESCAPE:
			minecraft.displayGuiScreen(prevScreen);
			break;
		}
		
		return super.keyPressed(keyCode, scanCode, int_3);
	}
	
	@Override
	public void tick()
	{
		valueField.tick();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY,
					   float partialTicks)
	{
		renderBackground(matrixStack);
		drawCenteredString(matrixStack, minecraft.fontRenderer,
			"Name your new profile", width / 2, 20, 0xFFFFFF);
		
		valueField.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
}
