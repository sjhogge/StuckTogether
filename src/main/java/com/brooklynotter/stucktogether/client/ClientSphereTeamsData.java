package com.brooklynotter.stucktogether.client;

import com.brooklynotter.stucktogether.data.SphereTeam;
import com.brooklynotter.stucktogether.util.CollectionHelper;
import com.brooklynotter.stucktogether.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientSphereTeamsData {

    private static List<SphereTeam> teams = new ArrayList<>();

    public static void receiveTeamsSync(ListTag nbt) {
        teams = NBTHelper.deserializeCollection(nbt, CompoundTag.class, ArrayList::new, SphereTeam::new);
    }

    /* ------------------------------ */

    @Nullable
    public static SphereTeam getSphereOf(UUID playerUUID) {
        return CollectionHelper.find(teams, team -> team.hasMember(playerUUID));
    }

}
