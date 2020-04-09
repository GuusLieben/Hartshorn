package com.darwinreforged.server.modules.friends.commands;

import com.darwinreforged.server.core.resources.Translations;
import com.darwinreforged.server.sponge.utils.PlayerUtils;
import com.darwinreforged.server.modules.friends.util.FriendsStorage;
import com.darwinreforged.server.modules.friends.util.FriendsStorageManager;
import com.darwinreforged.server.modules.friends.util.FriendsUtil;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

public class FriendsRemoveCommand
        implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        //get the online target for adding a friend and the source player
        User target = (User) args.getOne("online/offline player").get();
        Player source = (Player) src;

        //get their storage options
        FriendsStorage targetStorage = FriendsUtil.getData(target.getUniqueId());
        FriendsStorage sourceStorage = FriendsUtil.getData(source.getUniqueId());

        sourceStorage.removeFriend(target.getUniqueId());
        targetStorage.removeFriend(source.getUniqueId());
        if (target.isOnline() && target.getPlayer().isPresent())
            PlayerUtils.tell(target.getPlayer().get(), Translations.FRIEND_REMOVED.f(source.getName()));
        if (source.isOnline() && source.getPlayer().isPresent())
            PlayerUtils.tell(source.getPlayer().get(), Translations.FRIEND_REMOVED.f(target.getName()));

        FriendsStorageManager.save(target.getUniqueId(), targetStorage);
        FriendsStorageManager.save(source.getUniqueId(), sourceStorage);
        return CommandResult.success();
    }

}
