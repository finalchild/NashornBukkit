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

package me.finalchild.nashornbukkit;

import me.finalchild.nashornbukkit.script.Host;
import me.finalchild.nashornbukkit.script.nbscript.NBModuleLoader;
import me.finalchild.nashornbukkit.script.nbscript.NBScriptLoader;
import me.finalchild.nashornbukkit.util.BukkitImporter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

public final class NashornBukkit extends JavaPlugin {

    private static NashornBukkit instance;

    private Host host;

    public static NashornBukkit getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        host = new Host();

        saveResource("modules/finally.js", true);
        BukkitImporter.setCaching(true);
        BukkitImporter.getTypes();

        getHost().addModuleLoader(new NBModuleLoader(), Collections.singleton("js"));
        getHost().addScriptLoader(new NBScriptLoader(), Collections.singleton("js"));

        getHost().loadModules(getDataFolder().toPath().resolve("modules"));
        getHost().loadScripts(getDataFolder().toPath());

        new BukkitRunnable() {
            @Override
            public void run() {
                getHost().evalScripts();
                BukkitImporter.setCaching(false);
            }
        }.runTask(this);
    }

    @Override
    public void onDisable() {
        getHost().onDisable();
    }

    public Host getHost() {
        return host;
    }

}
