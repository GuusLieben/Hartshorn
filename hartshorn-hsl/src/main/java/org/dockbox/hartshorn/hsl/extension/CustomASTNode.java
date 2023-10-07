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

package org.dockbox.hartshorn.hsl.extension;

import org.dockbox.hartshorn.hsl.ast.ASTNode;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.semantic.Resolver;

public sealed interface CustomASTNode<T extends ASTNode & CustomASTNode<T, R>, R> permits CustomExpression, CustomStatement {

    ASTExtensionModule<T, R> module();

    default R interpret(Interpreter interpreter) {
        return this.module().interpreter().interpret((T) this, interpreter);
    }

    default void resolve(Resolver resolver) {
        this.module().resolver().resolve((T) this, resolver);
    }
}
