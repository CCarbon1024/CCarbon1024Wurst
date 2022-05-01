/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SearchTags({"kill aura", "ForceField", "force field", "CrystalAura",
        "crystal aura", "AutoCrystal", "auto crystal"})
public final class KillauraHack extends Hack
        implements UpdateListener, PostMotionListener {
    private final SliderSetting range = new SliderSetting("Range",
            "Determines how far Killaura will reach\n" + "to attack entities.\n"
                    + "Anything that is further away than the\n"
                    + "specified value will not be attacked.",
            5, 1, 10, 0.05, ValueDisplay.DECIMAL);

    private final EnumSetting<Priority> priority = new EnumSetting<>("Priority",
            "Determines which entity will be attacked first.\n"
                    + "\u00a7lDistance\u00a7r - Attacks the closest entity.\n"
                    + "\u00a7lAngle\u00a7r - Attacks the entity that requires\n"
                    + "the least head movement.\n"
                    + "\u00a7lHealth\u00a7r - Attacks the weakest entity.",
            Priority.values(), Priority.ANGLE);

    public final SliderSetting fov =
            new SliderSetting("FOV", 360, 30, 360, 10, ValueDisplay.DEGREES);

    private final CheckboxSetting damageIndicator = new CheckboxSetting(
            "Damage indicator", "Renders a colored box within the target\n"
            + "inversely proportional to its remaining health.",
            true);

    private final CheckboxSetting filterPlayers = new CheckboxSetting(
            "Filter players", "Won't attack other players.", false);

    private final CheckboxSetting filterSleeping =
            new CheckboxSetting("Filter sleeping",
                    "Won't attack sleeping players.\n\n"
                            + "Useful for servers like Mineplex that place\n"
                            + "sleeping players on the ground to make them\n"
                            + "look like corpses.",
                    false);

    private final SliderSetting filterFlying = new SliderSetting(
            "Filter flying",
            "Won't attack players that are at least\n"
                    + "the given distance above ground.\n\n"
                    + "Useful for servers that place a flying\n"
                    + "player behind you to try and detect\n" + "your Killaura.",
            0, 0, 2, 0.05,
            v -> v == 0 ? "off" : ValueDisplay.DECIMAL.getValueString(v));

    private final CheckboxSetting filterMonsters = new CheckboxSetting(
            "Filter monsters", "Won't attack zombies, creepers, etc.", false);

    private final CheckboxSetting filterPigmen = new CheckboxSetting(
            "Filter pigmen", "Won't attack zombie pigmen.", false);

    private final CheckboxSetting filterEndermen =
            new CheckboxSetting("Filter endermen", "Won't attack endermen.", false);

    private final CheckboxSetting filterAnimals = new CheckboxSetting(
            "Filter animals", "Won't attack pigs, cows, etc.", false);

    private final CheckboxSetting filterBabies =
            new CheckboxSetting("Filter babies",
                    "Won't attack baby pigs,\n" + "baby villagers, etc.", false);

    private final CheckboxSetting filterPets =
            new CheckboxSetting("Filter pets",
                    "Won't attack tamed wolves,\n" + "tamed horses, etc.", false);

    private final CheckboxSetting filterTraders =
            new CheckboxSetting("Filter traders",
                    "Won't attack villagers, wandering traders, etc.", false);

    private final CheckboxSetting filterGolems =
            new CheckboxSetting("Filter golems",
                    "Won't attack iron golems,\n" + "snow golems and shulkers.", false);

    private final CheckboxSetting filterInvisible = new CheckboxSetting(
            "Filter invisible", "Won't attack invisible entities.", false);
    private final CheckboxSetting filterNamed = new CheckboxSetting(
            "Filter named", "Won't attack name-tagged entities.", false);

    private final CheckboxSetting filterStands = new CheckboxSetting(
            "Filter armor stands", "Won't attack armor stands.", false);
    private final CheckboxSetting filterCrystals = new CheckboxSetting(
            "Filter end crystals", "Won't attack end crystals.", false);

    private final CheckboxSetting filterNotAlive = new CheckboxSetting(
            "Filter Not Alive", "if not check will attack all entity.\n"
            + "(include entity like minecart)", true);

    private Entity target;
    private Entity renderTarget;
    private int mobBox;

    public KillauraHack() {
        super("Killaura", "Automatically attacks entities around you.");
        setCategory(Category.COMBAT);

        addSetting(range);
        addSetting(priority);
        addSetting(fov);
        addSetting(damageIndicator);
        addSetting(filterNotAlive);
        addSetting(filterPlayers);
        addSetting(filterSleeping);
        addSetting(filterFlying);
        addSetting(filterMonsters);
        addSetting(filterPigmen);
        addSetting(filterEndermen);
        addSetting(filterAnimals);
        addSetting(filterBabies);
        addSetting(filterPets);
        addSetting(filterTraders);
        addSetting(filterGolems);
        addSetting(filterInvisible);
        addSetting(filterNamed);
        addSetting(filterStands);
        addSetting(filterCrystals);
    }

    @Override
    protected void onEnable() {
        // disable other killauras
        WURST.getHax().multiAuraHack.setEnabled(false);
        WURST.getHax().triggerBotHack.setEnabled(false);
        WURST.getHax().tpAuraHack.setEnabled(false);

        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PostMotionListener.class, this);
        mobBox = GL11.glGenLists(1);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PostMotionListener.class, this);

        target = null;
        renderTarget = null;
    }

    @Override
    public void onUpdate() {
        ClientPlayerEntity player = MC.player;
        ClientWorld world = MC.world;

        if (player.getCooledAttackStrength(0) < 1)
            return;

        double rangeSq = Math.pow(range.getValue(), 2);
        Stream<Entity> stream =
                StreamSupport.stream(world.getAllEntities().spliterator(), true)
                        .filter(e -> !e.removed)
                        .filter(e -> player.getDistanceSq(e) <= rangeSq)
                        .filter(e -> e != player)
                        .filter(e -> !(e instanceof FakePlayerEntity))
                        .filter(e -> !(Objects.equals(e.getEntityString(), "aoa3:bloodlust"))) //Advent of Ascension 3 BloodLust
                        .filter(e -> !WURST.getFriends().contains(e.getName().getString()));

        if (filterNotAlive.isChecked())
            stream = stream.filter(e -> e instanceof LivingEntity
                    && ((LivingEntity) e).getHealth() > 0
                    || e instanceof EnderCrystalEntity);

        if (fov.getValue() < 360.0)
            stream = stream.filter(e -> RotationUtils.getAngleToLookVec(
                    e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);

        if (filterPlayers.isChecked())
            stream = stream.filter(e -> !(e instanceof PlayerEntity));

        if (filterSleeping.isChecked())
            stream = stream.filter(e -> !(e instanceof PlayerEntity
                    && ((PlayerEntity) e).isSleeping()));

        if (filterMonsters.isChecked())
            stream = stream.filter(e -> !(e instanceof MonsterEntity));

        if (filterPigmen.isChecked())
            stream = stream.filter(e -> !(e instanceof ZombifiedPiglinEntity));

        if (filterEndermen.isChecked())
            stream = stream.filter(e -> !(e instanceof EndermanEntity));

        if (filterAnimals.isChecked())
            stream = stream.filter(
                    e -> !(e instanceof AnimalEntity || e instanceof AmbientEntity));
//TODO Filter WaterCreatureEntity

/*TODO Filter Baby
        if (filterBabies.isChecked())
            stream = stream.filter(e -> !(e instanceof PassiveEntity
                    && ((PassiveEntity) e).isBaby()));*/

        if (filterPets.isChecked())
            stream = stream
                    .filter(e -> !(e instanceof TameableEntity
                            && ((TameableEntity) e).isTamed()))
                    .filter(e -> !(e instanceof HorseEntity
                            && ((HorseEntity) e).isTame()));

        if (filterTraders.isChecked())
            stream = stream.filter(e -> !(e instanceof VillagerEntity));

        if (filterGolems.isChecked())
            stream = stream.filter(e -> !(e instanceof GolemEntity));

        if (filterInvisible.isChecked())
            stream = stream.filter(e -> !e.isInvisible());

        if (filterNamed.isChecked())
            stream = stream.filter(e -> !e.hasCustomName());

        if (filterStands.isChecked())
            stream = stream.filter(e -> !(e instanceof ArmorStandEntity));

        if (filterCrystals.isChecked())
            stream = stream.filter(e -> !(e instanceof EnderCrystalEntity));


        target = stream.min(priority.getSelected().comparator).orElse(null);
        renderTarget = target;
        if (target == null)
            return;

        WURST.getRotationFaker()
                .faceVectorPacket(target.getBoundingBox().getCenter());
    }

    @Override
    public void onPostMotion() {
        if (target == null)
            return;

        WURST.getHax().criticalsHack.doCritical();
        ClientPlayerEntity player = MC.player;
        MC.playerController.attackEntity(player, target);
        player.swing(Hand.MAIN_HAND, true);

        target = null;
    }

    public void onRender(float partialTicks) {
        if (renderTarget == null || !damageIndicator.isChecked())
            return;

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

        double extraSize = 0;


        GL11.glPushMatrix();

        GL11.glTranslated(
                target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * partialTicks - regionX,
                target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * partialTicks,
                target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * partialTicks - regionZ);

        GL11.glScaled(target.getWidth() + extraSize, target.getHeight() + extraSize,
                target.getWidth() + extraSize);

        float f = MC.player.getDistance(target) / 20F;
        GL11.glColor4f(2 - f, f, 0, 0.5F);

        GL11.glCallList(mobBox);

        GL11.glPopMatrix();


        GL11.glPopMatrix();

        // GL resets
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private enum Priority {
        DISTANCE("Distance", e -> MC.player.getDistanceSq(e)),

        ANGLE("Angle",
                e -> RotationUtils
                        .getAngleToLookVec(e.getBoundingBox().getCenter())),

        HEALTH("Health", e -> e instanceof LivingEntity
                ? ((LivingEntity) e).getHealth() : Integer.MAX_VALUE);

        private final String name;
        private final Comparator<Entity> comparator;

        private Priority(String name, ToDoubleFunction<Entity> keyExtractor) {
            this.name = name;
            comparator = Comparator.comparingDouble(keyExtractor);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
