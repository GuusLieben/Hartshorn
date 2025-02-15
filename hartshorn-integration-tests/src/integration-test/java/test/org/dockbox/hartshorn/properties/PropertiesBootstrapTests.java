/*
 * Copyright 2019-2024 the original author or authors.
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

package test.org.dockbox.hartshorn.properties;

import org.dockbox.hartshorn.inject.annotations.Inject;
import org.dockbox.hartshorn.inject.annotations.PropertyValue;
import org.dockbox.hartshorn.launchpad.properties.PropertiesSource;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.properties.value.StandardValuePropertyParsers;
import org.dockbox.hartshorn.test.junit.HartshornIntegrationTest;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PropertiesSource("classpath:it-additional-config.yml")
@HartshornIntegrationTest
public class PropertiesBootstrapTests {

    @Test
    void testConfigurationValueWasLoaded_AccessedByRegistry(@Inject PropertyRegistry propertyRegistry) {
        Option<Boolean> isAdditionalConfigPresent = propertyRegistry.value(
                "hartshorn.test.additional-config",
                StandardValuePropertyParsers.BOOLEAN
        );
        Assertions.assertTrue(isAdditionalConfigPresent.orElse(false));
    }

    @Test
    void testConfigurationValueWasLoaded_AccessedByInjector(@PropertyValue(name = "hartshorn.test.additional-config") boolean additionalConfig) {
        Assertions.assertTrue(additionalConfig);
    }

    @Test
    void testConfigurationPropertyWasLoaded_AccessedByInjector(@PropertyValue(name = "hartshorn.test.additional-config") ValueProperty property) {
        Assertions.assertNotNull(property);

        Option<String> value = property.value();
        Assertions.assertTrue(value.present());
        Assertions.assertEquals("true", value.get());
    }
}
