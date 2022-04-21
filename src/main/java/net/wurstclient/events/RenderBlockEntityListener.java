/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.events;

import net.minecraft.tileentity.TileEntity;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

import java.util.ArrayList;

public interface RenderBlockEntityListener extends Listener
{
	public void onRenderBlockEntity(RenderBlockEntityEvent event);
	
	public static class RenderBlockEntityEvent
		extends CancellableEvent<RenderBlockEntityListener>
	{
		private final TileEntity blockEntity;
		
		public RenderBlockEntityEvent(TileEntity blockEntity)
		{
			this.blockEntity = blockEntity;
		}
		
		public TileEntity getBlockEntity()
		{
			return blockEntity;
		}
		
		@Override
		public void fire(ArrayList<RenderBlockEntityListener> listeners)
		{
			for(RenderBlockEntityListener listener : listeners)
			{
				listener.onRenderBlockEntity(this);
				
				if(isCancelled())
					break;
			}
		}
		
		@Override
		public Class<RenderBlockEntityListener> getListenerType()
		{
			return RenderBlockEntityListener.class;
		}
	}
}
