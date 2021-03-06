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
import org.bukkit.plugin.java.JavaPlugin;

public class NashornBukkit extends JavaPlugin {

    private static NashornBukkit instance;

    private Host host;

    public NashornBukkit() {
        instance = this;
        host = new Host();
    }

    public static NashornBukkit getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        saveResource("extensions/finally.js", true);

        getHost().loadExtensions(getDataFolder().toPath().resolve("extensions"));
        getHost().loadScripts(getDataFolder().toPath());
        getHost().evalScripts();
    }

    @Override
    public void onDisable() {
        getHost().onDisable();
    }

    public Host getHost() {
        return host;
    }
}
