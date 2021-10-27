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

package org.dockbox.hartshorn.proxy.compiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    final Map<String, byte[]> classBytes = new HashMap<>();

    MemoryJavaFileManager(final JavaFileManager fileManager) {
        super(fileManager);
    }

    public Map<String, byte[]> classBytes() {
        return new HashMap<>(this.classBytes);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(final JavaFileManager.Location location, final String className, final Kind kind,
                                               final FileObject sibling) throws IOException {
        if (kind == Kind.CLASS) {
            return new MemoryOutputJavaFileObject(this, className);
        }
        else {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }

    @Override
    public void flush() {
        // Nothing to flush
    }

    @Override
    public void close() {
        this.classBytes.clear();
    }

    public JavaFileObject makeStringSource(final String name, final String code) {
        return new MemoryInputJavaFileObject(name, code);
    }

}
