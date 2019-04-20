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

package ru.endlesscode.rpginventory.resourcepack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by OsipXD on 22.11.2017
 * It is part of the RPGInventory.
 * All rights reserved 2014 - 2017 © «EndlessCode Group»
 */
final class ResourcePackValidator {

    private static final String ENDLESSCODE_CLOUD = "cloud.endlesscode.ru";
    private static final String EMPTY_URL = "PUT_YOUR_URL_HERE";
    private static final String EMPTY_HASH = "PUT_YOUR_HASH_HERE";

    private static final String HEADER_LOCATION = "Location";
    private static final String MIME_ZIP = "application/zip";

    private static final int CODE_MOVED_PERMANENTLY = 301;
    private static final int CODE_FOUND = 302;
    private static final int CODE_TEMPORARY_REDIRECT = 307;
    private static final int CODE_PERMANENT_REDIRECT = 308;

    private List<String> errors = new ArrayList<>();

    /**
     * Checks that resource-pack with given URL and hash can be used.
     * Also stores all validation errors. You can get it with {@link #getErrors}.
     *
     * @param resourcePackUrl  URL of the resource-pack
     * @param resourcePackHash Hash of the resource-pack
     * @return false if this resource-pack can't be used, otherwise true.
     */
    @Contract("null, _ -> false")
    boolean validateUrlAndHash(String resourcePackUrl, String resourcePackHash) {
        if (!isLegalUrl(resourcePackUrl)) {
            return false;
        }

        checkHashErrors(resourcePackHash);
        try {
            validateUrl(resourcePackUrl);
        } catch (IllegalArgumentException e) {
            addErrors(e.getMessage().split("\n"));
        } catch (Exception e) {
            addErrors(e.toString());
        }
        return true;
    }

    /**
     * Coarse check of the URL
     *
     * @return true if URL is present and legal
     */
    @Contract("null -> false")
    private boolean isLegalUrl(String url) {
        if (url == null || EMPTY_URL.equals(url)) {
            addErrors("Set 'resource-pack.url' in config or disable it!");
            return false;
        }
        if (url.contains(ENDLESSCODE_CLOUD)) {
            addErrors("You should not use EndlessCode's cloud for resource-pack.",
                    "Please, upload resource-pack to own host or use any third-party file hosting",
                    "that can provide direct download link.");
            return false;
        }
        return true;
    }

    private void checkHashErrors(String resourcePackHash) {
        if (resourcePackHash == null || EMPTY_HASH.equals(resourcePackHash)) {
            addErrors("Setting 'resource-pack.hash' is missing!",
                    "You should calculate SHA1 hash of RP and paste it into the setting.");
        }
    }

    private void validateUrl(@NotNull String address) throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection conn;
        try {
            conn = getRealConnection(address, false);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Wrong URL: " + e.getMessage());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error: " + e.toString());
        }

        String realUrl = conn.getURL().toString();
        if (!realUrl.equals(address)) {
            addError(
                    String.format("Link isn't direct. Redirect found!\n"
                            + "Try to replace your link: %s\n"
                            + "By this link: %s", address, realUrl)
            );
        }

        conn = getRealConnection(conn.getURL().toString(), true);
        if (conn.getResponseCode() != 200) {
            throw new IllegalArgumentException("Response: " + conn.getResponseMessage());
        }

        String contentType = conn.getContentType();
        if (!contentType.equals(MIME_ZIP)) {
            throw new IllegalArgumentException(
                    String.format("MIME type should be '%s' but received '%s'\n"
                            + "Please provide link to resource pack file, not to download page.", MIME_ZIP, contentType)
            );
        }
    }

    @NotNull
    private HttpURLConnection getRealConnection(@NotNull String address, boolean trackTemporaryRedirects)
            throws IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int code = conn.getResponseCode();
        boolean isPermanentRedirect = code == CODE_MOVED_PERMANENTLY || code == CODE_PERMANENT_REDIRECT;
        boolean isTemporaryRedirect = code == CODE_FOUND || code == CODE_TEMPORARY_REDIRECT;
        if (isPermanentRedirect || trackTemporaryRedirects && isTemporaryRedirect) {
            String redirectLocation = conn.getHeaderField(HEADER_LOCATION);
            if (redirectLocation.startsWith("/")) {
                redirectLocation = getAbsoluteUrl(url, redirectLocation);
            }
            return getRealConnection(redirectLocation, trackTemporaryRedirects);
        }

        return conn;
    }

    private String getAbsoluteUrl(@NotNull URL url, @NotNull String relativeUrl) {
        final String protocol = url.getProtocol();
        final String host = url.getHost();
        final int port = url.getPort();

        if (port == -1) {
            return String.format("%s://%s%s", protocol, host, relativeUrl);
        } else {
            return String.format("%s://%s:%d%s", protocol, host, port, relativeUrl);
        }
    }

    private void addError(@NotNull String message) {
        addErrors(message.split("\n"));
    }

    private void addErrors(String... messages) {
        Collections.addAll(errors, messages);
    }

    @Contract(pure = true)
    List<String> getErrors() {
        return errors;
    }
}
