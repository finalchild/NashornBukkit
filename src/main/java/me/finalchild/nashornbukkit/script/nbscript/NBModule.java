/*
 * This file is part of NashornBukkit, licensed under the MIT license (MIT).
 *
 * Copyright (c) Final Child <http://finalchild.me>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.finalchild.nashornbukkit.script.nbscript;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import me.finalchild.nashornbukkit.script.Host;
import me.finalchild.nashornbukkit.script.Module;
import me.finalchild.nashornbukkit.util.BukkitImporter;
import me.finalchild.nashornbukkit.util.ScriptExceptionLogger;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NBModule implements Module<NBScript> {

    private final Path file;

    private final Host host;
    private final String id;

    private final CompiledScript compiledScript;

    public NBModule(Path file, Host host, NashornScriptEngine engine) throws IOException, ScriptException {
        this.file = file;
        this.host = host;

        id = com.google.common.io.Files.getNameWithoutExtension(file.toString());

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            compiledScript = engine.compile(br);
        }
    }

    @Override
    public void apply(NBScript script) {
        try {
            BukkitImporter.importBukkit(script, this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            script.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(ScriptEngine.FILENAME, getFile().getFileName());
            compiledScript.eval(script.getContext());
        } catch (ScriptException e) {
            ScriptExceptionLogger.log(e);
        }
    }

    public Path getFile() {
        return file;
    }

    @Override
    public Host getHost() {
        return host;
    }

    @Override
    public String getId() {
        return id;
    }

}
