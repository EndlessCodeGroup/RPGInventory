/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.utils;

import com.comphenix.packetwrapper.WrapperPlayServerTitle;
import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.inspector.bukkit.plugin.TrackedBukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class EffectUtils {
    public static void playParticlesToAll(EnumWrappers.Particle particle, int particleNum, @NotNull Location location) {
        playParticlesToAll(particle, particleNum, location, 30.0D);
    }

    private static void playParticlesToAll(EnumWrappers.Particle particle, int particleNum, @NotNull Location location, double distance) {
        playParticlesToAll(particle, particleNum, location, LocationUtils.getRandomVector(), distance);
    }

    private static void playParticlesToAll(EnumWrappers.Particle particle, int particleNum, @NotNull Location location, @NotNull Vector direction, double distance) {
        for (Player player : LocationUtils.getNearbyPlayers(location, distance)) {
            playParticles(player, particle, particleNum, location, direction);
        }
    }

    private static void playParticles(Player player, EnumWrappers.Particle particle, int particleNum, Location location, Vector direction) {
        WrapperPlayServerWorldParticles particles = new WrapperPlayServerWorldParticles();
        particles.setParticleType(particle);
        particles.setNumberOfParticles(particleNum);
        particles.setX((float) location.getX());
        particles.setY((float) location.getY());
        particles.setZ((float) location.getZ());
        particles.setOffsetX((float) direction.getX());
        particles.setOffsetY((float) direction.getY());
        particles.setOffsetZ((float) direction.getZ());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, particles.getHandle());
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unable to send packet", e);
        }
    }

    public static void playSpawnEffect(Entity entity) {
        Location loc = entity.getLocation();

        entity.getWorld().playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, (float) (1.2 + Math.random() * 0.4));
        playParticlesToAll(EnumWrappers.Particle.EXPLOSION_LARGE, 3, loc);
    }

    public static void playDespawnEffect(Entity entity) {
        Location loc = entity.getLocation();

        entity.getWorld().playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, (float) (0.6 + Math.random() * 0.4));
        playParticlesToAll(EnumWrappers.Particle.SMOKE_NORMAL, 3, loc);
    }

    public static void sendTitle(final Player player, int delay, String title, @NotNull final List<String> subtitles, @Nullable final Runnable callback) {
        if (delay < 2) {
            delay = 2;
        }

        final WrapperPlayServerTitle titlePacket = new WrapperPlayServerTitle();
        int time = (subtitles.size() == 0 ? delay : delay * subtitles.size()) - 1;
        try {
            WrapperPlayServerTitle resetPacket = new WrapperPlayServerTitle();
            resetPacket.setAction(EnumWrappers.TitleAction.RESET);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, resetPacket.getHandle());

            WrapperPlayServerTitle timesPacket = new WrapperPlayServerTitle();
            timesPacket.setAction(EnumWrappers.TitleAction.TIMES);
            timesPacket.setFadeIn(10);
            timesPacket.setFadeOut(10);
            timesPacket.setStay(20 * time);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, timesPacket.getHandle());

            title = StringUtils.coloredLine(StringUtils.setPlaceholders(player, title));
            titlePacket.setAction(EnumWrappers.TitleAction.TITLE);
            titlePacket.setTitle(WrappedChatComponent.fromChatMessage(StringUtils.coloredLine(title))[0]);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, titlePacket.getHandle());
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unable to send packet", e);
        }

        new TrackedBukkitRunnable() {
            int line = 0;

            @Override
            public void run() {
                try {
                    if (line == subtitles.size()) {
                        this.cancel();
                        if (callback != null) {
                            callback.run();
                        }
                        return;
                    }

                    String subtitle = StringUtils.coloredLine(StringUtils.setPlaceholders(player, subtitles.get(line)));
                    titlePacket.setAction(EnumWrappers.TitleAction.SUBTITLE);
                    titlePacket.setTitle(WrappedChatComponent.fromChatMessage(subtitle)[0]);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, titlePacket.getHandle());
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Unable to send packet", e);
                }

                line++;
            }
        }.runTaskTimer(RPGInventory.getInstance(), 0, 20 * delay);
    }
}
