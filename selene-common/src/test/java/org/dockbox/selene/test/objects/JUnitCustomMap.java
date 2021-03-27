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

package org.dockbox.selene.test.objects;

import org.dockbox.selene.api.objects.item.maps.CustomMap;
import org.dockbox.selene.api.objects.targets.Identifiable;
import org.dockbox.selene.api.server.Selene;

public class JUnitCustomMap extends JUnitItem implements CustomMap {

    private final Identifiable owner;

    public JUnitCustomMap(Identifiable owner, int id) {
        super(Selene.getItems().getFilledMap().getId(), id);
        this.owner = owner;
    }

    @Override
    public Identifiable getOwner() {
        return this.owner;
    }

    @Override
    public int getMapId() {
        return this.getMeta();
    }
}
