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

package org.dockbox.hartshorn.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.dockbox.hartshorn.test.TestCustomizer;

/**
 * Marks a method within a test class as capable of customizing the test environment, typically through
 * the {@link TestCustomizer} utility class.
 *
 * <p>For example, a test class may have a method annotated with {@code @CustomizeTests} that sets up a
 * specific exception handler for the test environment.
 *
 * @since 0.4.11
 *
 * @author Guus Lieben
 *
 * @deprecated Use configuration classes instead
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
@Deprecated(since = "0.7.0", forRemoval = true)
public @interface CustomizeTests {
}
