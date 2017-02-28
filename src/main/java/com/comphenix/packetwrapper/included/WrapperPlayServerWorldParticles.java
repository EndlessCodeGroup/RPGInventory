package com.comphenix.packetwrapper.included;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

/**
 * This file is part of PacketWrapper.
 * Copyright (C) 2012-2015 Kristian S. Strangeland
 * Copyright (C) 2015 dmulloy2
 *
 * PacketWrapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PacketWrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PacketWrapper.  If not, see <http://www.gnu.org/licenses/>.
 */

@SuppressWarnings("ALL")
public class WrapperPlayServerWorldParticles extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.WORLD_PARTICLES;

    public WrapperPlayServerWorldParticles() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerWorldParticles(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Retrieve Particle type.
     *
     * @return The current Particle type
     */
    public Particle getParticleType() {
        return handle.getParticles().read(0);
    }

    /**
     * Set Particle type.
     *
     * @param value - new value.
     */
    public void setParticleType(Particle value) {
        handle.getParticles().write(0, value);
    }

    /**
     * Retrieve X.
     *
     * Notes: x position of the particle
     *
     * @return The current X
     */
    public float getX() {
        return handle.getFloat().read(0);
    }

    /**
     * Set X.
     *
     * @param value - new value.
     */
    public void setX(float value) {
        handle.getFloat().write(0, value);
    }

    /**
     * Retrieve Y.
     *
     * Notes: y position of the particle
     *
     * @return The current Y
     */
    public float getY() {
        return handle.getFloat().read(1);
    }

    /**
     * Set Y.
     *
     * @param value - new value.
     */
    public void setY(float value) {
        handle.getFloat().write(1, value);
    }

    /**
     * Retrieve Z.
     *
     * Notes: z position of the particle
     *
     * @return The current Z
     */
    public float getZ() {
        return handle.getFloat().read(2);
    }

    /**
     * Set Z.
     *
     * @param value - new value.
     */
    public void setZ(float value) {
        handle.getFloat().write(2, value);
    }

    /**
     * Retrieve Offset X.
     *
     * Notes: this is added to the X position after being multiplied by
     * random.nextGaussian()
     *
     * @return The current Offset X
     */
    public float getOffsetX() {
        return handle.getFloat().read(3);
    }

    /**
     * Set Offset X.
     *
     * @param value - new value.
     */
    public void setOffsetX(float value) {
        handle.getFloat().write(3, value);
    }

    /**
     * Retrieve Offset Y.
     *
     * Notes: this is added to the Y position after being multiplied by
     * random.nextGaussian()
     *
     * @return The current Offset Y
     */
    public float getOffsetY() {
        return handle.getFloat().read(4);
    }

    /**
     * Set Offset Y.
     *
     * @param value - new value.
     */
    public void setOffsetY(float value) {
        handle.getFloat().write(4, value);
    }

    /**
     * Retrieve Offset Z.
     *
     * Notes: this is added to the Z position after being multiplied by
     * random.nextGaussian()
     *
     * @return The current Offset Z
     */
    public float getOffsetZ() {
        return handle.getFloat().read(5);
    }

    /**
     * Set Offset Z.
     *
     * @param value - new value.
     */
    public void setOffsetZ(float value) {
        handle.getFloat().write(5, value);
    }

    /**
     * Retrieve Particle data.
     *
     * Notes: the data of each particle
     *
     * @return The current Particle data
     */
    public float getParticleData() {
        return handle.getFloat().read(6);
    }

    /**
     * Set Particle data.
     *
     * @param value - new value.
     */
    public void setParticleData(float value) {
        handle.getFloat().write(6, value);
    }

    /**
     * Retrieve Number of particles.
     *
     * Notes: the number of particles to create
     *
     * @return The current Number of particles
     */
    public int getNumberOfParticles() {
        return handle.getIntegers().read(0);
    }

    /**
     * Set Number of particles.
     *
     * @param value - new value.
     */
    public void setNumberOfParticles(int value) {
        handle.getIntegers().write(0, value);
    }

    /**
     * Retrieve Long Distance.
     *
     * Notes: if true, particle distance increases from 256 to 65536.
     *
     * @return The current Long Distance
     */
    public boolean getLongDistance() {
        return handle.getBooleans().read(0);
    }

    /**
     * Set Long Distance.
     *
     * @param value - new value.
     */
    public void setLongDistance(boolean value) {
        handle.getBooleans().write(0, value);
    }

    /**
     * Retrieve Data.
     *
     * Notes: length depends on particle. IRON_CRACK has a length of 2,
     * BLOCK_CRACK and BLOCK_DUST have lengths of 1, the rest have 0.
     *
     * @return The current Data
     * @see Particle#getDataLength()
     */
    public int[] getData() {
        return handle.getIntegerArrays().read(0);
    }

    /**
     * Set Data.
     *
     * @param value - new value.
     */
    public void setData(int[] value) {
        handle.getIntegerArrays().write(0, value);
    }
}