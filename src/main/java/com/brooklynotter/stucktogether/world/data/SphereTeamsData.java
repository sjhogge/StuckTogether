package com.brooklynotter.stucktogether.world.data;

import com.brooklynotter.stucktogether.StuckTogether;
import com.brooklynotter.stucktogether.data.SphereTeam;
import com.brooklynotter.stucktogether.packets.NetworkManager;
import com.brooklynotter.stucktogether.packets.SyncSphereTeamsPacket;
import com.brooklynotter.stucktogether.util.CollectionHelper;
import com.brooklynotter.stucktogether.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SphereTeamsData extends SavedData {

    public static final String DATA_NAME = String.format("%s_SphereTeams", StuckTogether.MOD_ID);

    private List<SphereTeam> teams;

    public SphereTeamsData() {
        this.teams = new ArrayList<>();
    }

    public List<SphereTeam> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    @Nullable
    public SphereTeam getTeamOf(UUID playerUUID) {
        return CollectionHelper.find(teams, team -> team.hasMember(playerUUID));
    }

    /* ------------------------- */

    public void createTeam(MinecraftServer server, UUID leaderUUID) {
        if (getTeamOf(leaderUUID) != null) return;

        SphereTeam sphereTeam = new SphereTeam(leaderUUID);
        teams.add(sphereTeam);

        setDirty();
        sync(server);
    }

    public void addMember(MinecraftServer server, SphereTeam team, UUID playerUUID) {
        if (getTeamOf(playerUUID) != null) return;

        team.addMember(playerUUID);

        setDirty();
        sync(server);
    }

    public void removeMember(MinecraftServer server, SphereTeam team, UUID playerUUID) {
        if (getTeamOf(playerUUID) == null) return;

        team.removeMember(playerUUID);

        setDirty();
        sync(server);
    }

    public void removeTeam(MinecraftServer server, SphereTeam team, UUID playerUUID) {
        if (getTeamOf(playerUUID) == null) return;

        teams.remove(team);

        setDirty();
        sync(server);
    }

    /* ------------------------- */

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        nbt.put("Teams", NBTHelper.serializeCollection(teams, SphereTeam::serializeNBT));
        return nbt;
    }

    public static SphereTeamsData load(CompoundTag nbt) {
        SphereTeamsData data = new SphereTeamsData();
        if (nbt.contains("Teams", Tag.TAG_LIST)) {
            ListTag teamsNBT = nbt.getList("Teams", Tag.TAG_COMPOUND);
            data.teams = NBTHelper.deserializeCollection(teamsNBT, CompoundTag.class, ArrayList::new, SphereTeam::new);
        }
        return data;
    }

    public void sync(MinecraftServer server) {
        SyncSphereTeamsPacket packet = new SyncSphereTeamsPacket(NBTHelper.serializeCollection(teams, SphereTeam::serializeNBT));
        NetworkManager.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    /* ------------------------- */

    public static SphereTeamsData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(SphereTeamsData::load, SphereTeamsData::new, DATA_NAME);
    }

}
