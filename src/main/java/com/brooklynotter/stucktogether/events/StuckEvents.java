package com.brooklynotter.stucktogether.events;
import com.brooklynotter.stucktogether.StuckTogether;
import com.brooklynotter.stucktogether.entities.ParticleSphere;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = StuckTogether.MOD_ID )
public class StuckEvents {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer entity = (ServerPlayer) event.getPlayer();
        entity.sendMessage(new TranslatableComponent("Welcome to your doom :)"), entity.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerCloneEvent(PlayerEvent.Clone event) {
        if(!event.getOriginal().isLocalPlayer()) {
            event.getPlayer().getPersistentData().putIntArray(StuckTogether.MOD_ID + "homepos",
                    event.getOriginal().getPersistentData().getIntArray(StuckTogether.MOD_ID + "homepos"));
            UUID uuid = event.getPlayer().getUUID();
        }
    }

//    @SubscribeEvent
//    public static void onBreakBlock(BlockEvent.BreakEvent event){
//        if(event.getPlayer() instanceof Player) {
//            Player player = event.getPlayer();
//            Level level = player.level;
////            UUID uuid = player.getUUID();
////            BlockPos playerPos = player.getOnPos();
//            BlockPos blockPos = event.getPos();
//            if (!level.isClientSide) {
//                ServerLevel serverlevel = (ServerLevel)level;
//                ParticleSphere sphere = new ParticleSphere();
//                sphere.level = serverlevel;
//                sphere.center = blockPos;
//                sphere.SpawnSphereParticles();
//            }
//            //Block anvil = Blocks.ANVIL;
//            //event.getWorld().setBlock(playerPos.above(15), anvil.defaultBlockState(), 1);
//            // spawn anvil at player pos + 10 blocks up
//        }
//


//    @SubscribeEvent
//    public static void onPlayerTickDrawSphereOfDeath (TickEvent.PlayerTickEvent event) {
//        if (event.phase == TickEvent.Phase.END) {
//            Player player = event.player;
//            Level level = player.getLevel();
//            if (!level.isClientSide) {
//                ServerLevel serverlevel = (ServerLevel)level;
//                BlockPos center = player.getOnPos();
//                ParticleSphere sphere = new ParticleSphere();
//                sphere.level = serverlevel;
//                sphere.center = center;
//                sphere.SpawnSphereParticles();
//            }
//        }
//    }


//    @SubscribeEvent
//    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        //Tick events happen at the start and end of each tick, so just pick one to do the actions during
//        if(event.phase == TickEvent.Phase.START) {
//            Player player = event.player;
//            UUID uuid = player.getUUID();
//            HitResult blockResult = player.pick(20.0D, 0, false);
//
//            if (blockResult.getType() == HitResult.Type.BLOCK) {
//                BlockPos blockpos = ((BlockHitResult) blockResult).getBlockPos();
//                BlockState blockstate = player.level.getBlockState(blockpos);
//                if (blockstate != Blocks.BEDROCK.defaultBlockState()) {
//                    Block.updateOrDestroy(blockstate, Blocks.BEDROCK.defaultBlockState(), player.getLevel(), blockpos, 1);
//                    player.sendMessage(new TranslatableComponent("Block turned to bedrock!"), uuid);
//                }
//            }
//        }
//    }

}
