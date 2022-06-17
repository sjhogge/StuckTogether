package com.brooklynotter.stucktogether.events;

import com.brooklynotter.stucktogether.StuckTogether;
import com.brooklynotter.stucktogether.client.particles.ParticleSphere;
import com.brooklynotter.stucktogether.data.SphereTeam;
import com.brooklynotter.stucktogether.packets.NetworkManager;
import com.brooklynotter.stucktogether.packets.StatusChangedPacket;
import com.brooklynotter.stucktogether.world.data.SphereTeamsData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import java.util.List;
import java.util.UUID;

import static com.brooklynotter.stucktogether.StuckTogether.SERVER;

@Mod.EventBusSubscriber(modid = StuckTogether.MOD_ID)
public class DeathSphereEvents {

    // TODO: Turn those 2 variables into actual configs
    public static boolean active;
    public static float sphereRadius;

    private static int particleSphereTicker;
    private final static int MAX_PARTICLE_SPHERE_TICKER = 5;

    @SubscribeEvent
    public static void onServerTickDoSphereOfDeath(TickEvent.ServerTickEvent event) {
        if (!active) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (event.type != TickEvent.Type.SERVER) return; // How would this be possible?
        if (SERVER == null) return;

        teamLoop:
        for (SphereTeam team : SphereTeamsData.get(SERVER).getTeams()) {
            List<ServerPlayer> onlineMembers = team.getOnlineMembers(SERVER);
            BlockPos teamCenter = team.getCenter(SERVER);
            double maxPercentToEdge = 0;

            ParticleSphere sphere = new ParticleSphere();
            sphere.center = teamCenter;

            for (ServerPlayer member : onlineMembers) {
                sphere.world = member.getLevel();

                double percentToEdge = team.percentToEdge(SERVER, member.position(), sphereRadius);

                if (percentToEdge >= 1) {
                    BlockPos respawnPosition = team.randomRespawnPosition(SERVER);
                    onlineMembers.forEach(player -> {
                        player.setRespawnPosition(member.getLevel().dimension(), respawnPosition, 0, true, false);
                        player.kill();
                    });
                    break teamLoop;

                } else {
                    maxPercentToEdge = Math.max(maxPercentToEdge, percentToEdge);
                }
            }

            if (onlineMembers.size() != 0) {
                ParticleOptions particleType = maxPercentToEdge < 0.075
                        ? ParticleTypes.ELECTRIC_SPARK
                        : ParticleTypes.SMALL_FLAME;
                sphere.spawnSphereParticles(sphereRadius, particleType, particleSphereTicker, MAX_PARTICLE_SPHERE_TICKER);
            }

        }

        particleSphereTicker = (particleSphereTicker + 1) % MAX_PARTICLE_SPHERE_TICKER; // Circular increment
    }

    @SubscribeEvent
    public static void onPlayerDeathSetSpawn(LivingDeathEvent event) {
        if (!active) return;
        if (!(event.getEntityLiving() instanceof ServerPlayer dyingPlayer)) return;

        MinecraftServer server = dyingPlayer.getServer();
        if (server == null) return;

        SphereTeamsData teamsData = SphereTeamsData.get(server);
        SphereTeam team = teamsData.getTeamOf(dyingPlayer.getUUID());
        if (team == null) return;

        BlockPos respawnPosition = team.randomRespawnPosition(server);

        for (ServerPlayer member : team.getOnlineMembers(server)) {
            member.setRespawnPosition(dyingPlayer.getLevel().dimension(), respawnPosition, 0, true, false);
            member.kill();
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!active) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        SphereTeamsData teamsData = SphereTeamsData.get(server);
        SphereTeam team = teamsData.getTeamOf(player.getUUID());
        if (team == null) return;

        for (UUID memberUUID : team.getMembers()) {
            ServerPlayer memberPlayer = server.getPlayerList().getPlayer(memberUUID);
            if (memberPlayer != null && !memberUUID.equals(player.getUUID())) {
                memberPlayer.teleportTo(
                        player.getLevel(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getYRot(),
                        player.getXRot()
                );
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        player.sendMessage(new TranslatableComponent("Welcome to your doom :)"), player.getUUID());
        player.sendMessage(new TranslatableComponent("Joined Stuck Together!"), player.getUUID());
        NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(active),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT);

        if (!DeathSphereEvents.active) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        SphereTeamsData teamsData = SphereTeamsData.get(server);
        SphereTeam team = teamsData.getTeamOf(player.getUUID());
        if (team == null) return;

        for (ServerPlayer member : team.getOnlineMembers(server)) {
            if (member != player) {
                player.teleportTo(
                        member.getLevel(),
                        member.getX(),
                        member.getY(),
                        member.getZ(),
                        member.getYRot(),
                        member.getXRot()
                );
                break;
            }
        }
    }

}
