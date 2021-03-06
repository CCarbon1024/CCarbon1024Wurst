/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.wurstclient.WurstClient;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.KeybindList;
import net.wurstclient.util.ListWidget;
import org.lwjgl.glfw.GLFW;

public final class KeybindManagerScreen extends Screen
{
	private final Screen prevScreen;
	
	private ListGui listGui;
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button backButton;
	
	public KeybindManagerScreen(Screen prevScreen)
	{
		super(new StringTextComponent(""));
		this.prevScreen = prevScreen;
	}
	
	@Override
	public void init()
	{
		listGui = new ListGui(minecraft, width, height, 36, height - 56, 30);
		
		addButton(addButton = new Button(width / 2 - 102, height - 52,
			100, 20, new StringTextComponent("Add"),
			b -> minecraft.displayGuiScreen(new KeybindEditorScreen(this))));
		
		addButton(editButton = new Button(width / 2 + 2, height - 52, 100,
			20, new StringTextComponent("Edit"), b -> edit()));
		
		addButton(removeButton = new Button(width / 2 - 102, height - 28,
			100, 20, new StringTextComponent("Remove"), b -> remove()));
		
		addButton(backButton = new Button(width / 2 + 2, height - 28, 100,
			20, new StringTextComponent("Back"), b -> minecraft.displayGuiScreen(prevScreen)));
		
		addButton(
			new Button(8, 8, 100, 20, new StringTextComponent("Reset Keybinds"),
				b -> minecraft.displayGuiScreen(new ConfirmScreen(confirmed -> {
					if(confirmed)
						WurstClient.INSTANCE.getKeybinds()
							.setKeybinds(KeybindList.DEFAULT_KEYBINDS);
					minecraft.displayGuiScreen(this);
				}, new StringTextComponent(
					"Are you sure you want to reset your keybinds?"),
					new StringTextComponent("This cannot be undone!")))));
		
		addButton(new Button(width - 108, 8, 100, 20,
			new StringTextComponent("Profiles..."),
			b -> minecraft.displayGuiScreen(new KeybindProfilesScreen(this))));
	}
	
	private void edit()
	{
		Keybind keybind = WurstClient.INSTANCE.getKeybinds().getAllKeybinds()
			.get(listGui.selected);
		minecraft.displayGuiScreen(new KeybindEditorScreen(this, keybind.getKey(),
			keybind.getCommands()));
	}
	
	private void remove()
	{
		Keybind keybind1 = WurstClient.INSTANCE.getKeybinds().getAllKeybinds()
			.get(listGui.selected);
		WurstClient.INSTANCE.getKeybinds().remove(keybind1.getKey());
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		boolean childClicked = super.mouseClicked(mouseX, mouseY, mouseButton);
		
		listGui.mouseClicked(mouseX, mouseY, mouseButton);
		
		if(!childClicked)
			if(mouseY >= 36 && mouseY <= height - 57)
				if(mouseX >= width / 2 + 140 || mouseX <= width / 2 - 126)
					listGui.selected = -1;
				
		return childClicked;
	}
	
	@Override
	public boolean mouseDragged(double double_1, double double_2, int int_1,
		double double_3, double double_4)
	{
		listGui.mouseDragged(double_1, double_2, int_1, double_3, double_4);
		return super.mouseDragged(double_1, double_2, int_1, double_3,
			double_4);
	}
	
	@Override
	public boolean mouseReleased(double double_1, double double_2, int int_1)
	{
		listGui.mouseReleased(double_1, double_2, int_1);
		return super.mouseReleased(double_1, double_2, int_1);
	}
	
	@Override
	public boolean mouseScrolled(double double_1, double double_2,
		double double_3)
	{
		listGui.mouseScrolled(double_1, double_2, double_3);
		return super.mouseScrolled(double_1, double_2, double_3);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int int_3)
	{
		if(keyCode == GLFW.GLFW_KEY_ENTER)
			if(editButton.active)
				editButton.onPress();
			else
				addButton.onPress();
		else if(keyCode == GLFW.GLFW_KEY_DELETE)
			removeButton.onPress();
		else if(keyCode == GLFW.GLFW_KEY_ESCAPE)
			backButton.onPress();
		
		return super.keyPressed(keyCode, scanCode, int_3);
	}
	
	@Override
	public void tick()
	{
		boolean inBounds =
			listGui.selected > -1 && listGui.selected < listGui.getItemCount();
		
		editButton.active = inBounds;
		removeButton.active = inBounds;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY,
		float partialTicks)
	{
		renderBackground(matrixStack);
		listGui.render(matrixStack, mouseX, mouseY, partialTicks);
		
		drawCenteredString(matrixStack, font, "Keybind Manager",
			width / 2, 8, 0xffffff);
		drawCenteredString(matrixStack, font,
			"Keybinds: " + listGui.getItemCount(), width / 2, 20, 0xffffff);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
	
	private static final class ListGui extends ListWidget
	{
		private int selected = -1;
		
		public ListGui(Minecraft mc, int width, int height, int top,
					   int bottom, int slotHeight)
		{
			super(mc, width, height, top, bottom, slotHeight);
		}
		
		@Override
		protected boolean isSelectedItem(int index)
		{
			return selected == index;
		}
		
		@Override
		protected int getItemCount()
		{
			return WurstClient.INSTANCE.getKeybinds().getAllKeybinds().size();
		}
		
		@Override
		protected boolean selectItem(int index, int int_2, double var3,
			double var4)
		{
			if(index >= 0 && index < getItemCount())
				selected = index;
			
			return true;
		}
		
		@Override
		protected void renderBackground()
		{
			
		}
		
		@Override
		protected void renderItem(MatrixStack matrixStack, int index, int x,
								  int y, int slotHeight, int mouseX, int mouseY, float partialTicks)
		{
			Keybind keybind =
				WurstClient.INSTANCE.getKeybinds().getAllKeybinds().get(index);
			
			client.fontRenderer.drawString(matrixStack,
				"Key: " + keybind.getKey().replace("key.keyboard.", ""), x + 3,
				y + 3, 0xa0a0a0);
			client.fontRenderer.drawString(matrixStack,
				"Commands: " + keybind.getCommands(), x + 3, y + 15, 0xa0a0a0);
		}
	}
}
