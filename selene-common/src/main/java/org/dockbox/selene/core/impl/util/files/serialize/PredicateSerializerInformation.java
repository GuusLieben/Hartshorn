/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.core.impl.util.files.serialize;

import com.google.common.reflect.TypeToken;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

class PredicateSerializerInformation<T> extends SerializerInformation<T> {

    PredicateSerializerInformation(Class<T> type, Supplier<TypeSerializer<?>> serializer) {
        super(type, serializer);
    }

    @Override
    public BiConsumer<TypeToken<T>, TypeSerializer<T>> getConsumer() {
        return (token, serializer) -> TypeSerializers.getDefaultSerializers().registerPredicate(typeToken ->
                        token.getRawType().isAssignableFrom(typeToken.getRawType()),
                serializer
        );
    }
}
