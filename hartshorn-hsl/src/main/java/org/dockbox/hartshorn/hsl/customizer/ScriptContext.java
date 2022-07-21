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

package org.dockbox.hartshorn.hsl.customizer;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.context.DefaultApplicationAwareContext;
import org.dockbox.hartshorn.hsl.ast.statement.Statement;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.interpreter.ResultCollector;
import org.dockbox.hartshorn.hsl.lexer.Comment;
import org.dockbox.hartshorn.hsl.lexer.Lexer;
import org.dockbox.hartshorn.hsl.parser.Parser;
import org.dockbox.hartshorn.hsl.semantic.Resolver;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.util.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The standard context which tracks the state of the execution of a script. It is used by the
 * {@link org.dockbox.hartshorn.hsl.runtime.StandardRuntime} to track the state of the script's
 * execution, and store its various executors. Everything but the original script can be
 * customized during the script's execution, though some runtimes may not support this.
 *
 * @author Guus Lieben
 * @since 22.4
 */
public class ScriptContext extends DefaultApplicationAwareContext implements ResultCollector {

    private static final String GLOBAL_RESULT = "$__result__$";
    protected final Map<String, Object> results = new ConcurrentHashMap<>();

    private final String source;

    private List<Token> tokens;
    private List<Statement> statements;
    private List<Comment> comments;

    private Lexer lexer;
    private Parser parser;
    private Resolver resolver;
    private Interpreter interpreter;

    public ScriptContext(final ApplicationContext context, final String source) {
        super(context);
        this.source = source;
    }

    public String source() {
        return this.source;
    }

    public List<Token> tokens() {
        return this.tokens;
    }

    public ScriptContext tokens(final List<Token> tokens) {
        this.tokens = tokens;
        return this;
    }

    public List<Statement> statements() {
        return this.statements;
    }

    public ScriptContext statements(final List<Statement> statements) {
        this.statements = statements;
        return this;
    }

    public List<Comment> comments() {
        return this.comments;
    }

    public ScriptContext comments(final List<Comment> comments) {
        this.comments = comments;
        return this;
    }

    public Lexer lexer() {
        return this.lexer;
    }

    public ScriptContext lexer(final Lexer lexer) {
        this.lexer = lexer;
        return this;
    }

    public Parser parser() {
        return this.parser;
    }

    public ScriptContext parser(final Parser parser) {
        this.parser = parser;
        return this;
    }

    public Resolver resolver() {
        return this.resolver;
    }

    public ScriptContext resolver(final Resolver resolver) {
        this.resolver = resolver;
        return this;
    }

    public Interpreter interpreter() {
        return this.interpreter;
    }

    public ScriptContext interpreter(final Interpreter interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    @Override
    public void addResult(final Object value) {
        this.addResult(GLOBAL_RESULT, value);
    }

    @Override
    public void addResult(final String id, final Object value) {
        this.results.put(id, value);
    }

    @Override
    public <T> Result<T> result() {
        return this.result(GLOBAL_RESULT);
    }

    @Override
    public <T> Result<T> result(final String id) {
        return Result.of(this.results.get(id))
                .map(result -> (T) result);
    }

    public void clear() {
        this.results.clear();
    }
}
