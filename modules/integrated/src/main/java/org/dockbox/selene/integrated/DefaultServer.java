/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.integrated;

import com.google.inject.Singleton;

import org.dockbox.selene.api.annotations.command.Command;
import org.dockbox.selene.api.annotations.module.Module;
import org.dockbox.selene.api.command.CommandBus;
import org.dockbox.selene.api.command.context.CommandArgument;
import org.dockbox.selene.api.command.context.CommandContext;
import org.dockbox.selene.api.events.EventBus;
import org.dockbox.selene.api.events.server.ServerReloadEvent;
import org.dockbox.selene.api.i18n.common.Language;
import org.dockbox.selene.api.i18n.entry.DefaultResource;
import org.dockbox.selene.api.module.ModuleContainer;
import org.dockbox.selene.api.module.ModuleManager;
import org.dockbox.selene.api.objects.Exceptional;
import org.dockbox.selene.api.objects.player.Player;
import org.dockbox.selene.api.objects.targets.AbstractIdentifiable;
import org.dockbox.selene.api.objects.targets.MessageReceiver;
import org.dockbox.selene.api.server.Selene;
import org.dockbox.selene.api.server.SeleneInformation;
import org.dockbox.selene.api.server.Server;
import org.dockbox.selene.api.server.ServerType;
import org.dockbox.selene.api.server.bootstrap.SeleneBootstrap;
import org.dockbox.selene.api.text.Text;
import org.dockbox.selene.api.text.actions.ClickAction;
import org.dockbox.selene.api.text.actions.HoverAction;
import org.dockbox.selene.api.text.pagination.PaginationBuilder;
import org.dockbox.selene.api.util.Reflect;
import org.dockbox.selene.api.util.SeleneUtils;

import java.util.List;

@Module(
        id = SeleneInformation.PROJECT_ID,
        name = SeleneInformation.PROJECT_NAME,
        description = "Integrated features of Selene",
        authors = "GuusLieben"
)
@Command(aliases = SeleneInformation.PROJECT_ID, usage = SeleneInformation.PROJECT_ID)
@Singleton
public class DefaultServer implements Server {

    // Parent command
    @Command(aliases = "", usage = "")
    public static void debugModules(MessageReceiver source) {
        Reflect.runWithInstance(ModuleManager.class, em -> {
            PaginationBuilder pb = Selene.provide(PaginationBuilder.class);

            List<Text> content = SeleneUtils.emptyList();
            content.add(DefaultServerResources.SERVER_HEADER
                    .format(Selene.getServer().getVersion())
                    .translate(source).asText()
            );
            content.add(DefaultServerResources.SERVER_UPDATE
                    .format(Selene.getServer().getLastUpdate())
                    .translate(source).asText()
            );
            content.add(DefaultServerResources.SERVER_AUTHORS
                    .format(String.join(",", SeleneBootstrap.getAuthors()))
                    .translate(source).asText());
            content.add(DefaultServerResources.SERVER_MODULES.translate(source).asText());

            em.getRegisteredModuleIds().forEach(id -> em.getContainer(id)
                    .map(e -> DefaultServer.generateText(e, source))
                    .present(content::add)
            );

            pb.title(DefaultServerResources.PAGINATION_TITLE.translate(source).asText());
            pb.content(content);

            source.sendPagination(pb.build());
        });
    }

    private static Text generateText(ModuleContainer e, MessageReceiver source) {
        Text line = DefaultServerResources.MODULE_ROW
                .format(e.name(), e.id())
                .translate(source)
                .asText();
        line.onClick(ClickAction.runCommand("/" + SeleneInformation.PROJECT_ID + " module " + e.id()));
        line.onHover(HoverAction.showText(DefaultServerResources.MODULE_ROW_HOVER
                .format(e.name())
                .translate(source)
                .asText()
        ));
        return line;
    }

    @Command(aliases = "module", usage = "module <id{Module}>")
    public static void debugModule(MessageReceiver src, CommandContext ctx) {
        ModuleContainer container = ctx.get("id");

        src.send(DefaultServerResources.MODULE_INFO_BLOCK.format(
                container.name(), container.id(), container.description(),
                0 == container.dependencies().length ? "None" : String.join("$3, $1", container.dependencies()),
                String.join("$3, $1", container.authors()), container.source()
        ));
    }

    @Command(aliases = "reload", usage = "reload [id{Module}]", confirm = true)
    public static void reload(MessageReceiver src, CommandContext ctx) {
        EventBus eb = Selene.provide(EventBus.class);
        if (ctx.has("id")) {
            ModuleContainer container = ctx.get("id");
            Exceptional<?> oi = Selene.provide(ModuleManager.class).getInstance(container.id());

            oi.present(o -> {
                eb.post(new ServerReloadEvent(), o.getClass());
                src.send(DefaultServerResources.MODULE_RELOAD_SUCCESSFUL.format(container.name()));
            }).absent(() ->
                    src.send(DefaultServerResources.NODULE_RELOAD_FAILED.format(container.name())));
        }
        else {
            eb.post(new ServerReloadEvent());
            src.send(DefaultServerResources.FULL_RELOAD_SUCCESSFUL);
        }
    }

    @Command(aliases = { "lang", "language" }, usage = "language <language{Language}> [player{Player}]", inherit = false)
    public static void switchLang(MessageReceiver src, CommandContext ctx, Language language, Player player) {
        if (null == player) {
            if (src instanceof Player) {
                player = (Player) src;
            }
            else {
                src.send(DefaultResource.CONFIRM_WRONG_SOURCE);
                return;
            }
        }

        player.setLanguage(language);

        String languageLocalized = language.getNameLocalized() + " (" + language.getNameEnglish() + ")";
        if (player != src)
            src.sendWithPrefix(DefaultServerResources.LANG_SWITCHED_OTHER.format(player.getName(), languageLocalized));
        player.sendWithPrefix(DefaultServerResources.LANG_SWITCHED.format(languageLocalized));
    }

    @Command(aliases = "platform", usage = "platform")
    public static void platform(MessageReceiver src) {
        ServerType st = Selene.getServer().getServerType();
        String platformVersion = Selene.getServer().getPlatformVersion();

        String mcVersion = Selene.getServer().getMinecraftVersion().getReadableVersionString();

        Object[] system = SeleneUtils.getAll(System::getProperty,
                "java.version", "java.vendor", "java.vm.version", "java.vm.name", "java.vm.vendor", "java.runtime.version", "java.class.version");

        src.send(DefaultServerResources.PLATFORM_INFORMATION.format(
                st.getDisplayName(), platformVersion, mcVersion, system[0], system[1], system[2], system[2], system[3], system[4], system[5])
        );
    }

    @Override
    @Command(aliases = "confirm", usage = "confirm <cooldownId{String}>")
    public void confirm(MessageReceiver src, CommandContext ctx) {
        if (!(src instanceof AbstractIdentifiable)) {
            src.send(DefaultServerResources.CONFIRM_WRONG_SOURCE);
            return;
        }
        Exceptional<CommandArgument<String>> optionalCooldownId = ctx.argument("cooldownId");

        // UUID is stored by the command executor to ensure runnables are not called by other sources. The uuid
        // argument here is just a confirmation that the source is correct.
        optionalCooldownId
                .present(cooldownId -> {
                    String cid = cooldownId.getValue();
                    Selene.provide(CommandBus.class).confirmCommand(cid).absent(() ->
                            src.send(DefaultServerResources.CONFIRM_FAILED));
                })
                .absent(() -> src.send(DefaultServerResources.CONFIRM_INVALID_ID));
    }

}
