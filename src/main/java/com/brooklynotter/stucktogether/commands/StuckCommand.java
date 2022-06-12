package com.brooklynotter.stucktogether.commands;

import com.brooklynotter.stucktogether.StuckTogether;
import com.brooklynotter.stucktogether.entities.DeathSphere;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import static com.brooklynotter.stucktogether.StuckTogether.SPHEREOFDEATH;

// Following this guide: https://www.youtube.com/watch?v=bYH2i-KOLgk

public class StuckCommand {

    public static final String COMMAND_NAME = "stucktogether";
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(COMMAND_NAME);

        root.then(Commands.literal("start").executes(StuckCommand::startModule));
        root.then(Commands.literal("stop").executes(StuckCommand::stopModule));
        root.then(Commands.literal("size")
                .then(Commands.argument("value", IntegerArgumentType.integer(3, 100))
                        .executes(context -> sizeModule(context, IntegerArgumentType.getInteger(context, "value"))))
        );

        dispatcher.register(root);

    }

    public static int startModule(CommandContext<CommandSourceStack> context){
        DeathSphere.active = true;
        return 0;
    }
    public static int stopModule(CommandContext<CommandSourceStack> context){
        DeathSphere.active = false;
        return 0;
    }
    public static int sizeModule(CommandContext<CommandSourceStack> context, int sphereRadius){
        DeathSphere.sphereRadius = sphereRadius;
        return 1;
    }
}
