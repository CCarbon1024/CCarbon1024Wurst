/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SearchTags({"player esp"})
public final class PlayerEspHack extends Hack implements UpdateListener,
        CameraTransformViewBobbingListener, RenderListener
{
    private final EnumSetting<Style> style =
            new EnumSetting<>("Style", Style.values(), Style.LINES_AND_BOXES);

    private final EnumSetting<BoxSize> boxSize = new EnumSetting<>("Box size",
            "\u00a7lAccurate\u00a7r mode shows the exact\n"
                    + "hitbox of each player.\n"
                    + "\u00a7lFancy\u00a7r mode shows slightly larger\n"
                    + "boxes that look better.",
            BoxSize.values(), BoxSize.FANCY);

    private final CheckboxSetting filterSleeping = new CheckboxSetting(
            "Filter sleeping", "Won't show sleeping players.", false);

    private final CheckboxSetting filterInvisible = new CheckboxSetting(
            "Filter invisible", "Won't show invisible players.", false);

    private int playerBox;
    private final ArrayList<Entity> players = new ArrayList<>();

    public PlayerEspHack()
    {
        super("PlayerESP", "Highlights nearby player.");
        setCategory(Category.RENDER);
        addSetting(style);
        addSetting(boxSize);
        addSetting(filterInvisible);
        addSetting(filterSleeping);
    }

    @Override
    public void onEnable()
    {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(CameraTransformViewBobbingListener.class, this);
        EVENTS.add(RenderListener.class, this);

        playerBox = GL11.glGenLists(1);
        GL11.glNewList(playerBox, GL11.GL_COMPILE);
        AxisAlignedBB bb = new AxisAlignedBB(-0.5, 0, -0.5, 0.5, 1, 0.5);
        RenderUtils.drawOutlinedBox(bb);
        GL11.glEndList();
    }

    @Override
    public void onDisable()
    {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(CameraTransformViewBobbingListener.class, this);
        EVENTS.remove(RenderListener.class, this);

        GL11.glDeleteLists(playerBox, 1);
        playerBox = 0;
    }

    @Override
    public void onUpdate()
    {
        players.clear();

        PlayerEntity player = MC.player;
        ClientWorld world = MC.world;

        Stream<AbstractClientPlayerEntity> stream =
                MC.world.getPlayers()
                        .parallelStream().filter(e -> !e.removed && e.getHealth() > 0)
                        .filter(e -> e != player)
                        .filter(e -> !(e instanceof FakePlayerEntity))
                        .filter(e -> Math.abs(e.getPosY() - MC.player.getPosY()) <= 1e6);

        if(filterInvisible.isChecked())
            stream = stream.filter(e -> !e.isInvisible());

        if(filterSleeping.isChecked())
            stream = stream.filter(e -> !e.isSleeping());

        if(filterInvisible.isChecked())
            stream = stream.filter(e -> !e.isInvisible());

        players.addAll(stream.collect(Collectors.toList()));
    }

    @Override
    public void onCameraTransformViewBobbing(
            CameraTransformViewBobbingEvent event)
    {
        if(style.getSelected().lines)
            event.cancel();
    }

    @Override
    public void onRender(float partialTicks)
    {
        // GL settings
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glPushMatrix();
        RenderUtils.applyRegionalRenderOffset();


        BlockPos camPos = RenderUtils.getCameraBlockPos();
        int regionX = (camPos.getX() >> 9) * 512;
        int regionZ = (camPos.getZ() >> 9) * 512;

        if(style.getSelected().boxes)
            renderBoxes(partialTicks, regionX, regionZ);

        if(style.getSelected().lines)
            renderTracers(partialTicks, regionX, regionZ);

        GL11.glPopMatrix();

        // GL resets
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private void renderBoxes(double partialTicks, int regionX, int regionZ)
    {
        double extraSize = boxSize.getSelected().extraSize;

        for(Entity e : players)
        {
            GL11.glPushMatrix();

            GL11.glTranslated(
                    e.lastTickPosX + (e.getPosX() - e.lastTickPosX) * partialTicks - regionX,
                    e.lastTickPosY + (e.getPosY() - e.lastTickPosY) * partialTicks,
                    e.lastTickPosZ + (e.getPosZ() - e.lastTickPosZ) * partialTicks - regionZ);

            GL11.glScaled(e.getWidth() + extraSize, e.getHeight() + extraSize,
                    e.getWidth() + extraSize);

            if(WURST.getFriends().contains(e.getName().getString()))
                GL11.glColor4f(0, 0, 1, 0.5F);
            else
            {
                float f = MC.player.getDistance(e) / 20F;
                GL11.glColor4f(2 - f, f, 0, 0.5F);
            }

            GL11.glCallList(playerBox);

            GL11.glPopMatrix();
        }
    }

    private void renderTracers(double partialTicks, int regionX, int regionZ)
    {
        Vector3d start =
                RotationUtils.getClientLookVec().add(RenderUtils.getCameraPos());

        GL11.glBegin(GL11.GL_LINES);
        for(Entity e : players)
        {
            Vector3d end = e.getBoundingBox().getCenter()
                    .subtract(new Vector3d(e.getPosX(), e.getPosY(), e.getPosZ())
                            .subtract(e.lastTickPosX, e.lastTickPosY, e.lastTickPosZ)
                            .scale(1 - partialTicks));

            if(WURST.getFriends().contains(e.getName().getString()))
                GL11.glColor4f(0, 0, 1, 0.5F);
            else
            {
                float f = MC.player.getDistance(e) / 20F;
                GL11.glColor4f(2 - f, f, 0, 0.5F);
            }

            GL11.glVertex3d(start.x - regionX, start.y, start.z - regionZ);
            GL11.glVertex3d(end.x - regionX, end.y, end.z - regionZ);
        }
        GL11.glEnd();
    }

    private enum Style
    {
        BOXES("Boxes only", true, false),
        LINES("Lines only", false, true),
        LINES_AND_BOXES("Lines and boxes", true, true);

        private final String name;
        private final boolean boxes;
        private final boolean lines;

        private Style(String name, boolean boxes, boolean lines)
        {
            this.name = name;
            this.boxes = boxes;
            this.lines = lines;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    private enum BoxSize
    {
        ACCURATE("Accurate", 0),
        FANCY("Fancy", 0.1);

        private final String name;
        private final double extraSize;

        private BoxSize(String name, double extraSize)
        {
            this.name = name;
            this.extraSize = extraSize;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
