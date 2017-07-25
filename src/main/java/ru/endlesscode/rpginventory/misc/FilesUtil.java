package ru.endlesscode.rpginventory.misc;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class FilesUtil {

    public static String readFileToString(File file, Charset charset) {
        try {
            return FileUtils.readFileToString(file, charset);
        } catch (IOException e) {
            //TODO: Log exception
            throw new IllegalArgumentException(e);
        }
    }
}
