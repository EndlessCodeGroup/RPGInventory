package ru.endlesscode.rpginventory.utils;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.AbstractPlayServerWorldParticlesPacket;
import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.nms.VersionHandler;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class EffectUtils {
    public static void playParticlesToAll(EnumWrappers.Particle particle, int particleNum, Location location) {
        playParticlesToAll(particle, particleNum, location, 30.0D);
    }

    private static void playParticlesToAll(EnumWrappers.Particle particle, int particleNum, Location location, double distance) {
        playParticlesToAll(particle, particleNum, location, LocationUtils.getRandomVector(), distance);
    }

    private static void playParticlesToAll(EnumWrappers.Particle particle, int particleNum, Location location, Vector direction, double distance) {
        for (Player player : LocationUtils.getNearbyPlayers(location, distance)) {
            playParticles(player, particle, particleNum, location, direction);
        }
    }

    private static void playParticles(Player player, EnumWrappers.Particle particle, int particleNum, Location location, Vector direction) {
        AbstractPacket packet;
        AbstractPlayServerWorldParticlesPacket particles;
        if (VersionHandler.is1_7_10()) {
            particles = new com.comphenix.packetwrapper.v1_7_R4.WrapperPlayServerWorldParticles();
        } else {
            particles = new WrapperPlayServerWorldParticles();
        }
        particles.setParticleType(particle);
        particles.setNumberOfParticles(particleNum);
        particles.setX((float) location.getX());
        particles.setY((float) location.getY());
        particles.setZ((float) location.getZ());
        particles.setOffsetX((float) direction.getX());
        particles.setOffsetY((float) direction.getY());
        particles.setOffsetZ((float) direction.getZ());
        packet = particles;

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.getHandle());
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unable to send packet", e);
        }
    }

    public static void playSpawnEffect(@NotNull Entity entity) {
        Location loc = entity.getLocation();

        entity.getWorld().playSound(loc,
                VersionHandler.is1_9() ? Sound.ENTITY_ENDERMEN_TELEPORT : Sound.valueOf("ENDERMAN_TELEPORT"),
                1, (float) (1.2 + Math.random() * 0.4));
        playParticlesToAll(EnumWrappers.Particle.EXPLOSION_LARGE, 3, loc);
    }

    public static void playDespawnEffect(@NotNull Entity entity) {
        Location loc = entity.getLocation();

        entity.getWorld().playSound(loc,
                VersionHandler.is1_9() ? Sound.ENTITY_ENDERMEN_TELEPORT : Sound.valueOf("ENDERMAN_TELEPORT"),
                1, (float) (0.6 + Math.random() * 0.4));
        playParticlesToAll(EnumWrappers.Particle.SMOKE_NORMAL, 3, loc);
    }
}
