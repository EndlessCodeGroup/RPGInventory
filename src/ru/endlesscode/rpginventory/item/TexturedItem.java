package ru.endlesscode.rpginventory.item;

/**
 * Created by OsipXD on 28.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class TexturedItem {
    protected final String texture;

    protected TexturedItem(String texture) {
        this.texture = texture;
    }

    public short getTextureDurability() {
        String[] data = texture.split(":");

        if (data.length == 2) {
            try {
                return  Short.parseShort(data[1]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }
}
