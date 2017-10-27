/*
 * Copyright 2014-2016 Daniel Davison (http://github.com/ddavison) and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package io.ddavison.conductor;

import com.google.common.base.Strings;
import io.ddavison.conductor.util.JvmUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

enum Platform {
    LINUX,
    WINDOWS,
    MAC
}

/**
 * the base test that includes all Selenium 2 functionality that you will need
 * to get you rolling.
 * @author ddavison
 *
 */
public class Locomotive implements Conductor<Locomotive> {

    public static final Logger log = LogManager.getLogger(Locomotive.class);

    /**
     * All test configuration in here
     */
    public Config configuration;

    public WebDriver driver;

    // max seconds before failing a script.
    public int MAX_ATTEMPTS = 5;
    public int MAX_TIMEOUT = 5;

    private int attempts = 0;

    public Actions actions;

    private Map<String, String> vars = new HashMap<String, String>();

    /**
     * The url that an automated test will be testing.
     */
    public String baseUrl;

    private Pattern p;
    private Matcher m;

    public Locomotive() {
        final Properties props = new Properties();
        try {
            System.out.println("CLASS: " + getClass());
            props.load(getClass().getResourceAsStream("/default.properties"));
        } catch (IOException e) {
            logFatal("Couldn't load in default properties");
        } catch (Exception e) {}

        try {
            // Set the webdriver env vars.
            if (JvmUtil.getJvmProperty("os.name").toLowerCase().contains("mac")) {
                System.setProperty("webdriver.chrome.driver", extractChromeDriver(Platform.MAC));
            } else if (JvmUtil.getJvmProperty("os.name").toLowerCase().contains("nix") ||
                    JvmUtil.getJvmProperty("os.name").toLowerCase().contains("nux") ||
                    JvmUtil.getJvmProperty("os.name").toLowerCase().contains("aix")
            ) {
                System.setProperty("webdriver.chrome.driver", extractChromeDriver(Platform.LINUX));
            } else if (JvmUtil.getJvmProperty("os.name").toLowerCase().contains("win")) {
                System.setProperty("webdriver.chrome.driver", extractChromeDriver(Platform.WINDOWS));
                System.setProperty("webdriver.ie.driver", extractIEDriver(Platform.WINDOWS));
            } else {

            }
        } catch (Exception e) {
            logFatal("Could not load webdriver");
            e.printStackTrace();
        }

        /**
         * Order of overrides:
         * <ol>
         *     <li>Test</li>
         *     <li>JVM Arguments</li>
         *     <li>Default properties</li>
         * </ol>
         */
        final Config testConfiguration = getClass().getAnnotation(Config.class);

        configuration = new LocomotiveConfig(testConfiguration, props);

        Capabilities capabilities;

        baseUrl = configuration.url();

        log.debug(String.format("\n=== Configuration ===\n" +
        "\tURL:     %s\n" +
        "\tBrowser: %s\n" +
        "\tHub:     %s\n" +
        "\tBase url: %s\n", configuration.url(), configuration.browser().moniker, configuration.hub(), configuration.baseUrl()));

        boolean isLocal = StringUtils.isEmpty(configuration.hub());

        switch (configuration.browser()) {
            case CHROME:
                capabilities = DesiredCapabilities.chrome();
                if (isLocal) try {
                    driver = new ChromeDriver(capabilities);
                } catch (Exception x) {
                    logFatal("Also see https://github.com/conductor-framework/conductor/wiki/WebDriver-Executables");
                    System.exit(1);
                }
                break;
            case FIREFOX:
                capabilities = DesiredCapabilities.firefox();
                if (isLocal) try {
                    driver = new FirefoxDriver(capabilities);
                } catch (Exception x) {
                    x.printStackTrace();
                    logFatal("Also see https://github.com/conductor-framework/conductor/wiki/WebDriver-Executables");
                    System.exit(1);
                }
                break;
            case INTERNET_EXPLORER:
                capabilities = DesiredCapabilities.internetExplorer();
                if (isLocal) try {
                    driver = new InternetExplorerDriver(capabilities);
                } catch (Exception x) {
                    x.printStackTrace();
                    logFatal("Also see https://github.com/conductor-framework/conductor/wiki/WebDriver-Executables");
                    System.exit(1);
                }
                break;
            case EDGE:
                capabilities = DesiredCapabilities.edge();
                if (isLocal) try {
                    driver = new EdgeDriver(capabilities);
                } catch (Exception x) {
                    x.printStackTrace();
                    logFatal("Also see https://github.com/conductor-framework/conductor/wiki/WebDriver-Executables");
                    System.exit(1);
                }
                break;
            case SAFARI:
                capabilities = DesiredCapabilities.safari();
                if (isLocal) try {
                    driver = new SafariDriver(capabilities);
                } catch (Exception x) {
                    x.printStackTrace();
                    logFatal("Also see https://github.com/conductor-framework/conductor/wiki/WebDriver-Executables");
                    System.exit(1);
                }
                break;
            case PHANTOMJS:
                capabilities = DesiredCapabilities.phantomjs();
                if (isLocal) try {
                    driver = new PhantomJSDriver(capabilities);
                } catch (Exception x) {
                    x.printStackTrace();
                    logFatal("Also see https://github.com/conductor-framework/conductor/wiki/WebDriver-Executables");
                    System.exit(1);
                }
                break;
            default:
                System.err.println("Unknown browser: " + configuration.browser());
                return;
        }

        if (!isLocal)
            // they are using a hub.
            try {
                driver = new RemoteWebDriver(new URL(configuration.hub()), capabilities); // just override the driver.
            } catch (Exception x) {
                logFatal("Couldn't connect to hub: " + configuration.hub());
                x.printStackTrace();
                return;
            }

        actions = new Actions(driver);

        if (StringUtils.isNotEmpty(baseUrl)) driver.navigate().to(baseUrl);
    }

