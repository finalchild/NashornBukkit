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

package me.finalchild.nashornbukkit.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.options.Options;
import me.finalchild.nashornbukkit.NashornBukkit;
import me.finalchild.nashornbukkit.script.Extension;
import me.finalchild.nashornbukkit.script.Script;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BukkitImporter {
    private boolean caching;
    private Map<String, ClassPath.ClassInfo> typesCache;

    public Map<String, ClassPath.ClassInfo> getTypes() {
        return getTypes(caching);
    }

    public Map<String, ClassPath.ClassInfo> getTypes(boolean cache) {
        if (typesCache != null) {
            return typesCache;
        }

        ClassPath classpath;
        try {
            classpath = ClassPath.from(NashornBukkit.class.getClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        ImmutableSet<ClassPath.ClassInfo> result = classpath.getTopLevelClassesRecursive("org.bukkit");
        Map<String, ClassPath.ClassInfo> types = result.stream()
                    .filter(e -> !(e.getName().startsWith("org.bukkit.craftbukkit")))
                    .filter(e -> !(e.getSimpleName().equals("package-info")))
                    .collect(Collectors.toMap(ClassPath.ClassInfo::getSimpleName, Function.identity(), (a, b) -> {
                        NashornBukkit.getInstance().getLogger().info("Duplicate class name: " + a.getName() + " and " + b.getName());
                        return a;
                    }));

        if (cache) {
            typesCache = types;
        }
        return Collections.unmodifiableMap(types);
    }

    public void setCaching(boolean caching) {
        if (!caching) {
            typesCache = null;
        }
        this.caching = caching;
    }

    public void importBukkit(Script script) throws ScriptException, IOException {
        Bindings bindings = script.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        Map<String, ClassPath.ClassInfo> types = getTypes();

        Set<String> usedIdentifiers = getUsedIdentifiers(script.getFile());
        usedIdentifiers.stream()
                .filter(e -> Character.isUpperCase(e.charAt(0)))
                .filter(types::containsKey)
                .forEach(e -> {
                    ClassPath.ClassInfo type = types.get(e);
                    try {
                        bindings.put(e, script.getHost().getEngine().eval("Java.type(\"" + type.getName() + "\")", script.getContext()));
                    } catch (ScriptException e1) {
                        e1.printStackTrace();
                    }
                });
    }

    public void importBukkit(Script script, Extension extension) throws ScriptException, IOException {
        Bindings bindings = script.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        Map<String, ClassPath.ClassInfo> types = getTypes();

        Set<String> usedIdentifiers = getUsedIdentifiers(extension.getFile());
        usedIdentifiers.stream()
                .filter(e -> Character.isUpperCase(e.charAt(0)))
                .filter(types::containsKey)
                .forEach(e -> {
                    ClassPath.ClassInfo type = types.get(e);
                    try {
                        bindings.put(e, script.getHost().getEngine().eval("Java.type(\"" + type.getName() + "\")", script.getContext()));
                    } catch (ScriptException e1) {
                        e1.printStackTrace();
                    }
                });
    }

    public Set<String> getUsedIdentifiers(Path file) throws IOException {
        Set<String> result = new HashSet<>();
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        String json;

        Options options = new Options("");
        ErrorManager errorManager = new ErrorManager();
        Context context = new Context(options, errorManager, NashornBukkit.class.getClassLoader());
        Context.setGlobal(context.createGlobal());
        json = ScriptUtils.parse(text, file.getFileName().toString(), false);

        JsonParser jsonParser = new JsonParser();
        JsonObject rootObj = jsonParser.parse(json).getAsJsonObject();

        addUsedIdentifiers(rootObj, result);
        return result;
    }

    private void addUsedIdentifiers(JsonObject obj, Set<String> set) {
        if (obj.has("type")) {
            String type = obj.getAsJsonPrimitive("type").getAsString();
            if (type.equals("Identifier")) {
                set.add(obj.getAsJsonPrimitive("name").getAsString());
                return;
            }
        }
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                addUsedIdentifiers(entry.getValue().getAsJsonObject(), set);
            } else if (entry.getValue().isJsonArray()) {
                addUsedIdentifiers(entry.getValue().getAsJsonArray(), set);
            }
        }
    }

    private void addUsedIdentifiers(JsonArray arr, Set<String> set) {
        for (JsonElement element : arr) {
            if (element.isJsonObject()) {
                addUsedIdentifiers(element.getAsJsonObject(), set);
            } else if (element.isJsonArray()) {
                addUsedIdentifiers(element.getAsJsonArray(), set);
            }
        }
    }
}
