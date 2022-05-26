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

package org.dockbox.hartshorn.i18n.services;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.processing.ServicePreProcessor;
import org.dockbox.hartshorn.i18n.Message;
import org.dockbox.hartshorn.i18n.TranslationBundle;
import org.dockbox.hartshorn.i18n.TranslationService;
import org.dockbox.hartshorn.i18n.annotations.TranslationProvider;
import org.dockbox.hartshorn.inject.Key;
import org.dockbox.hartshorn.util.reflect.MethodContext;

public class LanguageProviderServicePreProcessor implements ServicePreProcessor {

    @Override
    public boolean preconditions(final ApplicationContext context, final Key<?> key) {
        return !key.type().methods(TranslationProvider.class).isEmpty();
    }

    @Override
    public <T> void process(final ApplicationContext context, final Key<T> key) {
        final TranslationService translationService = context.get(TranslationService.class);
        for (final MethodContext<?, T> method : key.type().methods(TranslationProvider.class)) {
            if (method.returnType().childOf(TranslationBundle.class)) {
                final TranslationBundle bundle = (TranslationBundle) method.invoke(context).rethrowUnchecked().get();
                translationService.add(bundle);
            }
            else if (method.returnType().childOf(Message.class)) {
                final Message message = (Message) method.invoke(context).rethrowUnchecked().get();
                translationService.add(message);
            }
        }
    }
}
