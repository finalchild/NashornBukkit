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

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import me.finalchild.nashornbukkit.NashornBukkit;
import me.finalchild.nashornbukkit.util.BukkitImporter;
import me.finalchild.nashornbukkit.util.ScriptExceptionLogger;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Host {

    private NashornScriptEngine engine;
    private Map<String, Extension> loadedExtensions = new HashMap<>();
    private Map<String, Script> loadedScripts = new HashMap<>();
    private BukkitImporter importer;

    public Host() {
        engine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine(/*new String[] {"-scripting"}, */NashornBukkit.class.getClassLoader());
        importer = new BukkitImporter();
    }

    public void loadExtensions(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.js")) {
            for (Path file : stream) {
                loadExtension(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadScripts(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.js")) {
            for (Path file : stream) {
                try {
                    loadScript(file);
                } catch (ScriptException e) {
                    ScriptExceptionLogger.log(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void evalScripts() {
        getImporter().setCaching(true);

        for (Script loadedScript : loadedScripts.values()) {
            try {
                loadedScript.eval();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ScriptException e) {
                ScriptExceptionLogger.log(e);
            }
        }

        getImporter().setCaching(false);
    }

    private void loadExtension(Path file) {
        Extension extension = new Extension(this, file);
        loadedExtensions.put(extension.getId(), extension);
    }

    public void loadScript(Path file) throws IOException, ScriptException {
        Script script = new Script(this, file);
        loadedScripts.put(script.getId(), script);
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public Optional<Extension> getExtension(String id) {
        return Optional.ofNullable(loadedExtensions.get(id));
    }

    public Map<String, Extension> getExtensions() {
        return loadedExtensions;
    }

    public Optional<Script> getScript(String id) {
        return Optional.ofNullable(loadedScripts.get(id));
    }

    public Map<String, Script> getScripts(String id) {
        return loadedScripts;
    }

    public BukkitImporter getImporter() {
        return importer;
    }

    public void onDisable() {
        loadedScripts.values().forEach(Script::disable);
    }
}
