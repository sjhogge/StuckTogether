package com.brooklynotter.stucktogether;

import com.brooklynotter.stucktogether.commands.StuckCommand;
import com.brooklynotter.stucktogether.entities.DeathSphere;
import com.brooklynotter.stucktogether.packets.NetworkManager;
import com.brooklynotter.stucktogether.packets.StatusChangedPacket;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
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
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        player.sendMessage(new TranslatableComponent("Welcome to your doom :)"), player.getUUID());
        if (DeathSphere.active) {
            TranslatableComponent successText = new TranslatableComponent("Joined Stuck Together!");
            player.sendMessage(successText, player.getUUID());
            NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(true),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);

            for (ServerPlayer otherPlayer : SERVER.getPlayerList().getPlayers()) {
                if(otherPlayer != player && otherPlayer.getLevel() == player.getLevel()){
                    player.teleportTo(otherPlayer.getBlockX(), otherPlayer.getBlockY(), otherPlayer.getBlockZ());
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DeathSphere.active = false;
        DeathSphere.sphereRadius = 10;
        DeathSphere.sphereRespawnPosition = SERVER.getLevel(Level.OVERWORLD).getSharedSpawnPos();
    }
}
