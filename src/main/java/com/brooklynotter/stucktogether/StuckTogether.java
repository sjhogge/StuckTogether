package com.brooklynotter.stucktogether;

import com.brooklynotter.stucktogether.commands.StuckCommand;
import com.brooklynotter.stucktogether.entities.DeathSphere;
import com.google.common.base.Ticker;
import com.mojang.bridge.game.GameSession;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.stream.Collectors;

//Following this tutorial series: https://youtu.be/eqY17yWENEI
//To RUN the Gradle build : go to the Gradle Tab -> Tasks -> forgegradle runs -> runClient
// The value here should match an entry in the META-INF/mods.toml file
@Mod(StuckTogether.MOD_ID)
public class StuckTogether
{
    public static final String MOD_ID = "stucktogether";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static MinecraftServer SERVER;
    public static DeathSphere SPHEREOFDEATH;

    public StuckTogether() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        StuckCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        SERVER = event.getServer();
    }

    @SubscribeEvent
    public void onServerStarting (ServerStartingEvent event) {
        SERVER.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, SERVER);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DeathSphere.active = false;
        DeathSphere.sphereRadius = 10;

    }
}
