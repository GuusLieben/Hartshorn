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

package org.dockbox.hartshorn.hsl.interpreter.expression;

import java.util.function.BiPredicate;

import org.dockbox.hartshorn.hsl.ast.expression.BinaryExpression;
import org.dockbox.hartshorn.hsl.interpreter.ASTNodeInterpreter;
import org.dockbox.hartshorn.hsl.interpreter.Array;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.interpreter.InterpreterUtilities;
import org.dockbox.hartshorn.hsl.runtime.RuntimeError;
import org.dockbox.hartshorn.hsl.token.type.ArithmeticTokenType;
import org.dockbox.hartshorn.hsl.token.type.ConditionTokenType;

public class BinaryExpressionInterpreter implements ASTNodeInterpreter<Object, BinaryExpression> {

    @Override
    public Object interpret(final BinaryExpression node, final Interpreter interpreter) {
        Object left = interpreter.evaluate(node.leftExpression());
        Object right = interpreter.evaluate(node.rightExpression());

        left = InterpreterUtilities.unwrap(left);
        right = InterpreterUtilities.unwrap(right);

        return switch (node.operator().type()) {
            case ArithmeticTokenType.PLUS -> {
                // Math plus
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                // String Addition
                if (left instanceof String || right instanceof String) {
                    // String.valueOf to handle nulls
                    yield String.valueOf(left) + right;
                }

                // Special cases
                if ((left instanceof Character && right instanceof Character)) {
                    yield String.valueOf(left) + right;
                }
                if ((left instanceof Character) && (right instanceof Double)) {
                    int value = (Character) left;
                    yield (double) right + value;
                }
                if ((left instanceof Double) && (right instanceof Character)) {
                    int value = (Character) right;
                    yield (double) left + value;
                }
                throw new RuntimeError(node.operator(), "Unsupported child for PLUS.\n");
            }
            case ArithmeticTokenType.MINUS -> {
                InterpreterUtilities.checkNumberOperands(node.operator(), left, right);
                yield (double) left - (double) right;
            }
            case ArithmeticTokenType.STAR -> {
                if ((left instanceof String || left instanceof Character) && right instanceof Double) {
                    int times = (int) ((double) right);
                    int finalLen = left.toString().length() * times;
                    StringBuilder result = new StringBuilder(finalLen);
                    String strValue = left.toString();
                    result.append(strValue.repeat(Math.max(0, times)));
                    yield result.toString();
                }
                else if (left instanceof Array array && right instanceof Double) {
                    int times = (int) ((double) right);
                    int finalLen = array.length() * times;
                    Array result = new Array(finalLen);
                    for (int i = 0; i < times; i++) {
                        int originalIndex = times % array.length();
                        result.value(array.value(originalIndex), i);
                    }
                    yield result;
                }
                InterpreterUtilities.checkNumberOperands(node.operator(), left, right);
                yield (double) left * (double) right;
            }
            case ArithmeticTokenType.MODULO -> {
                InterpreterUtilities.checkNumberOperands(node.operator(), left, right);
                yield (double) left % (double) right;
            }
            case ArithmeticTokenType.SLASH -> {
                InterpreterUtilities.checkNumberOperands(node.operator(), left, right);
                if ((double) right == 0) {
                    throw new RuntimeError(node.operator(), "Can't use slash with zero double.");
                }
                yield (double) left / (double) right;
            }
            case ConditionTokenType.GREATER -> compareNumbers(node, left, right, (l, r) -> l > r);
            case ConditionTokenType.GREATER_EQUAL -> compareNumbers(node, left, right, (l, r) -> l >= r);
            case ConditionTokenType.LESS -> compareNumbers(node, left, right, (l, r) -> l < r);
            case ConditionTokenType.LESS_EQUAL -> compareNumbers(node, left, right, (l, r) -> l <= r);
            case ConditionTokenType.BANG_EQUAL -> !InterpreterUtilities.isEqual(left, right);
            case ConditionTokenType.EQUAL_EQUAL -> InterpreterUtilities.isEqual(left, right);
            default -> null;
        };
    }

    private boolean compareNumbers(BinaryExpression expression, Object left, Object right, BiPredicate<Double, Double> predicate) {
        InterpreterUtilities.checkNumberOperands(expression.operator(), left, right);
        return predicate.test(Double.parseDouble(left.toString()), Double.parseDouble(right.toString()));
    }
}
