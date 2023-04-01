package org.dockbox.hartshorn.util.introspect.convert.support;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.util.introspect.convert.ConvertibleTypePair;
import org.dockbox.hartshorn.util.introspect.convert.GenericConverter;

import java.lang.reflect.Array;
import java.util.Set;

public class ObjectToArrayConverter implements GenericConverter {

    @Override
    public Set<ConvertibleTypePair> convertibleTypes() {
        return Set.of(ConvertibleTypePair.of(Object.class, Object[].class));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <I, O> Object convert(@Nullable final Object source, @NonNull final Class<I> sourceType, @NonNull final Class<O> targetType) {
        final I[] array = (I[]) Array.newInstance(sourceType, 1);
        array[0] = (I) source;
        return array;
    }
}
