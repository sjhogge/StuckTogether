package com.brooklynotter.stucktogether.world.data;

import com.brooklynotter.stucktogether.StuckTogether;
import com.brooklynotter.stucktogether.data.SphereTeam;
import com.brooklynotter.stucktogether.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SphereTeamsData extends SavedData {

    public static final String DATA_NAME = String.format("%s_SphereTeams", StuckTogether.MOD_ID);

    private List<SphereTeam> teams;

    public SphereTeamsData() {
        this.teams = new ArrayList<>();
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
            data.teams = NBTHelper.deserializeCollection(teamsNBT, CompoundTag.class, ArrayList::new, NBTHelper.deserializer(SphereTeam::new));
        }
        return data;
    }

    /* ------------------------- */

    public static SphereTeamsData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(SphereTeamsData::load, SphereTeamsData::new, DATA_NAME);
    }

}
