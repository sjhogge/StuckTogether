package com.brooklynotter.stucktogether.entities;

import com.brooklynotter.stucktogether.StuckTogether;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.brooklynotter.stucktogether.StuckTogether.SERVER;

@Mod.EventBusSubscriber(modid = StuckTogether.MOD_ID )
public class DeathSphere {

    public static float sphereRadius;
    private static boolean generateParticleSphere;
    private static int particleSphereTicker;
    private final static int maxParticleSphereTicker = 5;
    public static boolean active;
    public static BlockPos sphereRespawnPosition;
    private static BlockPos sphereCenter;

    @SubscribeEvent
    public static void onWorldTickDoSphereOfDeath(TickEvent.WorldTickEvent event){
        if (event.phase == TickEvent.Phase.END && active) {

            Level world = event.world;
            List<Player> playersInDimension = (List<Player>) world.players();
            int numPlayers = playersInDimension.size();
            BlockPos center = new BlockPos(0,0,0);

            if (numPlayers > 0 && !world.isClientSide()){

                for(Player player : playersInDimension) {
                    BlockPos playerPos = player.getOnPos();
                    center = new BlockPos(center.getX() + playerPos.getX(), center.getY() + playerPos.getY(), center.getZ() + playerPos.getZ() );
                }

                ServerLevel serverlevel = (ServerLevel)world;
                center = new BlockPos(center.getX()/numPlayers, center.getY()/numPlayers, center.getZ()/numPlayers);
                ParticleSphere sphere = new ParticleSphere();
                sphere.world = serverlevel;
                sphere.center = center;
                sphereCenter = center;

                boolean playerIsOutsideSphere = false;
                double maxPercentDistanceToEdgeOfSphereForAlPlayers = 0;

                for(Player player : playersInDimension) {
                    double percentDistancePlayerIsToEdgeOfSphere = CheckPercentToEdge(player.getOnPos(), center, sphereRadius);
                    if (percentDistancePlayerIsToEdgeOfSphere >= 1) {
                        playerIsOutsideSphere = true;
                        generateNewRandomSpawnPosition(playersInDimension, serverlevel);
                        for(Player subPlayer : playersInDimension) {
                            ServerPlayer serverPlayer = (ServerPlayer)subPlayer;
                            serverPlayer.kill();
                        }
                        break;
                    } else {
                        maxPercentDistanceToEdgeOfSphereForAlPlayers =
                                Math.max(percentDistancePlayerIsToEdgeOfSphere, maxPercentDistanceToEdgeOfSphereForAlPlayers);
                    }
                }

                if (!playerIsOutsideSphere) {
                    if(particleSphereTicker >= maxParticleSphereTicker){
                        particleSphereTicker = 0;
                    } else {
                        particleSphereTicker++;
                    }
                    updateParticleSphere(sphere, maxPercentDistanceToEdgeOfSphereForAlPlayers);
                }
            }
        }
    }

    @SubscribeEvent
    public static void OnPlayerDeathSetSpawn(LivingDeathEvent event) {
        if(event.getEntityLiving() instanceof Player player && active) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            System.out.println("Setting player spawn to " + sphereRespawnPosition);
            serverPlayer.setRespawnPosition(Level.OVERWORLD, sphereRespawnPosition, 0, true, false);
            for(ServerPlayer otherServerPlayer : SERVER.getPlayerList().getPlayers()) {
                otherServerPlayer.kill();
            }
        }
    }

    public static BlockPos getSphereCenter() {
        return sphereCenter;
    }

    private static void generateNewRandomSpawnPosition(List<Player> players, ServerLevel serverlevel) {
        List<BlockPos> respawnPosList = new ArrayList<>();
        for(Player subPlayer : players) {
            ServerPlayer subServerPlayer = (ServerPlayer) subPlayer;
            if (subServerPlayer.getRespawnPosition() != null){
                respawnPosList.add(subServerPlayer.getRespawnPosition());
            }
        }
        Random rand = new Random();
        if (respawnPosList.size() > 0){
            sphereRespawnPosition = respawnPosList.get(rand.nextInt(respawnPosList.size()));
        } else {
            sphereRespawnPosition = serverlevel.getSharedSpawnPos();
        }
//        serverlevel.setDefaultSpawnPos(sphereRespawnPosition, 0);
    }

    private static double CheckPercentToEdge(BlockPos playerPos, BlockPos spherePos, double sphereRadius) {
        double d0 = playerPos.getX() - spherePos.getX();
        double d1 = playerPos.getY() - spherePos.getY();
        double d2 = playerPos.getZ() - spherePos.getZ();
        double sphereRadiusSquared = sphereRadius * sphereRadius;
        return 1 - (sphereRadiusSquared - (d0 * d0 + d1 * d1 + d2 * d2))/(sphereRadiusSquared);
    }

    private static void updateParticleSphere(ParticleSphere sphere, Double percentToEdge){
        ParticleOptions particleOptions;
        if (percentToEdge < 0.75D){
            particleOptions = ParticleTypes.ELECTRIC_SPARK;
        } else {
            particleOptions = ParticleTypes.SMALL_FLAME;
        }
        sphere.SpawnSphereParticles(sphereRadius, particleOptions, particleSphereTicker, maxParticleSphereTicker);
    }
}
