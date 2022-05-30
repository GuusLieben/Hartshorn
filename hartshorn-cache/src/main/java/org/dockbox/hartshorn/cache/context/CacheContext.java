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

package org.dockbox.hartshorn.cache.context;

import org.dockbox.hartshorn.cache.Cache;
import org.dockbox.hartshorn.cache.CacheManager;

/**
 * Context carrier for cache service modifiers, indicating the {@link CacheManager},
 * {@link Cache}, and cache ID to use. These are typically derived from
 * {@link CacheMethodContext} during processing.
 */
public interface CacheContext {

    CacheManager manager();

    <T> Cache<String, T> cache();

    String cacheName();

    String key();

}
