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

package me.finalchild.nashornbukkit.command;

import me.finalchild.nashornbukkit.NashornBukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

public class NBCommandUtil {

    public static boolean register(String name, Command command) {
        Server server = NashornBukkit.getInstance().getServer();
        Field commandMapField;
        try {
            commandMapField = server.getClass().getDeclaredField("commandMap");
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException(); // TODO: Add a message.
        }
        commandMapField.setAccessible(true);
        CommandMap commandMap;
        try {
            commandMap = (CommandMap) commandMapField.get(server);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException();
        }
        return commandMap.register(name, command);
    }

    public static boolean register(String name, BiFunction<CommandSender, String[], Boolean> command) {
        Server server = NashornBukkit.getInstance().getServer();
        Field commandMapField;
        try {
            commandMapField = server.getClass().getDeclaredField("commandMap");
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException(); // TODO: Add a message.
        }
        commandMapField.setAccessible(true);
        CommandMap commandMap;
        try {
            commandMap = (CommandMap) commandMapField.get(server);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException();
        }
        return commandMap.register(name, new NBCommand(name, command));
    }


}
