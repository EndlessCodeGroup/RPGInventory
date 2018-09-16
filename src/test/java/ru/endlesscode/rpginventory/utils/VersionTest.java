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

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    @Test
    public void shouldParseVersionInRightFormat() {
        Version version = Version.parseVersion("1.12.2-SNAPSHOT");

        Assert.assertEquals(new Version(1, 12, 2, "SNAPSHOT"), version);
        Assert.assertTrue(version.hasQualifier());
    }

    @Test
    public void shouldParseVersionInRightFormatWithoutQualifier() {
        Version version = Version.parseVersion("1.12.2");

        Assert.assertEquals(new Version(1, 12, 2), version);
        Assert.assertFalse(version.hasQualifier());
    }

    @Test
    public void shouldParseVersionInRightFormatWithoutPatchVersion() {
        Version version = Version.parseVersion("1.12-SNAPSHOT");

        Assert.assertEquals(new Version(1, 12, 0, "SNAPSHOT"), version);
        Assert.assertTrue(version.hasQualifier());
    }

    @Test
    public void shouldParseVersionInRightFormatWithoutPatchVersionAndQualifier() {
        Version version = Version.parseVersion("1.12");

        Assert.assertEquals(new Version(1, 12, 0), version);
        Assert.assertFalse(version.hasQualifier());
    }

    @Test
    public void shouldParseVersionInRightFormatWithoutMinorVersion() {
        Version version = Version.parseVersion("1-SNAPSHOT");

        Assert.assertEquals(new Version(1, 0, 0, "SNAPSHOT"), version);
        Assert.assertTrue(version.hasQualifier());
    }

    @Test
    public void shouldParseVersionInRightFormatWithoutMinorVersionAndQualifier() {
        Version version = Version.parseVersion("1");

        Assert.assertEquals(new Version(1, 0, 0), version);
        Assert.assertFalse(version.hasQualifier());
    }

    @Test
    public void shouldConvertVersionToStringRight() {
        Assert.assertEquals("1.2.3", new Version(1, 2, 3).toString());
        Assert.assertEquals("1.2.3-q", new Version(1, 2, 3, "q").toString());
    }

    @Test
    public void comparingShouldBeRight() {
        Version version = new Version(2, 1, 4);

        Assert.assertEquals(0, version.compareTo(new Version(2, 1, 4)));
        Assert.assertEquals(0, version.compareTo(new Version(2, 1, 4, "qualifier")));
        Assert.assertEquals(-1, version.compareTo(new Version(2, 1, 5)));
        Assert.assertEquals(-1, version.compareTo(new Version(2, 2, 0)));
        Assert.assertEquals(-1, version.compareTo(new Version(3, 0, 0)));
        Assert.assertEquals(1, version.compareTo(new Version(2, 1, 3)));
        Assert.assertEquals(1, version.compareTo(new Version(2, 0, 9)));
        Assert.assertEquals(1, version.compareTo(new Version(1, 9, 9)));
    }

    @Test
    public void comparingWithStringShouldBeRight() {
        Version version = new Version(2, 1, 4);

        Assert.assertEquals(0, version.compareTo("2.1.4"));
        Assert.assertEquals(0, version.compareTo("2.1.4-qualifier"));
        Assert.assertEquals(-1, version.compareTo("2.1.5"));
        Assert.assertEquals(-1, version.compareTo("2.2.0"));
        Assert.assertEquals(-1, version.compareTo("3.0.0"));
        Assert.assertEquals(1, version.compareTo("2.1.3"));
        Assert.assertEquals(1, version.compareTo("2.0.9"));
        Assert.assertEquals(1, version.compareTo("1.9.9"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPassedWrongString() {
        Version.parseVersion("1.0.0.0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPassedEmptyString() {
        Version.parseVersion("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPassedOnlyQualifier() {
        Version.parseVersion("-SNAPSHOT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPassedNegativeNumbers() {
        new Version(1, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPassedNullQualifier() {
        new Version(1, 0, 0, null);
    }
}
