package com.brooklynotter.stucktogether.packets;

import com.brooklynotter.stucktogether.client.data.ClientSphereTeamsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncSphereTeamsPacket(ListTag teamsNbt) {

    /* ---------------------------- */

    public static void encode(SyncSphereTeamsPacket packet, FriendlyByteBuf buffer) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("Teams", packet.teamsNbt);
        buffer.writeNbt(nbt);
    }

    public static SyncSphereTeamsPacket decode(FriendlyByteBuf buffer) {
        CompoundTag nbt = buffer.readNbt();
        if (nbt == null || !nbt.contains("Teams", Tag.TAG_LIST)) throw new InternalError();
        ListTag teamsNbt = nbt.getList("Teams", Tag.TAG_COMPOUND);
        return new SyncSphereTeamsPacket(teamsNbt);
    }

    public static void handle(final SyncSphereTeamsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientSphereTeamsData.receiveTeamsSync(packet.teamsNbt);
        });
        context.get().setPacketHandled(true);
    }

}