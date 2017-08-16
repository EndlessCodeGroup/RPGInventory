/*
 * This file is part of RPGInventory.
 * Copyright (C) 2017 EndlessCode Group and contributors
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

package ru.endlesscode.rpginventory.misc;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class SimpleI18N extends I18N {

    SimpleI18N(@NotNull File workDir) throws IOException {
        super(workDir, "test");
    }

    @NotNull
    @Override
    protected String stripColor(String message) {
        return message;
    }

    @NotNull
    @Override
    protected String translateCodes(String message) {
        return message;
    }
}
