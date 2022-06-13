package com.brooklynotter.stucktogether.commands;

import com.brooklynotter.stucktogether.entities.DeathSphere;
import com.brooklynotter.stucktogether.packets.NetworkManager;
import com.brooklynotter.stucktogether.packets.StatusChangedPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

import static com.brooklynotter.stucktogether.StuckTogether.SERVER;

// Following this guide: https://www.youtube.com/watch?v=bYH2i-KOLgk

public class StuckCommand {

    public static final String COMMAND_NAME = "stucktogether";
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(COMMAND_NAME);

        root.then(Commands.literal("start").executes(StuckCommand::startModule));
        root.then(Commands.literal("stop").executes(StuckCommand::stopModule));
        root.then(Commands.literal("size")
                .then(Commands.argument("value", IntegerArgumentType.integer(5, 100))
                        .executes(context -> sizeModule(context, IntegerArgumentType.getInteger(context, "value"))))
        );

        dispatcher.register(root);

    }

    public static int startModule(CommandContext<CommandSourceStack> context){
        for(ServerPlayer player : SERVER.getPlayerList().getPlayers()){
            TranslatableComponent successText = new TranslatableComponent("Stuck Together Started!");
            player.sendMessage(successText, player.getUUID());
            NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(true),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
        DeathSphere.active = true;
        return 0;
    }
    public static int stopModule(CommandContext<CommandSourceStack> context){
        for(ServerPlayer player : SERVER.getPlayerList().getPlayers()){
            TranslatableComponent successText = new TranslatableComponent("Stuck Together Stopped!");
            player.sendMessage(successText, player.getUUID());
            NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(false),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
        DeathSphere.active = false;
        return 0;
    }
    public static int sizeModule(CommandContext<CommandSourceStack> context, int sphereRadius){
        for(ServerPlayer player : SERVER.getPlayerList().getPlayers()) {
            TranslatableComponent successText = new TranslatableComponent("Stuck Together Sphere Radius Updated to " + String.valueOf(sphereRadius));
            player.sendMessage(successText, player.getUUID());
        }
        DeathSphere.sphereRadius = sphereRadius;
        return 1;
    }
}
