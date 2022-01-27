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

package org.dockbox.hartshorn.core.task.pipeline.pipes;

import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.dockbox.hartshorn.core.task.pipeline.pipelines.AbstractPipeline;
import org.dockbox.hartshorn.core.context.element.TypeContext;

/**
 * @deprecated Moved to https://github.com/GuusLieben/JPipelines
 */
@Deprecated(forRemoval = true, since = "22.2")
@FunctionalInterface
public interface ComplexPipe<I, O> extends IPipe<I, O> {

    static <I, O> ComplexPipe<I, O> of(final ComplexPipe<I, O> pipe) {
        return pipe;
    }

    O apply(AbstractPipeline<?, I> pipeline, I input, Throwable throwable) throws ApplicationException;

    @Override
    default TypeContext<? extends IPipe> type() {
        return TypeContext.of(ComplexPipe.class);
    }
}
