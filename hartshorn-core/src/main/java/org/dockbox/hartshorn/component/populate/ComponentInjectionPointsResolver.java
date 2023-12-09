/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.component.populate;

import java.util.Set;

import org.dockbox.hartshorn.util.introspect.view.TypeView;

/**
 * A resolver that determines the injection points of a component. This is commonly used by
 * {@link org.dockbox.hartshorn.component.ComponentPopulator}s to determine which injection points
 * should be populated.
 *
 * <p>Injection points typically don't require any filtering in this stage, as the {@link
 * org.dockbox.hartshorn.component.ComponentPopulator} is expected to filter injection points based
 * on available metadata.
 *
 * @see org.dockbox.hartshorn.component.ComponentPopulator
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public interface ComponentInjectionPointsResolver {

    /**
     * Resolves the injection points of the given type. This method is expected to return a set of
     * {@link ComponentInjectionPoint}s that represent the injection points of the given type.
     *
     * @param type the type to resolve the injection points for
     * @return the injection points of the given type
     * @param <T> the type of the component
     */
    <T> Set<ComponentInjectionPoint<T>> resolve(TypeView<T> type);
}
