package io.ddavison.conductor.util;

import io.ddavison.conductor.Locomotive;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;

/**
 * Created on 7/25/16.
 */
public class ScreenShotUtil {

    private static final String DIR = "/target/test-screenshots";
    private static final String WORKING_DIR = System.getProperty("user.dir");
    private static final String PNG_EXT = ".png";

    public static void take(Locomotive locomotive, String testName) {
        writeFile((TakesScreenshot) locomotive.driver, createFilePathAndName(testName));
    }

    public static void take(Locomotive locomotive, String path, String testName) {
        writeFile((TakesScreenshot) locomotive.driver, createFilePathAndName(path, testName));
    }

    private static void writeFile(TakesScreenshot takesScreenshot, String filePathAndName) {
        try {
            FileUtils.copyFile(takesScreenshot.getScreenshotAs(OutputType.FILE), new File(filePathAndName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createFilePathAndName(String testName) {
        return WORKING_DIR
                + File.separator
                + DIR
                + File.separator
                + limitLengthTo100Chars(removeInvalidFilenameChars(testName))
                + PNG_EXT;
    }

    private static String createFilePathAndName(String path, String testName) {
        return WORKING_DIR
                + File.separator
                + DIR
                + File.separator
                + limitLengthTo100Chars(removeInvalidFilenameChars(path))
                + File.separator
                + limitLengthTo100Chars(removeInvalidFilenameChars(testName))
                + PNG_EXT;
    }

    private static String removeInvalidFilenameChars(String name) {
        return name.replace(File.separator, "-")
                .replace(":", "-")
                .replace("?", "-")
                .replace("*", "-")
                .replace("|", "-")
                .replace(":", "-");
    }

    private static String limitLengthTo100Chars(String name) {
        return name.substring(0, name.length() > 100 ? 100 : name.length());
    }
}

