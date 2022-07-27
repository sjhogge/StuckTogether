package com.brooklynotter.stucktogether.commands;

import com.brooklynotter.stucktogether.StuckTogether;
import com.brooklynotter.stucktogether.configurations.ServerConfigurations;
import com.brooklynotter.stucktogether.data.SphereTeam;
import com.brooklynotter.stucktogether.packets.NetworkManager;
import com.brooklynotter.stucktogether.packets.StatusChangedPacket;
import com.brooklynotter.stucktogether.world.data.SphereTeamsData;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

import java.util.UUID;

import static com.brooklynotter.stucktogether.StuckTogether.SERVER;

// Following this guide: https://www.youtube.com/watch?v=bYH2i-KOLgk

public class StuckCommand {

    public static final String COMMAND_NAME = "stucktogether";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(COMMAND_NAME);

        root.then(Commands.literal("start")
                .executes(StuckCommand::startModule));

        root.then(Commands.literal("stop")
                .executes(StuckCommand::stopModule));

        root.then(Commands.literal("size")
                .then(Commands.argument("value", IntegerArgumentType.integer(5, 100))
                        .executes(ctx -> sizeModule(ctx, IntegerArgumentType.getInteger(ctx, "value"))))
        );

        root.then(Commands.literal("reloadcfg")
                .executes(StuckCommand::reloadConfigs));

        root.then(Commands.literal("teams")
                .then(Commands.literal("info")
                        .executes(StuckCommand::teamInfo))
                .then(Commands.literal("create")
                        .executes(StuckCommand::createTeam))
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> addMemberToTeam(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("kick")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> kickMemberFromTeam(ctx, EntityArgument.getPlayer(ctx, "player"))))));

        dispatcher.register(root);

    }

    public static int startModule(CommandContext<CommandSourceStack> context) {
        for (ServerPlayer player : SERVER.getPlayerList().getPlayers()) {
            TranslatableComponent successText = new TranslatableComponent("Stuck Together Started!");
            player.sendMessage(successText, player.getUUID());
            NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(true),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
        ServerConfigurations.SPHERE.active = true;
//        ServerConfigurations.saveDirtyConfigs();
        return 0;
    }

    public static int stopModule(CommandContext<CommandSourceStack> context) {
        for (ServerPlayer player : SERVER.getPlayerList().getPlayers()) {
            TranslatableComponent successText = new TranslatableComponent("Stuck Together Stopped!");
            player.sendMessage(successText, player.getUUID());
            NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(false),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
        ServerConfigurations.SPHERE.active = false;
//        ServerConfigurations.saveDirtyConfigs();
        return 0;
    }

    public static int sizeModule(CommandContext<CommandSourceStack> context, int sphereRadius) {
        for (ServerPlayer player : SERVER.getPlayerList().getPlayers()) {
            TranslatableComponent successText = new TranslatableComponent("Stuck Together Sphere Radius Updated to " + String.valueOf(sphereRadius));
            player.sendMessage(successText, player.getUUID());
        }
        ServerConfigurations.SPHERE.sphereRadius = sphereRadius;
//        ServerConfigurations.saveDirtyConfigs();
        return 1;
    }

    public static int reloadConfigs(CommandContext<CommandSourceStack> context) {
//        StuckTogether.LOGGER.info("Reloading server configs...");
//        ServerConfigurations.initialize();
//        StuckTogether.LOGGER.info("Reloading server configs succeeded!");
        return 0;
    }

    public static int teamInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
        SphereTeamsData data = SphereTeamsData.get(server);

        SphereTeam team = data.getTeamOf(sourcePlayer.getUUID());

        if (team == null) {
            MutableComponent text = new TranslatableComponent("commands.stucktogether.team.info.no_team")
                    .withStyle(ChatFormatting.AQUA);
            context.getSource().sendSuccess(text, true);
            return -1;
        }

        data.createTeam(server, sourcePlayer.getUUID());
        context.getSource().sendSuccess(new TranslatableComponent("commands.stucktogether.team.info.title").withStyle(ChatFormatting.BLUE), true);
        for (UUID member : team.getMembers()) {
            String nickname = server.getProfileCache().get(member).map(GameProfile::getName).orElse(member.toString());
            boolean isLeader = team.getLeader().equals(member);
            context.getSource().sendSuccess(new TextComponent((isLeader ? "\u2B50 " : "") + nickname)
                    .withStyle(isLeader ? ChatFormatting.GOLD : ChatFormatting.BLUE), true);
        }
        return 0;
    }

    public static int createTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
        SphereTeamsData data = SphereTeamsData.get(server);

        SphereTeam existingTeam = data.getTeamOf(sourcePlayer.getUUID());

        if (existingTeam != null) {
            context.getSource().sendFailure(new TranslatableComponent("commands.stucktogether.team.create.already_in_team"));
            return -1;
        }

        data.createTeam(server, sourcePlayer.getUUID());
        context.getSource().sendSuccess(new TranslatableComponent("commands.stucktogether.team.create.success"), true);
        return 0;
    }

    public static int addMemberToTeam(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
        SphereTeamsData data = SphereTeamsData.get(server);

        SphereTeam team = data.getTeamOf(sourcePlayer.getUUID());

        if (team == null) {
            // Not in a team
            context.getSource().sendFailure(new TranslatableComponent("commands.stucktogether.team.add.no_team"));
            return -1;
        }

        if (!team.getLeader().equals(sourcePlayer.getUUID())) {
            // Not the leader
            context.getSource().sendFailure(new TranslatableComponent("commands.stucktogether.team.add.not_leader"));
            return -1;
        }

        if (data.getTeamOf(player.getUUID()) != null) {
            // Player is already in team
            context.getSource().sendFailure(new TranslatableComponent("commands.stucktogether.team.add.already_in_team"));
            return -1;
        }

        data.addMember(server, team, player.getUUID());
        context.getSource().sendSuccess(new TranslatableComponent("commands.stucktogether.team.add.success", player.getDisplayName().getString()), true);
        return 0;
    }

    public static int kickMemberFromTeam(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
        SphereTeamsData data = SphereTeamsData.get(server);

        SphereTeam team = data.getTeamOf(sourcePlayer.getUUID());

        if (team == null) {
            // Not in a team
            context.getSource().sendFailure(new TranslatableComponent("commands.stucktogether.team.kick.no_team"));
            return -1;
        }

        if (!team.getLeader().equals(sourcePlayer.getUUID())) {
            // Not the leader
            context.getSource().sendFailure(new TranslatableComponent("commands.stucktogether.team.kick.not_leader"));
            return -1;
        }

        if (data.getTeamOf(player.getUUID()) != team) {
            // Player is not in this team
            context.getSource().sendFailure(new TranslatableComponent("commands.stucktogether.team.kick.not_in_this_team"));
            return -1;
        }

        data.removeMember(server, team, player.getUUID());
        context.getSource().sendSuccess(new TranslatableComponent("commands.stucktogether.team.kick.success", player.getDisplayName().getString()), true);
        return 0;
    }

}
