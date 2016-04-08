package com.comphenix.packetwrapper.v1_7_R4;

import com.comphenix.packetwrapper.AbstractPlayServerWorldParticlesPacket;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

@SuppressWarnings("ALL")
public class WrapperPlayServerWorldParticles extends AbstractPlayServerWorldParticlesPacket {
    private static final PacketType TYPE;

    static {
        TYPE = PacketType.Play.Server.WORLD_PARTICLES;
    }

    /**
     * Construct a new particle packet.
     */
    public WrapperPlayServerWorldParticles() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    /**
     * Construct a particle packet that reads and modifies a given native packet.
     *
     * @param packet - the native packet.
     */
    public WrapperPlayServerWorldParticles(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Retrieve the name of the particle to create. A list can be found here.
     *
     * @return The current Particle name
     */
    private String getParticleName() {
        return handle.getStrings().read(0);
    }

    /**
     * Set the name of the particle to create. A list can be found here.
     *
     * @param value - new value.
     */
    private void setParticleName(String value) {
        handle.getStrings().write(0, value);
    }

    /**
     * Retrieve the particle effect.
     *
     * @return The particle effect, or NULL if not known.
     */
    public EnumWrappers.Particle getParticleType() {
        return EnumWrappers.Particle.getByName(getParticleName());
    }

    /**
     * Set the particle effect to use.
     *
     * @param effect - the particle effect.
     */
    public void setParticleType(EnumWrappers.Particle effect) {
        if (effect == null) {
            throw new IllegalArgumentException("effect cannot be NULL.");
        }

        setParticleName(effect.getName());
    }

    /**
     * Retrieve the location of the current particle.
     *
     * @param event - the packet event.
     * @return The location.
     */
    public Location getLocation(PacketEvent event) {
        return getLocation(event.getPlayer().getWorld());
    }

    /**
     * Retrieve the location of the current particle.
     *
     * @param world - the containing world.
     * @return The location.
     */
    private Location getLocation(World world) {
        return new Location(world, getX(), getY(), getZ());
    }

    /**
     * Set the location of the particle to send.
     *
     * @param loc - the location.
     */
    public void setLocation(Location loc) {
        if (loc == null) {
            throw new IllegalArgumentException("Location cannot be NULL.");
        }

        setX((float) loc.getX());
        setY((float) loc.getY());
        setZ((float) loc.getZ());
    }

    /**
     * Retrieve the random offset that will be multiplied by a random gaussian and applied to each created particle.
     *
     * @return The random offset.
     */
    public Vector getOffset() {
        return new Vector(getX(), getY(), getZ());
    }

    /**
     * Set the random offset (multiplied by a random gaussian) to be applied after the particles are created.
     *
     * @param vector - the random vector offset.
     */
    public void setOffset(Vector vector) {
        if (vector == null)
            throw new IllegalArgumentException("Vector cannot be NULL.");
        setOffsetX((float) vector.getX());
        setOffsetY((float) vector.getY());
        setOffsetZ((float) vector.getZ());
    }

    /**
     * Retrieve the x position of the particle.
     *
     * @return The current position.
     */
    private float getX() {
        return handle.getFloat().read(0);
    }

    /**
     * Set the x position of the particle.
     *
     * @param value - new position.
     */
    public void setX(float value) {
        handle.getFloat().write(0, value);
    }

    /**
     * Retrieve the y position of the particle.
     *
     * @return The current Y position.
     */
    private float getY() {
        return handle.getFloat().read(1);
    }

    /**
     * Set the y position of the particle.
     *
     * @param value - new position.
     */
    public void setY(float value) {
        handle.getFloat().write(1, value);
    }

    /**
     * Retrieve the z position of the particle.
     *
     * @return The current Z position.
     */
    private float getZ() {
        return handle.getFloat().read(2);
    }

    /**
     * Set the z position of the particle.
     *
     * @param value - new position.
     */
    public void setZ(float value) {
        handle.getFloat().write(2, value);
    }

    /**
     * Retrieve the offset added to the X position after being multiplied by random.nextGaussian().
     *
     * @return The current Offset X
     */
    public float getOffsetX() {
        return handle.getFloat().read(3);
    }

    /**
     * Set this the offset added to the X position after being multiplied by random.nextGaussian().
     *
     * @param value - new value.
     */
    public void setOffsetX(float value) {
        handle.getFloat().write(3, value);
    }

    /**
     * Retrieve the offset added to the Y position after being multiplied by random.nextGaussian().
     *
     * @return The current Offset Y
     */
    public float getOffsetY() {
        return handle.getFloat().read(4);
    }

    /**
     * Set the offset added to the Y position after being multiplied by random.nextGaussian().
     *
     * @param value - new value.
     */
    public void setOffsetY(float value) {
        handle.getFloat().write(4, value);
    }

    /**
     * Retrieve the offset added to the Z position after being multiplied by random.nextGaussian().
     *
     * @return The current Offset Z
     */
    public float getOffsetZ() {
        return handle.getFloat().read(5);
    }

    /**
     * Set offset added to the Z position after being multiplied by random.nextGaussian().
     *
     * @param value - new value.
     */
    public void setOffsetZ(float value) {
        handle.getFloat().write(5, value);
    }

    /**
     * Retrieve the speed of each particle.
     *
     * @return The current particle speed
     */
    public float getParticleSpeed() {
        return handle.getFloat().read(6);
    }

    /**
     * Set the speed of each particle.
     *
     * @param value - new speed.
     */
    public void setParticleSpeed(float value) {
        handle.getFloat().write(6, value);
    }

    /**
     * Retrieve the number of particles to create.
     *
     * @return The current number of particles
     */
    public int getNumberOfParticles() {
        return handle.getIntegers().read(0);
    }

    /**
     * Set the number of particles to create.
     *
     * @param value - new count.
     */
    public void setNumberOfParticles(int value) {
        handle.getIntegers().write(0, value);
    }
}