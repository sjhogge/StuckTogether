package com.brooklynotter.stucktogether.packets;

import com.brooklynotter.stucktogether.StuckTogether;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(StuckTogether.MOD_ID, "network"))
            .clientAcceptedVersions(v -> v.equals(PROTOCOL_VERSION))
            .serverAcceptedVersions(v -> v.equals(PROTOCOL_VERSION))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void initialize() {
        CHANNEL.registerMessage(0, StatusChangedPacket.class,
                StatusChangedPacket::encode,
                StatusChangedPacket::decode,
                StatusChangedPacket::handle);

        CHANNEL.registerMessage(1, SyncSphereTeamsPacket.class,
                SyncSphereTeamsPacket::encode,
                SyncSphereTeamsPacket::decode,
                SyncSphereTeamsPacket::handle);
    }
}
