/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by OsipXD on 07.12.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class FileUtils {

    @NotNull
    public static String stripExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static void resolveException(Path path) {
        try {
            final byte[] bytes = Files.readAllBytes(path);
            boolean isAllBytesAreZero = true;
            for (byte b : bytes) {
                if (b != 0) {
                    isAllBytesAreZero = false;
                    break;
                }
            }

            String newFileName = path.getFileName().toString().concat(isAllBytesAreZero ? ".gone" : ".broken");
            Files.move(path, path.getParent().resolve(newFileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

}
