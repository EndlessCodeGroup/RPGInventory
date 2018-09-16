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


import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents Version according to semantic versioning.
 * See: https://semver.org/
 */
public final class Version implements Comparable<Version> {

    private final int major;
    private final int minor;
    private final int patch;
    private final String qualifier;

    /**
     * Constructs Version object from the given data according to semantic versioning.
     *
     * @param major Major version.
     * @param minor Minor version.
     * @param patch Patch version.
     */
    public Version(int major, int minor, int patch) {
        this(major, minor, patch, "");
    }

    /**
     * Constructs Version object from the given data according to semantic versioning.
     *
     * @param major     Major version.
     * @param minor     Minor version.
     * @param patch     Patch version.
     * @param qualifier Qualifier, or empty string if qualifier not exists.
     * @throws IllegalArgumentException when passed negative version codes.
     */
    public Version(int major, int minor, int patch, @NotNull String qualifier) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version can't include negative numbers");
        }

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.qualifier = qualifier;
    }

    /**
     * Parses version from the given string.
     *
     * @param version String representation of version.
     * @return The constructed version.
     * @throws IllegalArgumentException If passed string with wrong format.
     */
    @NotNull
    public static Version parseVersion(@NotNull String version) {
        String[] parts = version.split("-", 2);
        String qualifier = (parts.length > 1) ? parts[1] : "";
        parts = parts[0].split("\\.", 3);

        try {
            int patch = (parts.length > 2) ? Integer.parseInt(parts[2]) : 0;
            int minor = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
            int major = Integer.parseInt(parts[0]);

            return new Version(major, minor, patch, qualifier);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Can not parse version string: %s", version), e);
        }
    }

    public int compareTo(String version) {
        return this.compareTo(Version.parseVersion(version));
    }

    @Override
    public int compareTo(@NotNull Version o) {
        int result;
        if ((result = Integer.compare(major, o.major)) != 0
                || (result = Integer.compare(minor, o.minor)) != 0
                || (result = Integer.compare(patch, o.patch)) != 0) {
            // TODO: Maybe compare also qualifiers
            return result;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o || o == null || getClass() != o.getClass()) {
            return true;
        }

        Version version = (Version) o;
        return major == version.major
                && minor == version.minor
                && patch == version.patch
                && Objects.equals(qualifier, version.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, qualifier);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d%s", major, minor, patch, qualifier.isEmpty() ? "" : "-" + qualifier);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @NotNull
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Checks that version has qualifier.
     *
     * @return true if version contains qualifier, otherwise false.
     */
    public boolean hasQualifier() {
        return !qualifier.isEmpty();
    }
}
