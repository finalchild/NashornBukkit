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
import me.finalchild.nashornbukkit.NashornBukkit;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Host {

    private final Map<String, Extension> loadedExtensions = new HashMap<>();
    private final Map<String, Script> loadedScripts = new HashMap<>();

    private final Map<String, ScriptLoader> scriptLoaders = new HashMap<>();
    private final Map<String, ExtensionLoader> extensionLoaders = new HashMap<>();

    public void loadExtensions(Path directory) {
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
                        loadExtension(file);
                    } catch (Throwable t) {
                        NashornBukkit.getInstance().getLogger().severe("Failed to load a file as a extension: " + file.getFileName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Extension loadExtension(Path file) {
        return loadExtension(getExtensionLoader(file)
                .orElseThrow(() -> new UnsupportedOperationException("Could not find a ExtensionLoader for the file: " + file.getFileName().toString()))
                .loadExtension(file, this));
    }

    public Extension loadExtension(Extension extension) {
        if (loadedExtensions.containsKey(extension.getId())) {
            throw new UnsupportedOperationException("Duplicate extension id: " + extension.getId());
        }
        loadedExtensions.put(extension.getId(), extension);
        return extension;
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
            }
        }
    }

    public Map<String, Extension> getExtensions() {
        return Collections.unmodifiableMap(loadedExtensions);
    }

    public Optional<Extension> getExtension(String id) {
        return Optional.ofNullable(loadedExtensions.get(id));
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

    public Map<String, ExtensionLoader> getExtensionLoaders() {
        return Collections.unmodifiableMap(extensionLoaders);
    }

    public Optional<ExtensionLoader> getExtensionLoader(Path file) {
        return getExtensionLoader(com.google.common.io.Files.getFileExtension(file.toString()));
    }

    public Optional<ExtensionLoader> getExtensionLoader(String fileExtension) {
        return Optional.ofNullable(extensionLoaders.get(fileExtension));
    }

    public void addExtensionLoader(ExtensionLoader loader, Set<String> fileExtensions) {
        fileExtensions.forEach((fileExtension) -> extensionLoaders.put(fileExtension, loader));
    }

}
