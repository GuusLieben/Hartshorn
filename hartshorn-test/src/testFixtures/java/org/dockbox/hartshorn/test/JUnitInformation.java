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

package org.dockbox.hartshorn.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JUnitInformation {

    private final Path filePath;
    private int onlinePlayerCount = 10;

    public JUnitInformation() {
        try {
            this.filePath = Files.createTempDirectory("hartshorn-");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path path() {
        return this.filePath;
    }

    public int playerCount() {
        return this.onlinePlayerCount;
    }

    public void playerCount(int onlinePlayerCount) {
        this.onlinePlayerCount = onlinePlayerCount;
    }
}
