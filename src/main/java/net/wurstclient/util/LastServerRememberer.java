/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.wurstclient.WurstClient;
import net.wurstclient.mixinterface.IMultiplayerScreen;

/**
 * Remembers the last server you joined to make the "Reconnect",
 * "AutoReconnect" and "Last Server" buttons work.
 */
public enum LastServerRememberer
{
	;
	
	private static ServerData lastServer;
	
	public static ServerData getLastServer()
	{
		return lastServer;
	}
	
	public static void setLastServer(ServerData server)
	{
		lastServer = server;
	}
	
	public static void joinLastServer(MultiplayerScreen mpScreen)
	{
		if(lastServer == null)
			return;
		
		((IMultiplayerScreen)mpScreen).connectToServer(lastServer);
	}
	
	public static void reconnect(Screen prevScreen)
	{
		if(lastServer == null)
			return;

		WurstClient.MC.displayGuiScreen(
				new ConnectingScreen(prevScreen, WurstClient.MC, lastServer));
	}
}
