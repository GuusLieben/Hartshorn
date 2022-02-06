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

package org.dockbox.hartshorn.data.mapping;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.annotations.UsePersistence;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;

@HartshornTest
@UsePersistence
public class PersistenceModifiersTests {

    @Inject
    @Getter
    private ApplicationContext applicationContext;

    @Test
    void testSkipEmptyKeepsNonEmpty() {
        final ObjectMapper mapper = this.applicationContext().get(ObjectMapper.class).skipBehavior(JsonInclusionRule.SKIP_EMPTY);
        final ModifierElement element = new ModifierElement(HartshornUtils.asList("sample", "other"));
        final Exceptional<String> out = mapper.write(element);

        Assertions.assertTrue(out.present());
        Assertions.assertEquals("{\"names\":[\"sample\",\"other\"]}", HartshornUtils.strip(out.get()));
    }

    @Test
    void testSkipEmptySkipsEmpty() {
        final ObjectMapper mapper = this.applicationContext().get(ObjectMapper.class).skipBehavior(JsonInclusionRule.SKIP_EMPTY);
        final ModifierElement element = new ModifierElement(List.of());
        final Exceptional<String> out = mapper.write(element);

        Assertions.assertTrue(out.present());
        Assertions.assertEquals("{}", HartshornUtils.strip(out.get()));
    }
}
