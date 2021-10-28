/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.i18n.text.actions;

import org.dockbox.hartshorn.i18n.text.Text;

@SuppressWarnings({ "ClassReferencesSubclass", "AbstractClassWithoutAbstractMethods" })
public abstract class HoverAction<R> extends TextAction<R> {

    private HoverAction(final R result) {
        super(result);
    }

    public static ShowText showText(final Text text) {
        return new ShowText(text);
    }

    public static final class ShowText extends HoverAction<Text> {
        private ShowText(final Text result) {
            super(result);
        }
    }
}
