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

package org.dockbox.hartshorn.hsl;

import org.dockbox.hartshorn.inject.annotations.SupportPriority;
import org.dockbox.hartshorn.inject.annotations.configuration.Configuration;
import org.dockbox.hartshorn.inject.annotations.configuration.Prototype;
import org.dockbox.hartshorn.inject.annotations.configuration.Singleton;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.hsl.customizer.DefaultScriptStatementsParserCustomizer;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.parser.StandardTokenParser;
import org.dockbox.hartshorn.hsl.parser.TokenParser;
import org.dockbox.hartshorn.hsl.parser.expression.ComplexExpressionParserAdapter;
import org.dockbox.hartshorn.hsl.parser.expression.ExpressionParser;
import org.dockbox.hartshorn.hsl.runtime.ScriptRuntime;
import org.dockbox.hartshorn.hsl.runtime.StandardRuntime;
import org.dockbox.hartshorn.hsl.runtime.ValidateExpressionRuntime;
import org.dockbox.hartshorn.hsl.semantic.Resolver;
import org.dockbox.hartshorn.hsl.token.DefaultTokenRegistry;
import org.dockbox.hartshorn.launchpad.condition.RequiresActivator;

/**
 * TODO: #1061 Add documentation
 *
 * @since 0.4.12
 *
 * @author Guus Lieben
 */
@Configuration
@RequiresActivator(UseExpressionValidation.class)
public class ScriptLanguageConfiguration {

    @Singleton
    @SupportPriority
    private ScriptComponentFactory languageFactory() {
        return new StandardScriptComponentFactory();
    }

    @Prototype
    @SupportPriority
    private TokenParser tokenParser() {
        return new StandardTokenParser(DefaultTokenRegistry.createDefault());
    }

    @Prototype
    @SupportPriority
    private ExpressionParser expressionParser() {
        return new ComplexExpressionParserAdapter(() -> null);
    }

    @Prototype
    @SupportPriority
    private Resolver resolver(Interpreter interpreter) {
        return new Resolver(interpreter);
    }

    @Prototype
    @SupportPriority
    public ScriptRuntime runtime(
            ApplicationContext applicationContext,
            ScriptComponentFactory factory,
            ParserCustomizer parserCustomizer
    ) {
        return new StandardRuntime(applicationContext, factory, parserCustomizer);
    }

    @Prototype
    @SupportPriority
    public ValidateExpressionRuntime expressionRuntime(
            ApplicationContext applicationContext,
            ScriptComponentFactory factory,
            ParserCustomizer parserCustomizer
    ) {
        return new ValidateExpressionRuntime(applicationContext, factory, parserCustomizer);
    }

    @Singleton
    @SupportPriority
    public ParserCustomizer parserCustomizer() {
        return new DefaultScriptStatementsParserCustomizer();
    }
}
