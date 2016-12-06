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
import io.ddavison.conductor.util.ScreenShotUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.swing.assertions.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the base test that includes all Selenium 2 functionality that you will need
 * to get you rolling.
 *
 * @author ddavison
 */
public class Locomotive implements Conductor<Locomotive> {

    public static final Logger log = LogManager.getLogger(Locomotive.class);

    /**
     * All test configuration in here
     */
    public LocomotiveConfig configuration;

    public WebDriver driver;

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
            props.load(getClass().getResourceAsStream("/default.properties"));
        } catch (Exception e) {
            logFatal("Couldn't load in default properties");
        }

        /*
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

        if (StringUtils.isNotEmpty(baseUrl)) {
            driver.navigate().to(baseUrl);
        }
    }

    static {
        // Set the webdriver env vars.
        if (JvmUtil.getJvmProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.mac"));
            System.setProperty("webdriver.gecko.driver", findFile("geckodriver.mac"));
        } else if (JvmUtil.getJvmProperty("os.name").toLowerCase().contains("nix") ||
                JvmUtil.getJvmProperty("os.name").toLowerCase().contains("nux") ||
                JvmUtil.getJvmProperty("os.name").toLowerCase().contains("aix")
                ) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.linux"));
        } else if (JvmUtil.getJvmProperty("os.name").toLowerCase().contains("win")) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.exe"));
            System.setProperty("webdriver.ie.driver", findFile("iedriver.exe"));
            System.setProperty("webdriver.gecko.driver", findFile("geckodriver.exe"));
        }
    }

    static public String findFile(String filename) {
        String paths[] = {"", "bin/", "target/classes"}; // if you have chromedriver somewhere else on the path, then put it here.
        for (String path : paths) {
            if (new File(path + filename).exists()) {
                return path + filename;
            }
        }
        return "";
    }

    @Rule
    public TestRule watchman = new TestWatcher() {
        boolean failure;
        Throwable e;
        Description description;


        @Override
        protected void failed(Throwable e, Description description) {
            if (configuration.screenshotsOnFail()) {
                failure = true;
                this.e = e;
                this.description = description;
            }
        }

        /**
         * Take screenshot if the test failed.
         */
        @Override
        protected void finished(Description description) {
            super.finished(description);
            if (configuration.screenshotsOnFail()) {
                if (failure) {
                    ScreenShotUtil.take(Locomotive.this,
                            description.getDisplayName(),
                            e.getMessage());
                }
                Locomotive.this.driver.quit();
            }
        }
    };

    @After
    public void teardown() {
        if (!configuration.screenshotsOnFail()) {
            driver.quit();
        }
    }

    public WebElement waitForElement(String css) {
        return waitForElement(By.cssSelector(css));
    }

    /**
     * Method that acts as an arbiter of implicit timeouts of sorts
     */
    public WebElement waitForElement(By by) {
        List<WebElement> elements = waitForElements(by);
        int size = elements.size();

        if (size == 0) {
            Assertions.fail(String.format("Could not find %s after %d attempts",
                    by.toString(),
                    configuration.retries()));
        } else {
            // If an element is found try to move to it.
            moveToElement(elements.get(0));
        }

        if (size > 1) {
            System.err.println("WARN: There are more than 1 " + by.toString() + " 's!");
        }

        return driver.findElement(by);
    }

    public List<WebElement> waitForElements(String css) {
        return waitForElements(By.cssSelector(css));
    }

    public List<WebElement> waitForElements(By by) {
        List<WebElement> elements = driver.findElements(by);

        if (elements.size() == 0) {
            int attempts = 1;
            while (attempts <= configuration.retries()) {
                try {
                    Thread.sleep(1000); // sleep for 1 second.
                } catch (Exception e) {
                    Assertions.fail("Failed due to an exception during Thread.sleep!", e);
                }

                elements = driver.findElements(by);
                if (elements.size() > 0) {
                    break;
                }
                attempts++;
            }
        }
        return elements;
    }

    public Locomotive moveToElement(String css) {
        return moveToElement(By.cssSelector(css));
    }

    public Locomotive moveToElement(By by) {
        return moveToElement(waitForElement(by));
    }

    public Locomotive moveToElement(WebElement element) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(element);
            actions.perform();
        } catch (WebDriverException e) {
            logError("UnsupportedCommandException: moveToElement | " + e.getMessage());
        }
        return this;
    }

    /**
     * Wait for a specific condition (polling every 1s, for MAX_TIMEOUT seconds)
     *
     * @param condition the condition to wait for
     * @return The implementing class for fluency
     */
    public Locomotive waitForCondition(ExpectedCondition<?> condition) {
        return waitForCondition(condition, configuration.timeout());
    }

    /**
     * Wait for a specific condition (polling every 1s)
     *
     * @param condition        the condition to wait for
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
        WebElement e = waitForElement(by);
        return e.getTagName().equalsIgnoreCase("input")
                || e.getTagName().equalsIgnoreCase("select")
                || e.getTagName().equalsIgnoreCase("textarea")
                ? e.getAttribute("value")
                : e.getText();
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
            Assertions.assertThat(isChecked(by)).isTrue();
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
            Assertions.assertThat(isChecked(by)).isFalse();
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
                } else {
                    // try for title
                    m = p.matcher(driver.getTitle());

                    if (m.find()) {
                        attempts = 0;
                        return switchToWindow(regex);
                    }
                }
            } catch (NoSuchWindowException e) {
                if (attempts <= configuration.retries()) {
                    attempts++;

                    try {
                        Thread.sleep(1000);
                    } catch (Exception x) {
                        x.printStackTrace();
                    }

                    return waitForWindow(regex);
                } else {
                    Assertions.fail("Window with url|title: " + regex + " did not appear after " + configuration.retries() + " tries. Exiting.", e);
                }
            }
        }

        // when we reach this point, that means no window exists with that title..
        if (attempts == configuration.retries()) {
            Assertions.fail("Window with title: " + regex + " did not appear after " + configuration.retries() + " tries. Exiting.");
            return this;
        } else {
            System.out.println("#waitForWindow() : Window doesn't exist yet. [" + regex + "] Trying again. " + (attempts + 1) + "/" + configuration.retries());
            attempts++;
            try {
                Thread.sleep(1000);
            } catch (Exception x) {
                x.printStackTrace();
            }
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

        Assertions.fail("Could not switch to window with title / url: " + regex);
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

            } catch (NoSuchWindowException e) {
                Assertions.fail("Cannot close a window that doesn't exist. [" + regex + "]");
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
            Assertions.fail("Couldn't switch to frame with id or name [" + idOrName + "]");
        }
        return this;
    }

    public Locomotive switchToFrame(WebElement webElement) {
        try {
            driver.switchTo().frame(webElement);
        } catch (Exception x) {
            Assertions.fail("Couldn't switch to frame with WebElement [" + webElement + "]");
        }
        return this;
    }

    public Locomotive switchToFrame(int index) {
        try {
            driver.switchTo().frame(index);
        } catch (Exception x) {
            Assertions.fail("Couldn't switch to frame with an index of [" + index + "]");
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
        Assertions.assertThat(isPresent(by)).isTrue();
        return this;
    }

    public Locomotive validateNotPresent(String css) {
        return validateNotPresent(By.cssSelector(css));
    }

    public Locomotive validateNotPresent(By by) {
        Assertions.assertThat(isPresent(by)).isFalse();
        return this;
    }

    public Locomotive validateText(String css, String text) {
        return validateText(By.cssSelector(css), text);
    }

    public Locomotive validateText(By by, String text) {
        Assertions.assertThat(text).isEqualTo(getText(by));
        return this;
    }

    public Locomotive validateTextIgnoreCase(String css, String text) {
        return validateTextIgnoreCase(By.cssSelector(css), text);
    }

    public Locomotive validateTextIgnoreCase(By by, String text) {
        Assertions.assertThat(text.toLowerCase()).isEqualTo(getText(by).toLowerCase());
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
        Assertions.assertThat(text).isNotEqualTo(getText(by));
        return this;
    }

    public Locomotive validateTextPresent(String text) {
        Assertions.assertThat(driver.getPageSource()).contains(text);
        return this;
    }

    public Locomotive validateTextNotPresent(String text) {
        Assertions.assertThat(driver.getPageSource()).doesNotContain(text);
        return this;
    }

    public Locomotive validateChecked(String css) {
        return validateChecked(By.cssSelector(css));
    }

    public Locomotive validateChecked(By by) {
        Assertions.assertThat(isChecked(by)).isTrue();
        return this;
    }

    public Locomotive validateUnchecked(String css) {
        return validateUnchecked(By.cssSelector(css));
    }

    public Locomotive validateUnchecked(By by) {
        Assertions.assertThat(isChecked(by)).isFalse();
        return this;
    }

    public Locomotive validateAttribute(String css, String attr, String regex) {
        return validateAttribute(By.cssSelector(css), attr, regex);
    }

    public Locomotive validateAttribute(By by, String attr, String regex) {
        return validateAttribute(waitForElement(by), attr, regex);
    }

    @Override
    public Locomotive validateAttribute(WebElement element, String attr, String regex) {
        String actual = null;
        try {
            actual = element.getAttribute(attr);
            if (actual.equals(regex)) {
                return this; // test passes
            }
        } catch (Exception e) {
            Assertions.fail(e.getMessage(), e);
        }

        p = Pattern.compile(regex);
        m = p.matcher(actual);

        Assertions.assertThat(m.find())
                .withFailMessage("Attribute doesn't match! [Attribute: %s] [Desired value: %s] [Actual value: %s] [Element: %s]",
                        attr,
                        regex,
                        actual,
                        element.toString())
                .isTrue();
        return this;
    }

    public Locomotive validateUrl(String regex) {
        p = Pattern.compile(regex);
        m = p.matcher(driver.getCurrentUrl());

        Assertions.assertThat(m.find())
                .withFailMessage("Url does not match regex [%s] (actual is: \"%s\")",
                        regex,
                        driver.getCurrentUrl())
                .isTrue();
        return this;
    }

    public Locomotive validateTrue(boolean condition) {
        Assertions.assertThat(condition).isTrue();
        return this;
    }

    public Locomotive validateFalse(boolean condition) {
        Assertions.assertThat(condition).isFalse();
        return this;
    }

    /* ================================ */

    public Locomotive goBack() {
        driver.navigate().back();
        return this;
    }

    public Locomotive refresh() {
        driver.navigate().refresh();
        return this;
    }

    public Locomotive navigateTo(String url) {
        // absolute url
        if (url.contains("://")) {
            driver.navigate().to(url);
        } else if (url.startsWith("/")) {
            driver.navigate().to(baseUrl.concat(url));
        } else {
            driver.navigate().to(driver.getCurrentUrl().concat(url));
        }

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
        }
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
