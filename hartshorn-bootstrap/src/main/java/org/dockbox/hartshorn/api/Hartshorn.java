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

package org.dockbox.hartshorn.api;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** The global {@link Hartshorn} instance used to grant access to various components. */
public final class Hartshorn {

    /**
     * The global permission node for any {@link org.dockbox.hartshorn.api.domain.Subject}
     * to bypass application permissions.
     */
    public static final String GLOBAL_PERMITTED = "hartshorn.global.permitted";
    /**
     * The default package prefix to use when scanning Hartshorn internals.
     */
    public static final String PACKAGE_PREFIX = "org.dockbox.hartshorn";
    /**
     * The (human readable) display name of Hartshorn.
     */
    public static final String PROJECT_NAME = "Hartshorn";
    /**
     * The simplified identifier for Hartshorn-default identifiers.
     */
    public static final String PROJECT_ID = "hartshorn";
    /**
     * The semantic version of the current/latest release of Hartshorn
     */
    public static final String VERSION = "4.1.1";

    private static final Map<String, Logger> LOGGERS = HartshornUtils.emptyConcurrentMap();

    private Hartshorn() {}

    /**
     * Gets the current {@link ApplicationContext} associated with the active {@link #server()}.
     *
     * @return The active context
     */
    public static ApplicationContext context() {
        return server().context();
    }

    /**
     * Gets the instance of {@link Hartshorn}.
     *
     * @return The {@link Hartshorn} instance
     */
    public static HartshornBootstrap server() {
        return HartshornBootstrap.instance();
    }

    /**
     * Gets a log instance representing the calling type.
     *
     * @return The {@link Logger}
     */
    public static Logger log() {
        return internalLog();
    }

    static Logger internalLog() {
        final StackTraceElement element = Thread.currentThread().getStackTrace()[3];
        final String className = element.getClassName();
        if (LOGGERS.containsKey(className)) return LOGGERS.get(className);

        final String[] qualifiedClassName = className.split("\\.");
        final StringBuilder fullName = new StringBuilder();

        for (int i = 0; i < qualifiedClassName.length; i++) {
            final String part = qualifiedClassName[i];
            if (i > 0) fullName.append('.');
            if (i == qualifiedClassName.length - 1) fullName.append(part);
            else fullName.append(part.charAt(0));
        }

        final String name = HartshornUtils.wrap(fullName.toString(), 35);
        final Logger logger = LoggerFactory.getLogger(name);
        LOGGERS.put(className, logger);
        return logger;
    }

    /**
     * Attempts to look up a resource file. If the file exists it is wrapped in a {@link Exceptional}
     * and returned. If the file does not exist or is a directory, {@link Exceptional#empty()} is
     * returned. If the requested file name is invalid, or {@code null}, a {@link Exceptional}
     * containing the appropriate exception is returned.
     *
     * @param name
     *         The name of the file to look up
     *
     * @return The resource file wrapped in a {@link Exceptional}, or a appropriate {@link
     *         Exceptional} (either none or providing the appropriate exception).
     */
    public static Exceptional<Path> resource(final String name) {
        try {
            final InputStream in = Hartshorn.class.getClassLoader().getResourceAsStream(name);
            final byte[] buffer = new byte[in.available()];
            in.read(buffer);

            final Path tempFile = Files.createTempFile(name, ".tmp");
            final OutputStream outStream = new FileOutputStream(tempFile.toFile());
            outStream.write(buffer);

            return Exceptional.of(tempFile);
        }
        catch (final NullPointerException | IOException e) {
            return Exceptional.of(e);
        }
    }
}
