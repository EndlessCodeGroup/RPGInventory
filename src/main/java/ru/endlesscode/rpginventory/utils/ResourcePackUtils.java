/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.utils;

import org.jetbrains.annotations.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by OsipXD on 22.11.2017
 * It is part of the RPGInventory.
 * All rights reserved 2014 - 2017 © «EndlessCode Group»
 */
public final class ResourcePackUtils {

    private static final String HEADER_LOCATION = "Location";
    private static final String MIME_ZIP = "application/zip";

    private ResourcePackUtils() {
        // Utility class
    }

    public static void validateUrl(@NotNull String address) throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection conn;
        try {
            conn = getRealConnection(address);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Wrong URL: " + e.getMessage());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error: " + e.getMessage());
        }

        String realUrl = conn.getURL().toString();
        if (!realUrl.equals(address)) {
            throw new IllegalArgumentException(
                    String.format("Link isn't direct. Redirect found!\n"
                            + "Try to replace your link: %s\n"
                            + "By this link: %s\n"
                            + "If your link works normally, just ignore this message.", address, realUrl)
            );
        }

        int codeType = conn.getResponseCode() / 100;
        if (codeType != 2) {
            throw new IllegalArgumentException(conn.getResponseMessage());
        }

        String contentType = conn.getContentType();
        if (!contentType.equals(MIME_ZIP)) {
            throw new IllegalArgumentException(
                    String.format("MIME type should be '%s' but given '%s'\n"
                            + "Please provide link to resource pack file, not to download page.", MIME_ZIP, contentType)
            );
        }
    }

    @NotNull
    private static HttpURLConnection getRealConnection(@NotNull String address) throws IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int codeType = conn.getResponseCode() / 100;
        if (codeType == 3) {
            return getRealConnection(conn.getHeaderField(HEADER_LOCATION));
        }

        return conn;
    }
}
