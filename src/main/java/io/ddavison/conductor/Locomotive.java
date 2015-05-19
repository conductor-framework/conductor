/*
 * Copyright 2014 Daniel Davison (http://github.com/ddavison)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package io.ddavison.conductor;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

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
    private final int MAX_ATTEMPTS = 5;

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
        log.debug(System.getenv(""));
        final Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/default.properties"));
        } catch (IOException e) {
            logFatal("Couldn't load in default properties");
        }

        /**
         * Order of overrides:
         * <ul>
         *     <li>Test</li>
         *     <li>JVM Arguments</li>
         *     <li>Default properties</li>
         * </ul>
         */
        final Config testConfiguration = getClass().getAnnotation(Config.class);

        configuration = new Config() {
            @Override
            public String url() {
                String url = "";
                if (!StringUtils.isEmpty(System.getenv("CONDUCTOR_URL"))) url = System.getenv("CONDUCTOR_URL");
                if (!StringUtils.isEmpty(props.getProperty("url"))) url = props.getProperty("url");
                if (testConfiguration != null && (!StringUtils.isEmpty(testConfiguration.url()))) url = testConfiguration.url();
                return url;
            }

            @Override
            public Browser browser() {
                Browser browser = Browser.NONE;
                if (!StringUtils.isEmpty(System.getenv("CONDUCTOR_BROWSER")))
                    browser = Browser.valueOf(System.getenv("CONDUCTOR_BROWSER").toUpperCase());
                if (testConfiguration != null && testConfiguration.browser() != Browser.NONE) return testConfiguration.browser();
                if (!StringUtils.isEmpty(props.getProperty("browser")))
                    browser = Browser.valueOf(props.getProperty("browser").toUpperCase());
                return browser;
            }

            @Override
            public String hub() {
                String hub = "";
                if (!StringUtils.isEmpty(System.getenv("CONDUCTOR_HUB"))) hub = System.getenv("CONDUCTOR_HUB");
                if (!StringUtils.isEmpty(props.getProperty("hub"))) hub = props.getProperty("hub");
                if (testConfiguration != null && (!StringUtils.isEmpty(testConfiguration.hub()))) hub = testConfiguration.hub();
                return hub;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        };

        Capabilities capabilities;

        baseUrl = configuration.url();

        log.debug(String.format("\n=== Configuration ===\n" +
        "\tURL:     %s\n" +
        "\tBrowser: %s\n" +
        "\tHub:     %s\n", configuration.url(), configuration.browser().moniker, configuration.hub()));

        boolean isLocal = StringUtils.isEmpty(configuration.hub());

        switch (configuration.browser()) {
            case CHROME:
                capabilities = DesiredCapabilities.chrome();
                if (isLocal) try {
                    driver = new ChromeDriver(capabilities);
                } catch (Exception x) {
                    logFatal("chromedriver not found. See https://github.com/ddavison/conductor/wiki/WebDriver-Executables for more information.");
                    System.exit(1);
                }
                break;
            case FIREFOX:
                capabilities = DesiredCapabilities.firefox();
                if (isLocal) driver = new FirefoxDriver(capabilities);
                break;
            case INTERNET_EXPLORER:
                logFatal("iedriver not found. See https://github.com/ddavison/conductor/wiki/WebDriver-Executables for more information.");
                System.exit(1);
                capabilities = DesiredCapabilities.internetExplorer();
                if (isLocal) driver = new InternetExplorerDriver(capabilities);
                break;
            case SAFARI:
                logFatal("safaridriver not found. See https://github.com/ddavison/conductor/wiki/WebDriver-Executables for more information.");
                System.exit(1);
                capabilities = DesiredCapabilities.safari();
                if (isLocal) driver = new SafariDriver(capabilities);
                break;
            case HTMLUNIT: // If you are designing a regression system, HtmlUnit is NOT recommended.
                capabilities = DesiredCapabilities.htmlUnitWithJs();
                if (isLocal) driver = new HtmlUnitDriver(capabilities);
                break;
            case PHANTOMJS:
                capabilities = DesiredCapabilities.phantomjs();
                if (isLocal)
                    try {
                        driver = new PhantomJSDriver(capabilities);
                    } catch (Exception x) {
                        logFatal("phantomjs not found. Download them from https://bitbucket.org/ariya/phantomjs/downloads/ and extract the binary as phantomjs.exe, phantomjs.linux, or phantomjs.mac at project root for Windows, Linux, or MacOS.");
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
        driver.navigate().to(baseUrl);
    }

    static {
        // Set the webdriver env vars.
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.mac"));
            System.setProperty("webdriver.firefox.driver", "");
        } else if (System.getProperty("os.name").toLowerCase().contains("nix") ||
                   System.getProperty("os.name").toLowerCase().contains("nux") ||
                   System.getProperty("os.name").toLowerCase().contains("aix")
        ) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.linux"));
            System.setProperty("webdriver.firefox.driver", "");
        } else if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.exe"));
            System.setProperty("webdriver.ie.driver", findFile("iedriver.exe"));
            System.setProperty("webdriver.firefox.driver", "");
        } else {

        }
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
     * Private method that acts as an arbiter of implicit timeouts of sorts.. sort of like a Wait For Ajax method.
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

    public Locomotive click(String css) {
        return click(By.cssSelector(css));
    }

    public Locomotive click(By by) {
        waitForElement(by).click();
        return this;
    }

    public Locomotive setText(String css, String text) {
        return setText(By.cssSelector(css), text);
    }

    public Locomotive setText(By by, String text) {
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
        
        if (e.getTagName().equalsIgnoreCase("input") || e.getTagName().equalsIgnoreCase("select"))
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
        box.selectByVisibleText(text);
        return this;
    }

    public Locomotive selectOptionByValue(String css, String value) {
        return selectOptionByValue(By.cssSelector(css), value);
    }

    public Locomotive selectOptionByValue(By by, String value) {
        Select box = new Select(waitForElement(by));
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
            fail("Window with title: " + regex + " did not appear after 5 tries. Exiting.");
            return this;
        } else {
            System.out.println("#waitForWindow() : Window doesn't exist yet. [" + regex + "] Trying again. " + attempts + "/" + MAX_ATTEMPTS);
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

    public Locomotive setAndValidateText(By by, String text) {
        return setText(by, text).validateText(by, text);
    }

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
            actual = driver.findElement(by).getAttribute(attr);
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
