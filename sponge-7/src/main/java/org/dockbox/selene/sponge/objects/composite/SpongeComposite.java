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

package org.dockbox.selene.sponge.objects.composite;

import org.dockbox.selene.api.i18n.entry.IntegratedResource;
import org.dockbox.selene.api.objects.Exceptional;
import org.dockbox.selene.api.objects.keys.PersistentDataHolder;
import org.dockbox.selene.api.objects.keys.PersistentDataKey;
import org.dockbox.selene.api.objects.keys.StoredPersistentKey;
import org.dockbox.selene.api.objects.keys.TransactionResult;
import org.dockbox.selene.api.util.Reflect;
import org.dockbox.selene.api.util.SeleneUtils;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface SpongeComposite extends PersistentDataHolder {

    @Override
    default <T> Exceptional<T> get(PersistentDataKey<T> dataKey) {
        Exceptional<MutableCompositeData> result = this.getDataHolder()
                .map(composite -> composite.get(MutableCompositeData.class).orElse(null));

        if (result.isAbsent()) return Exceptional.empty();

        MutableCompositeData data = result.get();
        if (!data.getData().containsKey(dataKey.getDataKeyId())) return Exceptional.empty();

        Object value = data.getData().get(dataKey.getDataKeyId());
        if (Reflect.isAssignableFrom(dataKey.getDataType(), value.getClass()))
            // If a CCE is thrown, it'll be captured by the Exceptional because of the Callable
            //noinspection unchecked
            return Exceptional.of(() -> (T) value);

        return Exceptional.empty();
    }

    @Override
    default <T> TransactionResult set(PersistentDataKey<T> dataKey, T value) {
        return this.getDataHolder().map(composite -> {
            Map<String, Object> data = composite
                    .get(MutableCompositeData.class)
                    .orElse(new MutableCompositeData())
                    .getData();
            data.put(dataKey.getDataKeyId(), value);

            MutableCompositeData compositeData = new MutableCompositeData();
            compositeData.fillData(data);
            DataTransactionResult result = composite.offer(compositeData);
            if (result.isSuccessful()) return TransactionResult.success();
            else return TransactionResult.fail(IntegratedResource.KEY_BINDING_FAILED);
        })
                .orElseGet(() -> TransactionResult.fail(IntegratedResource.LOST_REFERENCE));
    }

    @Override
    default <T> void remove(PersistentDataKey<T> dataKey) {
        this.getDataHolder().ifPresent(composite -> {
            Optional<MutableCompositeData> result = composite.get(MutableCompositeData.class);
            if (!result.isPresent()) return; // No data to remove

            MutableCompositeData data = result.get();
            if (!data.getData().containsKey(dataKey.getDataKeyId())) return; // Already removed

            data.getData().remove(dataKey.getDataKeyId());

            composite.offer(data);
        });
    }

    @Override
    default Map<PersistentDataKey<?>, Object> getPersistentData() {
        Exceptional<? extends DataHolder> dataHolderExceptional = this.getDataHolder();
        if (dataHolderExceptional.isAbsent()) return SeleneUtils.emptyMap();

        Map<PersistentDataKey<?>, Object> persistentData = SeleneUtils.emptyMap();
        DataHolder dataHolder = dataHolderExceptional.get();
        for (ImmutableValue<?> value : dataHolder.getValues()) {
            PersistentDataKey<?> dataKey = StoredPersistentKey.of(value.getKey().getName());
            Object dataValue = value.get();
            persistentData.put(dataKey, dataValue);
        }
        return persistentData;
    }

    Exceptional<? extends DataHolder> getDataHolder();
}
