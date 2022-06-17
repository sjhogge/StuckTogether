package com.brooklynotter.stucktogether.data;

import com.brooklynotter.stucktogether.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public class SphereTeam implements INBTSerializable<CompoundTag> {

    protected @Nullable String teamName;
    protected @Nullable Color sphereColor; // For future uses, maybe? :P
    protected @NotNull UUID leader;
    protected @NotNull Set<UUID> members;

    public SphereTeam(CompoundTag tag) {
        this.leader = new UUID(0, 0);
        this.members = new HashSet<>();
        deserializeNBT(tag);
    }

    public SphereTeam(@NotNull UUID leader) {
        this.leader = leader;
        this.members = new HashSet<>(Collections.singleton(leader));
    }

    @Nullable
    public String getTeamName() {
        return teamName;
    }

    @Nullable
    public Color getSphereColor() {
        return sphereColor;
    }

    @NotNull
    public UUID getLeader() {
        return leader;
    }

    @NotNull
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public List<ServerPlayer> getOnlineMembers(MinecraftServer server) {
        return members.stream()
                .map(server.getPlayerList()::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    public BlockPos getCenter(MinecraftServer server) {
        float x = 0, y = 0, z = 0;
        int onlineCount = 0;

        for (ServerPlayer member : getOnlineMembers(server)) {
            BlockPos pos = member.getOnPos();
            x += pos.getX();
            y += pos.getY();
            z += pos.getZ();
            onlineCount++;
        }

        return new BlockPos(
                x / onlineCount,
                y / onlineCount,
                z / onlineCount);
    }

    public double percentToEdge(MinecraftServer server, Vec3 pos, float allowedRadius) {
        BlockPos center = getCenter(server);
        double dx = pos.x() - center.getX();
        double dy = pos.y() - center.getY();
        double dz = pos.z() - center.getZ();
        double radiusSq = allowedRadius * allowedRadius;
        return 1 - (radiusSq - (dx * dx + dy * dy + dz * dz)) / radiusSq;
    }

    public BlockPos randomRespawnPosition(MinecraftServer server) {
        List<BlockPos> respawnLocations = getOnlineMembers(server).stream()
                .map(ServerPlayer::getRespawnPosition)
                .toList();
        return respawnLocations.size() == 0
                ? server.overworld().getSharedSpawnPos()
                : respawnLocations.get(server.overworld().getRandom().nextInt(respawnLocations.size()));
    }

    public boolean hasMember(ServerPlayer player) {
        return hasMember(player.getUUID());
    }

    public boolean hasMember(UUID playerUUID) {
        return this.members.contains(playerUUID);
    }

    public void addMember(ServerPlayer player) {
        addMember(player.getUUID());
    }

    public void addMember(UUID playerUUID) {
        this.members.add(playerUUID);
    }

    public void removeMember(ServerPlayer player) {
        removeMember(player.getUUID());
    }

    public void removeMember(UUID playerUUID) {
        this.members.remove(playerUUID);
    }

    /* --------------------------- */

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        if (teamName != null) {
            nbt.putString("TeamName", teamName);
        }

        if (sphereColor != null) {
            nbt.putInt("SphereColor", sphereColor.getRGB());
        }

        nbt.put("Leader", NBTHelper.serializeUUID(leader));
        nbt.put("Members", NBTHelper.serializeCollection(members, NBTHelper::serializeUUID));

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("TeamName", Tag.TAG_STRING)) {
            this.teamName = nbt.getString("TeamName");
        }

        if (nbt.contains("SphereColor", Tag.TAG_INT)) {
            this.sphereColor = new Color(nbt.getInt("SphereColor"));
        }

        ListTag membersNBT = nbt.getList("Members", Tag.TAG_STRING);
        this.leader = UUID.fromString(nbt.getString("Leader"));
        this.members = NBTHelper.deserializeCollection(membersNBT, StringTag.class, HashSet::new, NBTHelper::deserializeUUID);
    }

}
