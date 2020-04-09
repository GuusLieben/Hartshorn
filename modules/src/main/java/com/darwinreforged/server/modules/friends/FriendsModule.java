package com.darwinreforged.server.modules.friends;

import com.darwinreforged.server.sponge.DarwinServer;
import com.darwinreforged.server.core.modules.ModuleInfo;
import com.darwinreforged.server.core.modules.PluginModule;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.modules.friends.commands.FriendsAddCommand;
import com.darwinreforged.server.modules.friends.commands.FriendsListCommand;
import com.darwinreforged.server.modules.friends.commands.FriendsRemoveCommand;
import com.darwinreforged.server.modules.friends.commands.FriendsTeleportCommand;
import com.darwinreforged.server.modules.friends.commands.FriendsToggleCommand;
import com.darwinreforged.server.modules.friends.util.FriendsStorage;
import com.darwinreforged.server.modules.friends.util.FriendsStorageManager;

import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.UUID;

@ModuleInfo(id = "darwinfriends", name = "Darwin Friends", version = "1.0", description = "Friend system for Darwin Reforged")
public class FriendsModule extends PluginModule {

    public HashMap<UUID, FriendsStorage> users = new HashMap<>();

    public FriendsModule() {
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        FriendsStorageManager.setup();
    }

    @Listener
    public void onServerFinishLoad(GameStartedServerEvent event) {
        DarwinServer.registerCommand(friendsCommand, "friend");
    }

    CommandSpec teleportCommand = CommandSpec.builder()
            .permission(Permissions.FRIENDS_USE.p())
            .arguments(GenericArguments.player(Text.of("online player")))
            .executor(new FriendsTeleportCommand())
            .build();
    CommandSpec listCommand = CommandSpec.builder()
            .permission(Permissions.FRIENDS_USE.p())
            .executor(new FriendsListCommand())
            .build();
    CommandSpec toggleCommand = CommandSpec.builder()
            .permission(Permissions.FRIENDS_USE.p())
            .executor(new FriendsToggleCommand())
            .build();
    CommandSpec addCommand = CommandSpec.builder()
            .permission(Permissions.FRIENDS_USE.p())
            .arguments(GenericArguments.player(Text.of("online player")))
            .executor(new FriendsAddCommand())
            .build();
    CommandSpec removeCommand = CommandSpec.builder()
            .permission(Permissions.FRIENDS_USE.p())
            .arguments(GenericArguments.user(Text.of("online/offline player")))
            .executor(new FriendsRemoveCommand())
            .build();
    CommandSpec friendsCommand = CommandSpec.builder()
            .permission(Permissions.FRIENDS_USE.p())
            .child(addCommand, "add")
            .child(removeCommand, "remove")
            .child(listCommand, "list")
            .child(toggleCommand, "toggle")
            .child(teleportCommand, "tp")
            .build();
}
