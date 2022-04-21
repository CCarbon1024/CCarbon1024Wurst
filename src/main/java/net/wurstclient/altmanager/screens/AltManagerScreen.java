/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.altmanager.screens;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.wurstclient.WurstClient;
import net.wurstclient.altmanager.*;
import net.wurstclient.util.ListWidget;
import net.wurstclient.util.MultiProcessingUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class AltManagerScreen extends Screen
{
	private final Screen prevScreen;
	private final AltManager altManager;
	
	private ListGui listGui;
	private boolean shouldAsk = true;
	private int errorTimer;
	
	private Button useButton;
	private Button starButton;
	private Button editButton;
	private Button deleteButton;
	
	private Button importButton;
	private Button exportButton;
	
	public AltManagerScreen(Screen prevScreen, AltManager altManager)
	{
		super(new StringTextComponent("Alt Manager"));
		this.prevScreen = prevScreen;
		this.altManager = altManager;
	}
	
	@Override
	public void init()
	{
		listGui = new ListGui(minecraft, this, altManager.getList());
		
		if(altManager.getList().isEmpty() && shouldAsk)
			minecraft.displayGuiScreen(new ConfirmScreen(this::confirmGenerate,
				new StringTextComponent("Your alt list is empty."), new StringTextComponent(
					"Would you like some random alts to get started?")));
		
		addButton(useButton = new Button(width / 2 - 154, height - 52,
			100, 20, new StringTextComponent("Login"), b -> pressLogin()));
		
		addButton(new Button(width / 2 - 50, height - 52, 100, 20,
			new StringTextComponent("Direct Login"),
			b -> minecraft.displayGuiScreen(new DirectLoginScreen(this))));
		
		addButton(new Button(width / 2 + 54, height - 52, 100, 20,
			new StringTextComponent("Add"),
			b -> minecraft.displayGuiScreen(new AddAltScreen(this, altManager))));
		
		addButton(starButton = new Button(width / 2 - 154, height - 28,
			75, 20, new StringTextComponent("Favorite"), b -> pressFavorite()));
		
		addButton(editButton = new Button(width / 2 - 76, height - 28, 74,
			20, new StringTextComponent("Edit"), b -> pressEdit()));
		
		addButton(deleteButton = new Button(width / 2 + 2, height - 28,
			74, 20, new StringTextComponent("Delete"), b -> pressDelete()));
		
		addButton(new Button(width / 2 + 80, height - 28, 75, 20,
			new StringTextComponent("Cancel"), b -> minecraft.displayGuiScreen(prevScreen)));
		
		addButton(importButton = new Button(8, 8, 50, 20,
			new StringTextComponent("Import"), b -> pressImportAlts()));
		
		addButton(exportButton = new Button(58, 8, 50, 20,
			new StringTextComponent("Export"), b -> pressExportAlts()));
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		listGui.mouseClicked(mouseX, mouseY, mouseButton);
		
		if(mouseY >= 36 && mouseY <= height - 57)
			if(mouseX >= width / 2 + 140 || mouseX <= width / 2 - 126)
				listGui.selected = -1;
			
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button,
		double deltaX, double deltaY)
	{
		listGui.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		listGui.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double d, double e, double amount)
	{
		listGui.mouseScrolled(d, e, amount);
		return super.mouseScrolled(d, e, amount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(keyCode == GLFW.GLFW_KEY_ENTER)
			useButton.onPress();
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void tick()
	{
		boolean altSelected = listGui.selected >= 0
			&& listGui.selected < altManager.getList().size();
		
		useButton.active = altSelected;
		starButton.active = altSelected;
		editButton.active = altSelected;
		deleteButton.active = altSelected;
		
		boolean windowMode = !minecraft.gameSettings.fullscreen;
		importButton.active = windowMode;
		exportButton.active = windowMode;
	}
	
	private void pressLogin()
	{
		Alt alt = listGui.getSelectedAlt();
		if(alt == null)
			return;
		
		if(alt.isCracked())
		{
			LoginManager.changeCrackedName(alt.getEmail());
			minecraft.displayGuiScreen(prevScreen);
			return;
		}
		
		String error = LoginManager.login(alt.getEmail(), alt.getPassword());
		
		if(!error.isEmpty())
		{
			errorTimer = 8;
			return;
		}
		
		altManager.setChecked(listGui.selected,
			minecraft.getSession().getUsername());
		minecraft.displayGuiScreen(prevScreen);
	}
	
	private void pressFavorite()
	{
		Alt alt = listGui.getSelectedAlt();
		if(alt == null)
			return;
		
		altManager.setStarred(listGui.selected, !alt.isStarred());
		listGui.selected = -1;
	}
	
	private void pressEdit()
	{
		Alt alt = listGui.getSelectedAlt();
		if(alt == null)
			return;
		
		minecraft.displayGuiScreen(new EditAltScreen(this, altManager, alt));
	}
	
	private void pressDelete()
	{
		Alt alt = listGui.getSelectedAlt();
		if(alt == null)
			return;

		StringTextComponent text =
			new StringTextComponent("Are you sure you want to remove this alt?");
		
		String altName = alt.getNameOrEmail();
		StringTextComponent message = new StringTextComponent(
			"\"" + altName + "\" will be lost forever! (A long time!)");
		
		ConfirmScreen screen = new ConfirmScreen(this::confirmRemove, text,
			message, new StringTextComponent("Delete"), new StringTextComponent("Cancel"));
		minecraft.displayGuiScreen(screen);
	}
	
	private void pressImportAlts()
	{
		try
		{
			Process process = MultiProcessingUtils.startProcessWithIO(
				ImportAltsFileChooser.class,
				WurstClient.INSTANCE.getWurstFolder().toString());
			
			Path path = getFileChooserPath(process);
			process.waitFor();
			
			if(path.getFileName().toString().endsWith(".json"))
				importAsJSON(path);
			else
				importAsTXT(path);
			
		}catch(IOException | InterruptedException | JsonException e)
		{
			e.printStackTrace();
		}
	}
	
	private void importAsJSON(Path path) throws IOException, JsonException
	{
		WsonObject wson = JsonUtils.parseFileToObject(path);
		ArrayList<Alt> alts = AltsFile.parseJson(wson);
		altManager.addAll(alts);
	}
	
	private void importAsTXT(Path path) throws IOException
	{
		List<String> lines = Files.readAllLines(path);
		ArrayList<Alt> alts = new ArrayList<>();
		
		for(String line : lines)
		{
			String[] data = line.split(":");
			
			switch(data.length)
			{
				case 1:
				alts.add(new Alt(data[0], null, null));
				break;
				
				case 2:
				alts.add(new Alt(data[0], data[1], null));
				break;
			}
		}
		
		altManager.addAll(alts);
	}
	
	private void pressExportAlts()
	{
		try
		{
			Process process = MultiProcessingUtils.startProcessWithIO(
				ExportAltsFileChooser.class,
				WurstClient.INSTANCE.getWurstFolder().toString());
			
			Path path = getFileChooserPath(process);
			
			process.waitFor();
			
			if(path.getFileName().toString().endsWith(".json"))
				exportAsJSON(path);
			else
				exportAsTXT(path);
			
		}catch(IOException | InterruptedException | JsonException e)
		{
			e.printStackTrace();
		}
	}
	
	private Path getFileChooserPath(Process process) throws IOException
	{
		try(BufferedReader bf =
			new BufferedReader(new InputStreamReader(process.getInputStream(),
				StandardCharsets.UTF_8)))
		{
			String response = bf.readLine();
			
			if(response == null)
				throw new IOException("No reponse from FileChooser");
			
			try
			{
				return Paths.get(response);
				
			}catch(InvalidPathException e)
			{
				throw new IOException(
					"Reponse from FileChooser is not a valid path");
			}
		}
	}
	
	private void exportAsJSON(Path path) throws IOException, JsonException
	{
		JsonObject json = AltsFile.createJson(altManager);
		JsonUtils.toJson(json, path);
	}
	
	private void exportAsTXT(Path path) throws IOException
	{
		List<String> lines = new ArrayList<>();
		
		for(Alt alt : altManager.getList())
			if(alt.isCracked())
				lines.add(alt.getEmail());
			else
				lines.add(alt.getEmail() + ":" + alt.getPassword());
			
		Files.write(path, lines);
	}
	
	private void confirmGenerate(boolean confirmed)
	{
		if(confirmed)
		{
			ArrayList<Alt> alts = new ArrayList<>();
			for(int i = 0; i < 8; i++)
				alts.add(new Alt(NameGenerator.generateName(), null, null));
			
			altManager.addAll(alts);
		}
		
		shouldAsk = false;
		minecraft.displayGuiScreen(this);
	}
	
	private void confirmRemove(boolean confirmed)
	{
		if(confirmed)
			altManager.remove(listGui.selected);
		
		minecraft.displayGuiScreen(this);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY,
		float partialTicks)
	{
		renderBackground(matrixStack);
		listGui.render(matrixStack, mouseX, mouseY, partialTicks);
		
		// skin preview
		if(listGui.getSelectedSlot() != -1
			&& listGui.getSelectedSlot() < altManager.getList().size())
		{
			Alt alt = listGui.getSelectedAlt();
			if(alt == null)
				return;
			
			AltRenderer.drawAltBack(matrixStack, alt.getNameOrEmail(),
				(width / 2 - 125) / 2 - 32, height / 2 - 64 - 9, 64, 128);
			AltRenderer.drawAltBody(matrixStack, alt.getNameOrEmail(),
				width - (width / 2 - 140) / 2 - 32, height / 2 - 64 - 9, 64,
				128);
		}
		
		// title text
		drawCenteredString(matrixStack, font, "Alt Manager", width / 2,
			4, 16777215);
		drawCenteredString(matrixStack, font,
			"Alts: " + altManager.getList().size(), width / 2, 14, 10526880);
		drawCenteredString(
			matrixStack, font, "premium: " + altManager.getNumPremium()
				+ ", cracked: " + altManager.getNumCracked(),
			width / 2, 24, 10526880);
		
		// red flash for errors
		if(errorTimer > 0)
		{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			
			GL11.glColor4f(1, 0, 0, errorTimer / 16F);
			
			GL11.glBegin(GL11.GL_QUADS);
			{
				GL11.glVertex2d(0, 0);
				GL11.glVertex2d(width, 0);
				GL11.glVertex2d(width, height);
				GL11.glVertex2d(0, height);
			}
			GL11.glEnd();
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			errorTimer--;
		}
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderButtonTooltip(matrixStack, mouseX, mouseY);
	}
	
	private void renderButtonTooltip(MatrixStack matrixStack, int mouseX,
		int mouseY)
	{
		for(Widget button : buttons)
		{
			if(!button.isHovered())
				continue;
			
			if(button != importButton && button != exportButton)
				continue;
			
			ArrayList<ITextComponent> tooltip = new ArrayList<>();
			tooltip.add(new StringTextComponent("This button opens another window."));
			if(minecraft.gameSettings.fullscreen)
				tooltip
					.add(new StringTextComponent("\u00a7cTurn off fullscreen mode!"));
			else
			{
				tooltip
					.add(new StringTextComponent("It might look like the game is not"));
				tooltip.add(
					new StringTextComponent("responding while that window is open."));
			}
			func_243308_b(matrixStack, tooltip, mouseX, mouseY);
			break;
		}
	}
	
	public static final class ListGui extends ListWidget
	{
		private final List<Alt> list;
		private int selected = -1;
		
		public ListGui(Minecraft minecraft, AltManagerScreen prevScreen,
					   List<Alt> list)
		{
			super(minecraft, prevScreen.width, prevScreen.height, 36,
				prevScreen.height - 56, 30);
			
			this.list = list;
		}
		
		@Override
		protected boolean isSelectedItem(int id)
		{
			return selected == id;
		}
		
		protected int getSelectedSlot()
		{
			return selected;
		}
		
		protected Alt getSelectedAlt()
		{
			if(selected < 0 || selected >= list.size())
				return null;
			
			return list.get(selected);
		}
		
		@Override
		protected int getItemCount()
		{
			return list.size();
		}
		
		@Override
		protected boolean selectItem(int index, int button, double mouseX,
			double mouseY)
		{
			if(index >= 0 && index < list.size())
				selected = index;
			
			return true;
		}
		
		@Override
		protected void renderBackground()
		{
			
		}
		
		@Override
		protected void renderItem(MatrixStack matrixStack, int id, int x, int y,
								  int var4, int var5, int var6, float partialTicks)
		{
			Alt alt = list.get(id);
			
			// green glow when logged in
			if(client.getSession().getUsername().equals(alt.getName()))
			{
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_CULL_FACE);
				GL11.glEnable(GL11.GL_BLEND);
				
				float opacity =
					0.3F - Math.abs(MathHelper.sin(System.currentTimeMillis()
						% 10000L / 10000F * (float)Math.PI * 2.0F) * 0.15F);
				
				GL11.glColor4f(0, 1, 0, opacity);
				
				GL11.glBegin(GL11.GL_QUADS);
				{
					GL11.glVertex2d(x - 2, y - 2);
					GL11.glVertex2d(x - 2 + 220, y - 2);
					GL11.glVertex2d(x - 2 + 220, y - 2 + 30);
					GL11.glVertex2d(x - 2, y - 2 + 30);
				}
				GL11.glEnd();
				
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glDisable(GL11.GL_BLEND);
			}
			
			// face
			AltRenderer.drawAltFace(matrixStack, alt.getNameOrEmail(), x + 1,
				y + 1, 24, 24, isSelectedItem(id));
			
			// name / email
			client.fontRenderer.drawString(matrixStack,
				"Name: " + alt.getNameOrEmail(), x + 31, y + 3, 10526880);
			
			// tags
			String tags = alt.isCracked() ? "\u00a78cracked" : "\u00a72premium";
			if(alt.isStarred())
				tags += "\u00a7r, \u00a7efavorite";
			if(alt.isUnchecked())
				tags += "\u00a7r, \u00a7cunchecked";
			client.fontRenderer.drawString(matrixStack, tags, x + 31, y + 15,
				10526880);
		}
	}
}
