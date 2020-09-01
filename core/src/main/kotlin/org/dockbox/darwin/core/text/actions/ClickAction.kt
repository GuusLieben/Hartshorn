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

package org.dockbox.darwin.core.text.actions

import TextAction
import org.dockbox.darwin.core.objects.targets.CommandSource
import org.dockbox.darwin.core.text.Text
import java.net.URL
import java.util.function.Consumer

abstract class ClickAction<R>

internal constructor(result: R) : TextAction<R>(result) {
    override fun applyTo(text: Text) {
        text.onClick(this)
    }

    class OpenUrl
    internal constructor(url: URL) : ClickAction<URL?>(url)

    class RunCommand
    internal constructor(command: String) : ClickAction<String?>(command)

    class ChangePage
    internal constructor(page: Int) : ClickAction<Int?>(page)

    class SuggestCommand
    internal constructor(command: String) : ClickAction<String?>(command)

    class ExecuteCallback
    internal constructor(result: Consumer<CommandSource?>) : ClickAction<Consumer<CommandSource?>?>(result)
}
