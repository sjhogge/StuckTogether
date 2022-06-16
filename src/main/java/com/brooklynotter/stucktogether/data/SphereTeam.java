package com.brooklynotter.stucktogether.data;

import com.brooklynotter.stucktogether.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SphereTeam implements INBTSerializable<CompoundTag> {

    protected @Nullable String teamName;
    protected @Nullable Color sphereColor; // For future uses, maybe? :P
    protected @Nonnull Set<UUID> members;

    public SphereTeam() {
        this.members = new HashSet<>();
    }

    @Nullable
    public String getTeamName() {
        return teamName;
    }

    @Nullable
    public Color getSphereColor() {
        return sphereColor;
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
        this.members = NBTHelper.deserializeCollection(membersNBT, StringTag.class, HashSet::new, NBTHelper::deserializeUUID);
    }

}
