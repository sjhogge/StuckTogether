package com.brooklynotter.stucktogether.entities;

import com.brooklynotter.stucktogether.StuckTogether;
import com.brooklynotter.stucktogether.events.ScreenEvents;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.List;

import static com.brooklynotter.stucktogether.StuckTogether.LOGGER;

@Mod.EventBusSubscriber(modid = StuckTogether.MOD_ID )
public class DeathSphere {

    public static final DamageSource SPHERE_OF_DEATH = (new DamageSource("sphereOfDeath")).bypassArmor();
    public static float sphereRadius;
    public static boolean active;
    public static BlockPos sphereRespawnPosition;

//    private static int respawnDelayTicker;

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

                boolean playerIsOutsideSphere = false;
                double maxPercentDistanceToEdgeOfSphereForAlPlayers = 0;

                for(Player player : playersInDimension) {
                    double percentDistancePlayerIsToEdgeOfSphere = CheckPercentDistancePlayerIsFromCenterToEdgeOfSphere(player.getOnPos(), center, sphereRadius);
                    if (percentDistancePlayerIsToEdgeOfSphere >= 1) {
                        playerIsOutsideSphere = true;
                        ServerPlayer serverPlayer = (ServerPlayer) player;
                        sphereRespawnPosition = serverPlayer.getRespawnPosition();
                        break;
                    } else {
                        maxPercentDistanceToEdgeOfSphereForAlPlayers =
                                Math.max(percentDistancePlayerIsToEdgeOfSphere, maxPercentDistanceToEdgeOfSphereForAlPlayers);
                    }
                }

                if (playerIsOutsideSphere) {
                    for(Player player : playersInDimension) {
                        player.die(SPHERE_OF_DEATH);
                    }
                } else {
                    ParticleOptions particleOptions;
                    if (maxPercentDistanceToEdgeOfSphereForAlPlayers < 0.75D){
                        particleOptions = ParticleTypes.ELECTRIC_SPARK;
                    } else {
                        particleOptions = ParticleTypes.SMALL_FLAME;
                    }
                    sphere.SpawnSphereParticles(sphereRadius, particleOptions);
                }
            }
        }
    }
    
    private static double CheckPercentDistancePlayerIsFromCenterToEdgeOfSphere(BlockPos playerPos, BlockPos spherePos, double sphereRadius) {
        double d0 = playerPos.getX() - spherePos.getX();
        double d1 = playerPos.getY() - spherePos.getY();
        double d2 = playerPos.getZ() - spherePos.getZ();
        double sphereRadiusSquared = sphereRadius * sphereRadius;
        return 1 - (sphereRadiusSquared - (d0 * d0 + d1 * d1 + d2 * d2))/(sphereRadiusSquared);
    }
}
