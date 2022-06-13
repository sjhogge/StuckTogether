package com.brooklynotter.stucktogether;

import com.brooklynotter.stucktogether.commands.StuckCommand;
import com.brooklynotter.stucktogether.entities.DeathSphere;
import com.brooklynotter.stucktogether.packets.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//Following this tutorial series: https://youtu.be/eqY17yWENEI
//To RUN the Gradle build : go to the Gradle Tab -> Tasks -> forgegradle runs -> runClient
// The value here should match an entry in the META-INF/mods.toml file
@Mod(StuckTogether.MOD_ID)
public class StuckTogether
{
    public static final String MOD_ID = "stucktogether";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static MinecraftServer SERVER;
    public static DeathSphere SPHEREOFDEATH;

    public StuckTogether() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkManager.initialize();
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
        // Do Nothing
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DeathSphere.active = false;
        DeathSphere.sphereRadius = 10;
    }
}