    private String extractChromeDriver(Platform platform) throws IOException, RuntimeException {
        return extractDriver(platform, "chrome");
    }

    private String extractIEDriver(Platform platform) throws IOException, RuntimeException {
        return extractDriver(platform, "ie");
    }

    private String extractDriver(Platform platform, String browser) throws IOException, RuntimeException {
        URL rsrcDriver;
        if (platform == Platform.LINUX) {
            rsrcDriver = getClass().getResource("/drivers/" + browser + "driver-linux-32bit");
        } else if (platform == Platform.MAC) {
            rsrcDriver = getClass().getResource("/drivers/" + browser + "driver-mac-32bit");
        } else if (platform == Platform.WINDOWS) {
            rsrcDriver = getClass().getResource("/drivers/" + browser + "driver-windows-32bit.exe");
        } else {
            throw new RuntimeException("Unknown platform");
        }

        File tempFile = File.createTempFile("conductor-" + browser + "driver-", "");
        tempFile.deleteOnExit();

        FileUtils.copyURLToFile(rsrcDriver, tempFile);
        tempFile.setExecutable(true);

        System.out.println(tempFile.getAbsolutePath());

        return tempFile.getAbsolutePath();
    }

    static public String findFile(String filename) {
        String paths[] = {"", "bin/", "target/classes"}; // if you have chromedriver somewhere else on the path, then put it here.
        for (String path : paths) {
            if (new File(path + filename).exists())
                return path + filename;
        }
        return "";
    }

    @After
    public void teardown() {
        driver.quit();
    }

    /**
     * Method that acts as an arbiter of implicit timeouts of sorts.. sort of like a Wait For Ajax method.
     */
    public WebElement waitForElement(By by) {
        int attempts = 0;
        int size = driver.findElements(by).size();

        while (size == 0) {
            size = driver.findElements(by).size();
            if (attempts == MAX_ATTEMPTS) fail(String.format("Could not find %s after %d seconds",
                                                             by.toString(),
                                                             MAX_ATTEMPTS));
            attempts++;
            try {
                Thread.sleep(1000); // sleep for 1 second.
            } catch (Exception x) {
                fail("Failed due to an exception during Thread.sleep!");
                x.printStackTrace();
            }
        }

        if (size > 1) System.err.println("WARN: There are more than 1 " + by.toString() + " 's!");

        return driver.findElement(by);
    }

    /**
     * Wait for a specific condition (polling every 1s, for MAX_TIMOUT seconds)
     * @param condition the condition to wait for
     * @return The implementing class for fluency
     */
    public Locomotive waitForCondition(ExpectedCondition<?> condition) {
        return waitForCondition(condition, MAX_TIMEOUT);
    }

    /**
     * Wait for a specific condition (polling every 1s)
     * @param condition the condition to wait for
     * @param timeOutInSeconds the timeout in seconds
     * @return The implementing class for fluency
     */
    public Locomotive waitForCondition(ExpectedCondition<?> condition, long timeOutInSeconds) {
        return waitForCondition(condition, timeOutInSeconds, 1000); // poll every second
    }

