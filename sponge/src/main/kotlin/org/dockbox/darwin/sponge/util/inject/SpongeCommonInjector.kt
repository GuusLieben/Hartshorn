package org.dockbox.darwin.sponge.util.inject

import net.byteflux.libby.LibraryManager
import net.byteflux.libby.SpongeLibraryManager
import org.dockbox.darwin.core.i18n.I18nService
import org.dockbox.darwin.core.i18n.SimpleI18NService
import org.dockbox.darwin.core.util.discord.DiscordUtils
import org.dockbox.darwin.core.util.events.EventBus
import org.dockbox.darwin.core.util.events.SimpleEventBus
import org.dockbox.darwin.core.util.exceptions.ExceptionHelper
import org.dockbox.darwin.core.util.exceptions.SimpleExceptionHelper
import org.dockbox.darwin.core.util.files.*
import org.dockbox.darwin.core.util.inject.AbstractCommonInjector
import org.dockbox.darwin.core.util.module.ModuleLoader
import org.dockbox.darwin.core.util.module.ModuleScanner
import org.dockbox.darwin.core.util.module.SimpleModuleLoader
import org.dockbox.darwin.core.util.module.SimpleModuleScanner
import org.dockbox.darwin.core.util.player.PlayerStorageService
import org.dockbox.darwin.core.util.text.BroadcastService
import org.dockbox.darwin.core.util.text.SimpleBroadcastService
import org.dockbox.darwin.sponge.util.discord.SpongeDiscordUtils
import org.dockbox.darwin.sponge.util.files.SpongeFileUtils
import org.dockbox.darwin.sponge.util.player.SpongePlayerStorageService

class SpongeCommonInjector : AbstractCommonInjector() {
    override fun configureExceptionInject() {
        bind(ExceptionHelper::class.java).to(SimpleExceptionHelper::class.java)
    }

    override fun configureModuleInject() {
        bind(ModuleLoader::class.java).to(SimpleModuleLoader::class.java)
        bind(ModuleScanner::class.java).to(SimpleModuleScanner::class.java)
    }

    override fun configureUtilInject() {
        bind(FileUtils::class.java).to(SpongeFileUtils::class.java)
        bind(ConfigManager::class.java).to(YamlConfigManager::class.java)
        bind(DataManager::class.java).to(YamlSQLiteDataManager::class.java)
        bind(EventBus::class.java).to(SimpleEventBus::class.java)
        bind(DiscordUtils::class.java).to(SpongeDiscordUtils::class.java)
        bind(LibraryManager::class.java).to(SpongeLibraryManager::class.java)
        bind(BroadcastService::class.java).to(SimpleBroadcastService::class.java)
        bind(PlayerStorageService::class.java).to(SpongePlayerStorageService::class.java)
        bind(I18nService::class.java).to(SimpleI18NService::class.java)
    }
}
