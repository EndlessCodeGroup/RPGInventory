/*
 * This file is part of RPGInventory.
 * Copyright (C) 2018 EndlessCode Group and contributors
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.utils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogTest {

    private static final Logger logger = Mockito.mock(Logger.class);

    @BeforeClass
    public static void init() {
        Log.init(logger);
    }

    @Test
    public void shouldApplyArgsToMessage() {
        Exception exception = new Exception("Exception message");

        Log.i("Info {0}, {1}", 0, "arg1");
        Mockito.verify(logger).info("Info 0, arg1");

        Log.w(exception);
        Mockito.verify(logger).log(Level.WARNING, "Exception message", exception);

        Log.w("Warning {0}, {1}", 0, "arg1");
        Mockito.verify(logger).warning("Warning 0, arg1");

        Log.w(exception, "Warning {0}, {1}", 0, "arg1");
        Mockito.verify(logger).log(Level.WARNING, "Warning 0, arg1", exception);

        Log.s("Severe {0}, {1}", 0, "arg1");
        Mockito.verify(logger).severe("Severe 0, arg1");
    }

    @Test
    public void shouldApplyArgsToMessageWithQuotes() {
        Log.i("Info ''{0}'', \"{1}\"", "q", "qq");
        Mockito.verify(logger).info("Info 'q', \"qq\"");
    }

}
