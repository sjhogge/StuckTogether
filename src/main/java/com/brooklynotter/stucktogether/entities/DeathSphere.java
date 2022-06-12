package com.brooklynotter.stucktogether.entities;

import com.brooklynotter.stucktogether.StuckTogether;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = StuckTogether.MOD_ID )
public class DeathSphere {

    public static float sphereRadius;
    public static boolean active;
    public static BlockPos sphereRespawnPosition;

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
                sphere.SpawnSphereParticles(sphereRadius);

                boolean isPlayerOutsideSphere = false;

                for(Player player : playersInDimension) {
                    if (!checkIfPlayerInsideSphere(player.getOnPos(), center, sphereRadius)) {
                        isPlayerOutsideSphere = true;
                        ServerPlayer serverPlayer = (ServerPlayer) player;
                        sphereRespawnPosition = serverPlayer.getRespawnPosition();
                        break;
                    }
                }

                if (isPlayerOutsideSphere) {
                    for(Player player : playersInDimension) {
                        player.deathTime = 0;
                        player.kill();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void ImmediateRespawn(LivingDeathEvent event) {
        if(event.getEntityLiving() != null && event.getEntityLiving() instanceof Player){
            ServerPlayer player = (ServerPlayer) event.getEntityLiving();
            player.setRespawnPosition(Level.OVERWORLD, sphereRespawnPosition, 0, true, false);
        }
    }

    private static boolean checkIfPlayerInsideSphere(BlockPos playerPos, BlockPos spherePos, double sphereRadius) {
        double d0 = playerPos.getX() - spherePos.getX();
        double d1 = playerPos.getY() - spherePos.getY();
        double d2 = playerPos.getZ() - spherePos.getZ();
        return d0 * d0 + d1 * d1 + d2 * d2 < sphereRadius * sphereRadius;
    }
}
