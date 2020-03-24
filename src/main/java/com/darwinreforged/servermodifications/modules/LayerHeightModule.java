package com.darwinreforged.servermodifications.modules;

import com.darwinreforged.servermodifications.DarwinServer;
import com.darwinreforged.servermodifications.commands.LayerheightCommand;
import com.darwinreforged.servermodifications.listeners.LayerheightPlaceEventListener;
import com.darwinreforged.servermodifications.modules.root.ModuleInfo;
import com.darwinreforged.servermodifications.modules.root.PluginModule;
import com.darwinreforged.servermodifications.resources.Permissions;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.text.Text;

@ModuleInfo(id = "darwinlayerheight", name = "Darwin Layer Height", version = "1.0", description = "Easy to use layer heights")
public class LayerHeightModule extends PluginModule {
	public LayerHeightModule() {
	}

	@Override
	public void onServerStart(GameStartedServerEvent event) {
		DarwinServer.registerCommand(layerHeightMain, "layerheight");
		DarwinServer.registerListener(new LayerheightPlaceEventListener());
	}

	//pls update
    CommandSpec layerHeightMain = CommandSpec.builder()
    	    .permission(Permissions.LAYERHEIGHT_USE.p())
    	    .arguments(GenericArguments.integer(Text.of("1 to 8")))
    	    .executor(new LayerheightCommand())
    	    .build();
}