    public Locomotive waitForCondition(ExpectedCondition<?> condition, long timeOutInSeconds, long sleepInMillis) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds, sleepInMillis);
        wait.until(condition);
        return this;
    }

    public Locomotive click(String css) {
        return click(By.cssSelector(css));
    }

    public Locomotive click(By by) {
        waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)))
        .waitForCondition(ExpectedConditions.elementToBeClickable(by));
        waitForElement(by).click();
        return this;
    }

    public Locomotive setText(String css, String text) {
        return setText(By.cssSelector(css), text);
    }

    public Locomotive setText(By by, String text) {
        waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)))
        .waitForCondition(ExpectedConditions.elementToBeClickable(by));
        WebElement element = waitForElement(by);
        element.clear();
        element.sendKeys(text);
        return this;
    }

    public Locomotive hoverOver(String css) {
        return hoverOver(By.cssSelector(css));
    }

    public Locomotive hoverOver(By by) {
        actions.moveToElement(driver.findElement(by)).perform();
        return this;
    }

    public boolean isChecked(String css) {
        return isChecked(By.cssSelector(css));
    }

    public boolean isChecked(By by) {
        return waitForElement(by).isSelected();
    }

    public boolean isPresent(String css) {
        return isPresent(By.cssSelector(css));
    }

    public boolean isPresent(By by) {
        return driver.findElements(by).size() > 0;
    }

    public String getText(String css) {
        return getText(By.cssSelector(css));
    }

    public String getText(By by) {
        String text;
        WebElement e = waitForElement(by);

        if (e.getTagName().equalsIgnoreCase("input") || e.getTagName().equalsIgnoreCase("select") || e.getTagName().equalsIgnoreCase("textarea"))
            text = e.getAttribute("value");
        else
            text = e.getText();

        return text;
    }

    public String getAttribute(String css, String attribute) {
        return getAttribute(By.cssSelector(css), attribute);
    }

    public String getAttribute(By by, String attribute) {
        return waitForElement(by).getAttribute(attribute);
    }

    public Locomotive check(String css) {
        return check(By.cssSelector(css));
    }

    public Locomotive check(By by) {
        if (!isChecked(by)) {
            waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)))
            .waitForCondition(ExpectedConditions.elementToBeClickable(by));
            waitForElement(by).click();
            assertTrue(by.toString() + " did not check!", isChecked(by));
        }
        return this;
    }

    public Locomotive uncheck(String css) {
        return uncheck(By.cssSelector(css));
    }

    public Locomotive uncheck(By by) {
        if (isChecked(by)) {
            waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)))
            .waitForCondition(ExpectedConditions.elementToBeClickable(by));
            waitForElement(by).click();
            assertFalse(by.toString() + " did not uncheck!", isChecked(by));
        }
        return this;
    }

    public Locomotive selectOptionByText(String css, String text) {
        return selectOptionByText(By.cssSelector(css), text);
    }

    public Locomotive selectOptionByText(By by, String text) {
        Select box = new Select(waitForElement(by));
        waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)))
        .waitForCondition(ExpectedConditions.elementToBeClickable(by));
        box.selectByVisibleText(text);
        return this;
    }

    public Locomotive selectOptionByValue(String css, String value) {
        return selectOptionByValue(By.cssSelector(css), value);
    }

    public Locomotive selectOptionByValue(By by, String value) {
        Select box = new Select(waitForElement(by));
        waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)))
        .waitForCondition(ExpectedConditions.elementToBeClickable(by));
        box.selectByValue(value);
        return this;
    }

    @Override
    public Locomotive selectOptionByIndex(String css, Integer i) {
        return selectOptionByIndex(By.cssSelector(css), i);
    }

    @Override
    public Locomotive selectOptionByIndex(By by, Integer i) {
        Select box = new Select(waitForElement(by));
        waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)))
        .waitForCondition(ExpectedConditions.elementToBeClickable(by));
        box.selectByIndex(i);
        return this;
    }

    /* Window / Frame Switching */

    public Locomotive waitForWindow(String regex) {
        Set<String> windows = driver.getWindowHandles();

        for (String window : windows) {
            try {
                driver.switchTo().window(window);

                p = Pattern.compile(regex);
                m = p.matcher(driver.getCurrentUrl());

                if (m.find()) {
                    attempts = 0;
                    return switchToWindow(regex);
                }
                else {
                    // try for title
                    m = p.matcher(driver.getTitle());

                    if (m.find()) {
                        attempts = 0;
                        return switchToWindow(regex);
                    }
                }
            } catch(NoSuchWindowException e) {
                if (attempts <= MAX_ATTEMPTS) {
                    attempts++;

                    try {Thread.sleep(1000);}catch(Exception x) { x.printStackTrace(); }

                    return waitForWindow(regex);
                } else {
                    fail("Window with url|title: " + regex + " did not appear after " + MAX_ATTEMPTS + " tries. Exiting.");
                }
            }
        }

        // when we reach this point, that means no window exists with that title..
        if (attempts == MAX_ATTEMPTS) {
            fail("Window with title: " + regex + " did not appear after " + MAX_ATTEMPTS + " tries. Exiting.");
            return this;
        } else {
            System.out.println("#waitForWindow() : Window doesn't exist yet. [" + regex + "] Trying again. " + (attempts+1) + "/" + MAX_ATTEMPTS);
            attempts++;
            try {Thread.sleep(1000);}catch(Exception x) { x.printStackTrace(); }
            return waitForWindow(regex);
        }
    }

    public Locomotive switchToWindow(String regex) {
        Set<String> windows = driver.getWindowHandles();

        for (String window : windows) {
            driver.switchTo().window(window);
            System.out.println(String.format("#switchToWindow() : title=%s ; url=%s",
                    driver.getTitle(),
                    driver.getCurrentUrl()));

            p = Pattern.compile(regex);
            m = p.matcher(driver.getTitle());

            if (m.find()) return this;
            else {
                m = p.matcher(driver.getCurrentUrl());
                if (m.find()) return this;
            }
        }

        fail("Could not switch to window with title / url: " + regex);
        return this;
    }

    public Locomotive closeWindow(String regex) {
        if (regex == null) {
            driver.close();

            if (driver.getWindowHandles().size() == 1)
                driver.switchTo().window(driver.getWindowHandles().iterator().next());

            return this;
        }

        Set<String> windows = driver.getWindowHandles();

        for (String window : windows) {
            try {
                driver.switchTo().window(window);

                p = Pattern.compile(regex);
                m = p.matcher(driver.getTitle());

                if (m.find()) {
                    switchToWindow(regex); // switch to the window, then close it.
                    driver.close();

                    if (windows.size() == 2) // just default back to the first window.
                        driver.switchTo().window(windows.iterator().next());
                } else {
                    m = p.matcher(driver.getCurrentUrl());
                    if (m.find()) {
                        switchToWindow(regex);
                        driver.close();

                        if (windows.size() == 2) driver.switchTo().window(windows.iterator().next());
                    }
                }

            } catch(NoSuchWindowException e) {
                fail("Cannot close a window that doesn't exist. ["+regex+"]");
            }
        }
        return this;
    }

    public Locomotive closeWindow() {
        return closeWindow(null);
    }

    public Locomotive switchToFrame(String idOrName) {
        try {
            driver.switchTo().frame(idOrName);
        } catch (Exception x) {
            fail("Couldn't switch to frame with id or name [" + idOrName + "]");
        }
        return this;
    }

    @Override
    public Locomotive switchToFrame(WebElement webElement) {
        try {
            driver.switchTo().frame(webElement);
        } catch (Exception x) {
            fail("Couldn't switch to frame with WebElement [" + webElement + "]");
        }
        return this;
    }

    public Locomotive switchToFrame(int index) {
        try {
            driver.switchTo().frame(index);
        } catch (Exception x) {
            fail("Couldn't switch to frame with an index of [" + index + "]");
        }
        return this;
    }

    public Locomotive switchToDefaultContent() {
        driver.switchTo().defaultContent();
        return this;
    }

    /* ************************ */

    /* Validation Functions for Testing */

    public Locomotive validatePresent(String css) {
        return validatePresent(By.cssSelector(css));
    }

    public Locomotive validatePresent(By by) {
        waitForElement(by);
        assertTrue("Element " + by.toString() + " does not exist!",
                isPresent(by));
        return this;
    }

    public Locomotive validateNotPresent(String css) {
        return validateNotPresent(By.cssSelector(css));
    }

    public Locomotive validateNotPresent(By by) {
        assertFalse("Element " + by.toString() + " exists!", isPresent(by));
        return this;
    }

    public Locomotive validateText(String css, String text) {
        return validateText(By.cssSelector(css), text);
    }

    public Locomotive validateText(By by, String text) {
        String actual = getText(by);

        assertTrue(String.format("Text does not match! [expected: %s] [actual: %s]", text, actual), text.equals(actual));
        return this;
    }

    @Deprecated
    public Locomotive setAndValidateText(By by, String text) {
        return setText(by, text).validateText(by, text);
    }

    @Deprecated
    public Locomotive setAndValidateText(String css, String text) {
        return setText(css, text).validateText(css, text);
    }

    public Locomotive validateTextNot(String css, String text) {
        return validateTextNot(By.cssSelector(css), text);
    }

    public Locomotive validateTextNot(By by, String text) {
        String actual = getText(by);

        assertFalse(String.format("Text matches! [expected: %s] [actual: %s]", text, actual), text.equals(actual));
        return this;
    }

    public Locomotive validateTextPresent(String text) {
        assertTrue(driver.getPageSource().contains(text));
        return this;
    }

    public Locomotive validateTextNotPresent(String text) {
        assertFalse(driver.getPageSource().contains(text));
        return this;
    }

    public Locomotive validateChecked(String css) {
        return validateChecked(By.cssSelector(css));
    }

    public Locomotive validateChecked(By by) {
        assertTrue(by.toString() + " is not checked!", isChecked(by));
        return this;
    }

    public Locomotive validateUnchecked(String css) {
        return validateUnchecked(By.cssSelector(css));
    }

    public Locomotive validateUnchecked(By by) {
        assertFalse(by.toString() + " is not unchecked!", isChecked(by));
        return this;
    }

    public Locomotive validateAttribute(String css, String attr, String regex) {
        return validateAttribute(By.cssSelector(css), attr, regex);
    }

    public Locomotive validateAttribute(By by, String attr, String regex) {
        String actual = null;
        try {
            actual = getAttribute(by, attr);
            if (actual.equals(regex)) return this; // test passes.
        } catch (NoSuchElementException e) {
            fail("No such element [" + by.toString() + "] exists.");
        } catch (Exception x) {
            fail("Cannot validate an attribute if an element doesn't have it!");
        }

        p = Pattern.compile(regex);
        m = p.matcher(actual);

        assertTrue(String.format("Attribute doesn't match! [Selector: %s] [Attribute: %s] [Desired value: %s] [Actual value: %s]",
                by.toString(),
                attr,
                regex,
                actual
                ), m.find());

        return this;
    }

    public Locomotive validateUrl(String regex) {
        p = Pattern.compile(regex);
        m = p.matcher(driver.getCurrentUrl());

        assertTrue("Url does not match regex [" + regex + "] (actual is: \""+driver.getCurrentUrl()+"\")", m.find());
        return this;
    }

    public Locomotive validateTrue(boolean condition) {
        assertTrue(condition);
        return this;
    }

    public Locomotive validateFalse(boolean condition) {
        assertFalse(condition);
        return this;
    }

    /* ================================ */

    public Locomotive goBack() {
        driver.navigate().back();
        return this;
    }

    @Override
    public Locomotive refresh() {
        driver.navigate().refresh();
        return this;
    }

    public Locomotive navigateTo(String url) {
        // absolute url
        if (url.contains("://"))      driver.navigate().to(url);
        else if (url.startsWith("/")) driver.navigate().to(baseUrl.concat(url));
        else                          driver.navigate().to(driver.getCurrentUrl().concat(url));

        return this;
    }

    public Locomotive store(String key, String value) {
        vars.put(key, value);
        return this;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String defaultValue) {
        if (Strings.isNullOrEmpty(vars.get(key))) {
            return defaultValue;
        } else
            return vars.get(key);
    }

    public Locomotive log(Object object) {
        return logInfo(object);
    }

    public Locomotive logInfo(Object object) {
        log.info(object);
        return this;
    }

    public Locomotive logWarn(Object object) {
        log.warn(object);
        return this;
    }

    public Locomotive logError(Object object) {
        log.error(object);
        return this;
    }

    public Locomotive logDebug(Object object) {
        log.debug(object);
        return this;
    }

    public Locomotive logFatal(Object object) {
        log.fatal(object);
        return this;
    }
}
