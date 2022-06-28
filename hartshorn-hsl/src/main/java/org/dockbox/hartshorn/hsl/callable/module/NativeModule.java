package org.dockbox.hartshorn.hsl.callable.module;

import org.dockbox.hartshorn.hsl.ast.statement.NativeFunctionStatement;
import org.dockbox.hartshorn.hsl.callable.NativeExecutionException;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.token.Token;

import java.util.List;

public interface NativeModule {
    Object call(Token at, Interpreter interpreter, String function, List<Object> arguments) throws NativeExecutionException;

    List<NativeFunctionStatement> supportedFunctions(Token moduleName);
}
