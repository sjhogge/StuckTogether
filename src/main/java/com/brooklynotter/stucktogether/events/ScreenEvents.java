package com.brooklynotter.stucktogether.events;

import com.brooklynotter.stucktogether.StuckTogether;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StuckTogether.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScreenEvents {

    public static Screen playerDeathScreen;
    private static boolean deathSphereActive;
    private static int respawnDelayTicker;

    public static void setDeathSphereActive(boolean active){
        deathSphereActive = active;
    }

    @SubscribeEvent
    public static void ImmediateRespawn(ScreenOpenEvent event) {
        Screen screen = event.getScreen();
        if (screen instanceof DeathScreen deathScreen) {
            playerDeathScreen = deathScreen;
        }
    }

    @SubscribeEvent
    public static void handleDelayTicker(TickEvent.ClientTickEvent event) {
        if (deathSphereActive && event.phase == TickEvent.Phase.END){
            if (respawnDelayTicker != 40 && ScreenEvents.playerDeathScreen != null){
                respawnDelayTicker++;
//                System.out.println("Incrementing delay ticker");
            } else if (ScreenEvents.playerDeathScreen != null) {
                Double[] clickPos = {((double)ScreenEvents.playerDeathScreen.width)/2 - 100, ((double)ScreenEvents.playerDeathScreen.height)/4 + 72};
                ScreenEvents.playerDeathScreen.mouseClicked(clickPos[0], clickPos[1], 0); // 0 for left button
                MinecraftForge.EVENT_BUS.post(new ScreenEvent.MouseClickedEvent.Pre(ScreenEvents.playerDeathScreen, clickPos[0], clickPos[1], 0));
                ScreenEvents.playerDeathScreen = null;
//                System.out.println("Handle death screen here!");
                respawnDelayTicker = 0;
            }
        }
    }
}
