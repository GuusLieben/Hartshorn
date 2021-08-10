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

package org.dockbox.hartshorn.persistence.service;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.persistence.PersistentElement;
import org.dockbox.hartshorn.persistence.annotations.Deserialise;
import org.dockbox.hartshorn.persistence.annotations.Serialise;

@Service(owner = Hartshorn.class)
public interface AnnotationPathPersistenceService {

    @Serialise
    boolean writeToPath(PersistentElement element);

    @Deserialise
    PersistentElement readFromPath();

}
