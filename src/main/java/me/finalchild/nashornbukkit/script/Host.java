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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Host {

    private final Map<String, Module> loadedModules = new HashMap<>();
    private final Map<String, Script> loadedScripts = new HashMap<>();

    private final Map<String, ModuleLoader> moduleLoaders = new HashMap<>();
    private final Map<String, ScriptLoader> scriptLoaders = new HashMap<>();

    public void loadModules(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                if (!Files.isDirectory(file)) {
                    try {
                        loadModule(file);
                    } catch (Throwable t) {
                        NashornBukkit.getInstance().getLogger().severe("Failed to load a file as a extension: " + file.getFileName());
                        t.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Module loadModule(Path file) {
        return loadModule(getModuleLoader(file)
                .orElseThrow(() -> new UnsupportedOperationException("Could not find a ModuleLoader for the file: " + file.getFileName().toString()))
                .loadModule(file, this));
    }

    public Module loadModule(Module module) {
        if (loadedModules.containsKey(module.getId())) {
            throw new UnsupportedOperationException("Duplicate module id: " + module.getId());
        }
        loadedModules.put(module.getId(), module);
        return module;
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
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                if (!Files.isDirectory(file)) {
                    try {
                        loadScript(file);
                    } catch (Throwable t) {
                        NashornBukkit.getInstance().getLogger().severe("Failed to load a file as a script: " + file.getFileName());
                        t.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Script loadScript(Path file) {
        return loadScript(getScriptLoader(file)
                .orElseThrow(() -> new UnsupportedOperationException("Could not find a ScriptLoader for the file: " + file.getFileName().toString()))
                .loadScript(file, this));
    }

    public Script loadScript(Script script) {
        if (loadedScripts.containsKey(script.getId())) {
            throw new UnsupportedOperationException("Duplicate script id: " + script.getId());
        }
        loadedScripts.put(script.getId(), script);
        return script;
    }

    public void evalScripts() {
        for (Script loadedScript : loadedScripts.values()) {
            try {
                loadedScript.eval();
            } catch (Throwable t) {
                NashornBukkit.getInstance().getLogger().severe("Failed to run a script: " + loadedScript.getId());
                t.printStackTrace();
            }
        }
    }

    public Map<String, Module> getModules() {
        return Collections.unmodifiableMap(loadedModules);
    }

    public Optional<Module> getModule(String id) {
        return Optional.ofNullable(loadedModules.get(id));
    }

    public Map<String, Script> getScripts(String id) {
        return Collections.unmodifiableMap(loadedScripts);
    }

    public Optional<Script> getScript(String id) {
        return Optional.ofNullable(loadedScripts.get(id));
    }

    public void onDisable() {
        loadedScripts.values().forEach(Script::disable);
    }

    public Map<String, ModuleLoader> getModuleLoaders() {
        return Collections.unmodifiableMap(moduleLoaders);
    }

    public Optional<ModuleLoader> getModuleLoader(Path file) {
        return getModuleLoader(com.google.common.io.Files.getFileExtension(file.toString()));
    }

    public Optional<ModuleLoader> getModuleLoader(String fileExtension) {
        return Optional.ofNullable(moduleLoaders.get(fileExtension));
    }

    public void addModuleLoader(ModuleLoader loader, Set<String> fileExtensions) {
        fileExtensions.forEach((fileExtension) -> moduleLoaders.put(fileExtension, loader));
    }

    public Map<String, ScriptLoader> getScriptLoaders() {
        return Collections.unmodifiableMap(scriptLoaders);
    }

    public Optional<ScriptLoader> getScriptLoader(Path file) {
        return getScriptLoader(com.google.common.io.Files.getFileExtension(file.toString()));
    }

    public Optional<ScriptLoader> getScriptLoader(String fileExtension) {
        return Optional.ofNullable(scriptLoaders.get(fileExtension));
    }

    public void addScriptLoader(ScriptLoader loader, Set<String> fileExtensions) {
        fileExtensions.forEach((fileExtension) -> scriptLoaders.put(fileExtension, loader));
    }

}
