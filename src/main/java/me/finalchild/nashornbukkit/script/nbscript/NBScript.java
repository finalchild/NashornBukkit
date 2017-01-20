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

import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import me.finalchild.nashornbukkit.NashornBukkit;
import me.finalchild.nashornbukkit.script.Extension;
import me.finalchild.nashornbukkit.script.Host;
import me.finalchild.nashornbukkit.script.Script;
import me.finalchild.nashornbukkit.util.BukkitImporter;
import me.finalchild.nashornbukkit.util.CommandUtil;
import me.finalchild.nashornbukkit.util.ScriptExceptionLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class NBScript implements Script {

    private final Path file;
    private final Host host;

    private final String id;

    private final NashornScriptEngine engine;

    private final Logger logger;

    private final ScriptContext context;

    private final Map<String, Extension<NBScript>> installedExtensions = new HashMap<>();
    private final Map<String, Extension<NBScript>> extensionsBeingInstalled = new HashMap<>();

    private final Listener listener;

    public NBScript(Path file, Host host, NashornScriptEngine engine) {
        this.file = file;
        this.host = host;
        this.engine = engine;

        id = com.google.common.io.Files.getNameWithoutExtension(file.toString());
        String prefix = "[" + id + "] ";
        Logger logger = new Logger(getId(), null) {
            @Override
            public void log(LogRecord logRecord) {
                logRecord.setMessage(prefix + logRecord.getMessage());
                super.log(logRecord);
            }
        };
        logger.setParent(Bukkit.getServer().getLogger());
        logger.setLevel(Level.ALL);
        this.logger = logger;

        ScriptContext context = new SimpleScriptContext();
        Bindings bindings = getEngine().createBindings();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        bindings.put("script", this);
        bindings.remove("print");
        bindings.remove("load");
        bindings.remove("loadWithNewGlobal");
        bindings.remove("exit");
        bindings.remove("quit");
        try {
            Object global = getEngine().eval("this", context);
            getEngine().invokeMethod(bindings.get("Object"), "bindProperties", global, this);
            getEngine().invokeMethod(bindings.get("Object"), "bindProperties", global, NashornBukkit.getInstance().getServer());
        } catch (ScriptException e) {
            ScriptExceptionLogger.log(e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.context = context;

        listener = new Listener() {
        };
    }

    @Override
    public void eval() {
        require("finally");

        try {
            BukkitImporter.importBukkit(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bindings bindings = getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        try (BufferedReader br = Files.newBufferedReader(getFile())) {
            bindings.put(ScriptEngine.FILENAME, getFile().getFileName());
            getEngine().eval(br, getContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            ScriptExceptionLogger.log(e);
        }
    }

    @Override
    public void disable() {
        Object obj = getContext().getAttribute("onDisable", ScriptContext.ENGINE_SCOPE);
        if (obj instanceof JSObject) {
            JSObject jsobj = (JSObject) obj;
            if (jsobj.isFunction()) {
                jsobj.call(null);
            }
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

    public NashornScriptEngine getEngine() {
        return engine;
    }

    public Logger getLogger() {
        return logger;
    }

    public ScriptContext getContext() {
        return context;
    }

    public Map<String, Extension<NBScript>> getInstalledExtensions() {
        return Collections.unmodifiableMap(installedExtensions);
    }

    public void evalExtension(Extension<NBScript> extension) {
        if (extensionsBeingInstalled.containsKey(extension.getId())) {
            throw new UnsupportedOperationException("Circular dependency found: " + extension.getId());
        }
        if (getInstalledExtensions().containsKey(extension.getId())) {
            return;
        }

        extensionsBeingInstalled.put(extension.getId(), extension);

        extension.apply(this);

        extensionsBeingInstalled.remove(extension.getId());
        installedExtensions.put(extension.getId(), extension);
    }

    public Extension require(String id) {
        Extension extension = getHost().getExtension(id).orElseThrow(NoSuchElementException::new);
        evalExtension(extension);
        return extension;
    }

    public void on(StaticClass event, Consumer<Event> executor) {
        on((Class<? extends Event>) event.getRepresentedClass(), executor, EventPriority.NORMAL);
    }

    public void on(StaticClass event, Consumer<Event> executor, EventPriority eventPriority) {
        on((Class<? extends Event>) event.getRepresentedClass(), executor, eventPriority);
    }

    public void on(Class<? extends Event> event, Consumer<Event> executor) {
        on(event, executor, EventPriority.NORMAL);
    }

    public void on(Class<? extends Event> event, Consumer<Event> executor, EventPriority eventPriority) {
        NashornBukkit.getInstance().getServer().getPluginManager().registerEvent(event, listener, eventPriority, (listener, event1) -> executor.accept(event1), NashornBukkit.getInstance());
    }

    public void onCommand(String name, BiFunction<CommandSender, String[], Boolean> executor) {
        onCommand(name, new Command(name) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                try {
                    return executor.apply(sender, args);
                } catch (ClassCastException e) {
                    return true;
                }
            }
        });
    }

    public void onCommand(String name, Command command) {
        CommandUtil.register(name, command);
    }

    public BukkitTask runTask(Runnable runnable) {
        return runTask(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    public BukkitTask runTask(BukkitRunnable bukkitRunnable) {
        return bukkitRunnable.runTask(NashornBukkit.getInstance());
    }

    public BukkitTask runTaskAsynchronously(Runnable runnable) {
        return runTaskAsynchronously(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    public BukkitTask runTaskAsynchronously(BukkitRunnable bukkitRunnable) {
        return bukkitRunnable.runTaskAsynchronously(NashornBukkit.getInstance());
    }

    public BukkitTask runTaskLater(Runnable runnable, long delay) {
        return runTaskLater(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay);
    }

    public BukkitTask runTaskLater(BukkitRunnable bukkitRunnable, long delay) {
        return bukkitRunnable.runTaskLater(NashornBukkit.getInstance(), delay);
    }

    public BukkitTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return runTaskLaterAsynchronously(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay);
    }

    public BukkitTask runTaskLaterAsynchronously(BukkitRunnable bukkitRunnable, long delay) {
        return bukkitRunnable.runTaskLaterAsynchronously(NashornBukkit.getInstance(), delay);
    }

    public BukkitTask runTaskTimer(Runnable runnable, long delay, long period) {
        return runTaskTimer(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay, period);
    }

    public BukkitTask runTaskTimer(BukkitRunnable bukkitRunnable, long delay, long period) {
        return bukkitRunnable.runTaskTimer(NashornBukkit.getInstance(), delay, period);
    }

    public BukkitTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay, period);
    }

    public BukkitTask runTaskTimerAsynchronously(BukkitRunnable bukkitRunnable, long delay, long period) {
        return bukkitRunnable.runTaskTimerAsynchronously(NashornBukkit.getInstance(), delay, period);
    }

}
