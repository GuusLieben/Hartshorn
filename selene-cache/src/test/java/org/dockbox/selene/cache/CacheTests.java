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

package org.dockbox.selene.cache;

import org.dockbox.selene.api.Selene;
import org.dockbox.selene.api.domain.Exceptional;
import org.dockbox.selene.test.SeleneJUnit5Runner;
import org.dockbox.selene.util.SeleneUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

@ExtendWith(SeleneJUnit5Runner.class)
public class CacheTests {

    @Test
    void testCacheIsReused() {
        TestCacheService service = Selene.context().get(TestCacheService.class);
        List<String> cached = service.getCachedThings();
        Assertions.assertNotNull(cached);
        Assertions.assertFalse(cached.isEmpty());

        final String first = cached.get(0);
        Assertions.assertNotNull(first);

        cached = service.getCachedThings();
        Assertions.assertNotNull(cached);
        Assertions.assertFalse(cached.isEmpty());

        final String second = cached.get(0);
        Assertions.assertNotNull(second);

        Assertions.assertEquals(first, second);
    }

    @Test
    void testCacheCanBeUpdated() {
        TestCacheService service = Selene.context().get(TestCacheService.class);
        List<String> cached = service.getCachedThings();
        Assertions.assertEquals(1, cached.size());
        String first = cached.get(0);

        service.updateCache("second value");
        cached = service.getCachedThings();
        Assertions.assertEquals(2, cached.size());

        String newFirst = cached.get(0);
        Assertions.assertEquals(first, newFirst);

        String second = cached.get(1);
        Assertions.assertEquals("second value", second);
    }

    @Test
    void testCacheCanBeEvicted() {
        TestCacheService service = Selene.context().get(TestCacheService.class);
        List<String> cached = service.getCachedThings();
        String first = cached.get(0);

        service.evict();
        cached = service.getCachedThings();
        String second = cached.get(0);
        Assertions.assertNotEquals(first, second);
    }

    @Test
    void testCacheCanBeUpdatedThroughManager() {
        TestCacheService service = Selene.context().get(TestCacheService.class);
        List<String> cached = service.getCachedThings();
        Assertions.assertEquals(1, cached.size());
        String first = cached.get(0);

        final CacheManager cacheManager = Selene.context().get(CacheManager.class);
        cacheManager.update("sample", "second value");

        final Cache<Object> cache = cacheManager.get("sample").get();
        final Exceptional<Collection<Object>> content = cache.get();
        Assertions.assertTrue(content.present());

        final List<Object> objects = (List<Object>) content.get();
        Assertions.assertEquals(2, objects.size());

        String newFirst = (String) objects.get(0);
        Assertions.assertEquals(first, newFirst);

        String second = (String) objects.get(1);
        Assertions.assertEquals("second value", second);
    }

    @Test
    void testCacheCanBeEvictedThroughManager() {
        // Initial population through source service
        TestCacheService service = Selene.context().get(TestCacheService.class);
        List<String> cached = service.getCachedThings();
        Assertions.assertNotNull(cached);
        Assertions.assertFalse(cached.isEmpty());

        final CacheManager cacheManager = Selene.context().get(CacheManager.class);
        cacheManager.evict("sample");

        final Cache<Object> cache = cacheManager.get("sample").get();
        final Exceptional<Collection<Object>> content = cache.get();
        Assertions.assertTrue(content.absent());
    }

    @AfterEach
    void reset() throws IllegalAccessException, NoSuchFieldException {
        final Field caches = SimpleCacheManager.class.getDeclaredField("caches");
        caches.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(caches, caches.getModifiers() & ~Modifier.FINAL);
        caches.set(null, SeleneUtils.emptyConcurrentMap());
    }
}
