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

package me.finalchild.nashornbukkit.script;

import me.finalchild.nashornbukkit.NashornBukkit;
import me.finalchild.nashornbukkit.util.BukkitImporter;
import me.finalchild.nashornbukkit.util.ScriptExceptionLogger;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Script {
    private Host host;
    private Path file;

    private String id;
    private Logger logger;

    private ScriptContext context;

    private Map<String, Extension> installedExtensions = new HashMap<>();
    private Map<String, Extension> extensionsBeingInstalled = new HashMap<>();

    public Script(Host host, Path file) throws ScriptException, IOException {
        this.host = host;
        this.file = file;

        String fileName = file.getFileName().toString();
        id = fileName.substring(0, fileName.length() - 3);
        Logger logger = Logger.getLogger(getId(), null);
        logger.setParent(NashornBukkit.getInstance().getServer().getLogger());
        logger.setLevel(Level.ALL);
        this.logger = logger;

        ScriptContext context = new SimpleScriptContext();
        Bindings bindings = getHost().getEngine().createBindings();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        bindings.put("script", this);
        bindings.put("require", (Function<String, Extension>) this::require);
        bindings.remove("print");
        bindings.remove("load");
        bindings.remove("loadWithNewGlobal");
        bindings.remove("exit");
        bindings.remove("quit");

        this.context = context;
    }

    public Object eval() throws IOException, ScriptException {
        require("finally");

        getHost().getImporter().importBukkit(this);

        Bindings bindings = getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        BufferedReader br2 = Files.newBufferedReader(getFile());
        bindings.put(ScriptEngine.FILENAME, getFile().getFileName());
        Object result = getHost().getEngine().eval(br2, getContext());
        br2.close();
        return result;
    }

    public Host getHost() {
        return host;
    }

    public Path getFile() {
        return file;
    }

    public String getId() {
        return id;
    }

    public Logger getLogger() {
        return logger;
    }

    public ScriptContext getContext() {
        return context;
    }

    public Map<String, Extension> getInstalledExtensions() {
        return installedExtensions;
    }

    public void evalExtension(Extension extension) throws IOException, ScriptException {
        if (extensionsBeingInstalled.containsKey(extension.getId())) {
            throw new UnsupportedOperationException();
        }

        if (getInstalledExtensions().containsKey(extension.getId())) {
            return;
        }

        extensionsBeingInstalled.put(extension.getId(), extension);

        getHost().getImporter().importBukkit(this, extension);

        Bindings bindings = getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put(ScriptEngine.FILENAME, extension.getFile().getFileName());
        BufferedReader br = Files.newBufferedReader(extension.getFile());
        getHost().getEngine().eval(br, getContext());
        br.close();

        extensionsBeingInstalled.remove(extension.getId());
        getInstalledExtensions().put(extension.getId(), extension);
    }

    public Extension require(String id) {
        Extension extension = getHost().getExtension(id).orElseThrow(NoSuchElementException::new);
        try {
            evalExtension(extension);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        } catch (ScriptException e) {
            ScriptExceptionLogger.log(e);
            throw new UnsupportedOperationException();
        }
        return extension;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Script && getFile().equals(((Script) o).getFile());
    }
}
