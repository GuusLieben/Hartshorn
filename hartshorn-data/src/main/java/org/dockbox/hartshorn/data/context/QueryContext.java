/*
 * Copyright 2019-2022 the original author or authors.
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

package org.dockbox.hartshorn.data.context;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.parameter.ParameterLoader;
import org.dockbox.hartshorn.data.annotations.Query;
import org.dockbox.hartshorn.data.jpa.JpaRepository;

import javax.persistence.EntityManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryContext {

    private ParameterLoader<JpaParameterLoaderContext> parameterLoader;

    private final Query annotation;
    private final Object[] args;
    private final MethodContext<?, ?> method;
    private final TypeContext<?> entityType;

    @Getter private final ApplicationContext applicationContext;
    @Getter private final JpaRepository<?, ?> repository;
    @Getter private final boolean modifiesEntity;

    public boolean automaticClear() {
        return this.annotation.automaticClear();
    }

    public boolean automaticFlush() {
        return this.annotation.automaticFlush();
    }

    public javax.persistence.Query query(final EntityManager entityManager) {
        final javax.persistence.Query persistenceQuery = this.persistenceQuery(entityManager, this.annotation);
        final JpaParameterLoaderContext loaderContext = new JpaParameterLoaderContext(this.method, this.entityType, null, this.applicationContext, persistenceQuery);
        this.parameterLoader().loadArguments(loaderContext, this.args);
        return persistenceQuery;
    }

    protected javax.persistence.Query persistenceQuery(final EntityManager entityManager, final Query query) throws IllegalArgumentException {
        return switch (query.type()) {
            case JPQL -> {
                if (this.modifiesEntity || this.entityType.isVoid()) yield entityManager.createQuery(query.value());
                else yield entityManager.createQuery(query.value(), this.entityType.type());
            }
            case NATIVE -> {
                if (this.modifiesEntity || this.entityType.isVoid()) yield entityManager.createNativeQuery(query.value());
                else yield entityManager.createNativeQuery(query.value(), this.entityType.type());
            }
            default -> throw new IllegalStateException("Unexpected value: " + query.type());
        };
    }

    protected ParameterLoader<JpaParameterLoaderContext> parameterLoader() {
        if (this.parameterLoader == null) {
            this.parameterLoader = this.applicationContext().get(Key.of(ParameterLoader.class, "jpa_query"));
        }
        return this.parameterLoader;
    }
}
