package com.brooklynotter.stucktogether.packets;

import com.brooklynotter.stucktogether.events.ScreenEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// Code taken from iGoodie's excellent project TwitchSpawn
public class StatusChangedPacket {

    public static void encode(StatusChangedPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.status);
    }

    public static StatusChangedPacket decode(FriendlyByteBuf buffer) {
        return new StatusChangedPacket(buffer.readBoolean());
    }

    public static void handle(final StatusChangedPacket packet,
                              Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ScreenEvents.setDeathSphereActive(packet.status);
        });
        context.get().setPacketHandled(true);
    }

    /* ---------------------------- */

    private boolean status;

    public StatusChangedPacket(boolean status) {
        this.status = status;
    }

}