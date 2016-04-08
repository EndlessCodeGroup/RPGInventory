package com.comphenix.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public abstract class AbstractPlayServerWorldParticlesPacket extends AbstractPacket {
    /**
     * Constructs a new strongly typed wrapper for the given packet.
     *
     * @param handle - handle to the raw packet data.
     * @param type   - the packet type.
     */
    protected AbstractPlayServerWorldParticlesPacket(PacketContainer handle, PacketType type) {
        super(handle, type);
    }

    public abstract void setParticleType(EnumWrappers.Particle value);

    public abstract void setX(float value);

    public abstract void setY(float value);

    public abstract void setZ(float value);

    public abstract void setOffsetX(float value);

    public abstract void setOffsetY(float value);

    public abstract void setOffsetZ(float value);

    public abstract void setNumberOfParticles(int value);
}
