package org.dockbox.selene.core.impl.objects.registry;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Registry<V extends Serializable> extends HashMap<RegistryIdentifier<?>, RegistryColumn<V>> {

    /**
     * Adds a column of data to the Registry. <B>Note</B> this will override an existing column
     * if they share the same {@link RegistryIdentifier}
     *
     * @param columnID The {@link RegistryIdentifier} for which to add this data added under.
     * @param values A safe varargs of type {@link V} to be added.
     * @return Itself.
     */
    @SafeVarargs
    public final Registry<V> addColumn(RegistryIdentifier<?> columnID, V... values) {
        return this.addColumn(columnID, Arrays.asList(values));
    }

    /**
     * Adds a column of data to the Registry. <B>Note</B> this will override an existing column
     * if they share the same {@link RegistryIdentifier}
     *
     * @param columnID The {@link RegistryIdentifier} for which to add this data added under.
     * @param values A collection of type {@link V} or its children to be added.
     * @return Itself.
     */
    public Registry<V> addColumn(RegistryIdentifier<?> columnID, Collection<? extends V> values) {
        super.put(columnID, new RegistryColumn<>(values));
        return this;
    }
    /**
     * Adds data to the Registry. If the columnID does not exist, it creates a new column, otherwise it
     * adds the data to the existing column.
     *
     * @param columnID The {@link RegistryIdentifier} for which this data will be added to.
     * @param values A safe varargs of type {@link V} to be added.
     * @return Itself.
     */
    @SafeVarargs
    public final Registry<V> addData(RegistryIdentifier<?> columnID, V... values) {
        return this.addData(columnID, Arrays.asList(values));
    }

    /**
     * Adds data to the Registry. If the columnID does not exist, it creates a new column, otherwise it
     * adds the data to the existing column.
     *
     * @param columnID The {@link RegistryIdentifier} for which this data will be added to.
     * @param values A collection of type {@link V} or its children to be added.
     * @return Itself.
     */
    public Registry<V> addData(RegistryIdentifier<?> columnID, Collection<? extends V> values) {
        if (super.containsKey(columnID)) {
            super.get(columnID).addAll(values);
        }
        else {
            this.addColumn(columnID, values);
        }
        return this;
    }

    /**
     * Adds another Registry to this one. If the added Registry contains the same {@link RegistryIdentifier}s, then that
     * data will be added to the existing columns.
     *
     * @param otherRegistry The other Registry to add to this one.
     * @return Itself.
     */
    public Registry<V> addRegistry(@NotNull Map<RegistryIdentifier<?>, RegistryColumn<V>> otherRegistry) {
        otherRegistry.forEach(this::addData);
        return this;
    }

    /**
     * @param columnIDs A varargs of {@link RegistryIdentifier}s to remove from the Registry if contained.
     * @return Itself.
     */
    public Registry<V> removeColumns(@NotNull RegistryIdentifier<?>... columnIDs) {
        for (RegistryIdentifier<?> columnID : columnIDs) {
            super.remove(columnID);
        }
        return this;
    }

    /**
     * @param columnIDs A varargs of {@link RegistryIdentifier}s to check if contained in the Registry.
     *
     * @return True if all of the {@link RegistryIdentifier}s are contained, otherwise false.
     */
    public boolean containsColumns(RegistryIdentifier<?>... columnIDs) {
        for (RegistryIdentifier<?> columnID : columnIDs) {
            if (!super.containsKey(columnID)) return false;
        }
        return true;
    }

    /**
     * Gets all the matching columns in the Registry if contained.
     *
     * @param columnIDs A varargs of {@link RegistryIdentifier}s to return from the Registry if contained.
     * @return
     * All the matching columns data combined into a single {@link RegistryColumn}. If no matches are found, an empty
     * {@link RegistryColumn} will be returned.
     */
    public RegistryColumn<V> getMatchingColumns(RegistryIdentifier<?>... columnIDs) {
        RegistryColumn<V> result = new RegistryColumn<>();
        for (RegistryIdentifier<?> columnID : columnIDs) {
            if (super.containsKey(columnID)) {
                result.addAll(super.get(columnID));
            }
        }
        return result;
    }

    /**
     * @return All the data in the Registry combined into a single {@link RegistryColumn<V>}
     */
    public RegistryColumn<V> getAllData() {
        RegistryColumn<V> result = new RegistryColumn<>();
        for (RegistryColumn<V> columnData : super.values()) {
            result.addAll(columnData);
        }
        return result;
    }

    /**
     * Filter the Registry by its columns. Note this creates a new Registry and doesn't modify itself.
     *
     * @param filter
     * The filter accepts a {@link RegistryIdentifier} and returns false to keep that column, true to remove it.
     * The columns which pass the filter are stored in a <b>new</b> Registry.
     * @return The new Registry containing the filtered columns.
     */
    public Registry<V> removeColumnsIf(Predicate<RegistryIdentifier<?>> filter) {
        Registry<V> registry = new Registry<>();

        for (RegistryIdentifier<?> columnID : super.keySet()) {
            if (!filter.test(columnID)) {
                registry.addColumn(columnID, super.get(columnID));
            }
        }
        return registry;
    }

    /**
     * Filter the Registry by its values. Note this creates a new Registry and doesn't modify itself.
     *
     * @param filter
     * The filter accepts a value of type {@link V} or its parents and returns false to keep that value, true to remove it.
     * The values which pass the filter are stored in a <b>new</b> Registry. If no values in a particular column pass the
     * filter, it is still added to the new Registry, it will simply contain no values.
     * @return The new Registry containing the filtered values.
     */
    public Registry<V> removeValuesIf(Predicate<? super V> filter) {
        Registry<V> registry = new Registry<>();

        for (RegistryIdentifier<?> columnID : super.keySet()) {
            RegistryColumn<V> column = new RegistryColumn<>(super.get(columnID));
            column.removeValueIf(filter);
            registry.addColumn(columnID, column);
        }
        return registry;
    }
}
