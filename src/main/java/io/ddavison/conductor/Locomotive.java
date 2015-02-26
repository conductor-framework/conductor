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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * the base test that includes all Selenium 2 functionality that you will need
 * to get you rolling.
 * @author ddavison
 *
 */
public class Locomotive {

    public static final Logger log = LogManager.getLogger(Locomotive.class);

    public WebDriver driver;

    // max seconds before failing a script.
    private final int MAX_ATTEMPTS = 5;

    private int attempts = 0;

    public Actions actions;

    private Map<String, String> vars = new HashMap<String, String>();

    /**
     * The url that an automated test will be testing.
     */
    private String baseUrl;

    private Pattern p;
    private Matcher m;

    public Locomotive() {
        Config configuration = getClass().getAnnotation(Config.class);

        Capabilities capabilities;

        baseUrl = configuration.url();

        boolean isLocal = StringUtils.isEmpty(configuration.hub());

        switch (configuration.browser()) {
            case CHROME:
                capabilities = DesiredCapabilities.chrome();
                if (isLocal) try {
                    driver = new ChromeDriver(capabilities);
                } catch (Exception x) {
                    logFatal("chromedriver not found. See https://github.com/ddavison/getting-started-with-selenium-framework/wiki/WebDriver-Executables for more information.");
                    System.exit(1);
                }
                break;
            case FIREFOX:
                capabilities = DesiredCapabilities.firefox();
                if (isLocal) driver = new FirefoxDriver(capabilities);
                break;
            case INTERNET_EXPLORER:
                logFatal("iedriver not found. See https://github.com/ddavison/getting-started-with-selenium-framework/wiki/WebDriver-Executables for more information.");
                System.exit(1);
                capabilities = DesiredCapabilities.internetExplorer();
                if (isLocal) driver = new InternetExplorerDriver(capabilities);
                break;
            case SAFARI:
                logFatal("safaridriver not found. See https://github.com/ddavison/getting-started-with-selenium-framework/wiki/WebDriver-Executables for more information.");
                System.exit(1);
                capabilities = DesiredCapabilities.safari();
                if (isLocal) driver = new SafariDriver(capabilities);
                break;
            case HTMLUNIT: // If you are designing a regression system, HtmlUnit is NOT recommended.
                capabilities = DesiredCapabilities.htmlUnitWithJs();
                if (isLocal) driver = new HtmlUnitDriver(capabilities);
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
                System.err.println("Couldn't connect to hub: " + configuration.hub());
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

    /**
     * Click an element.
     * @param css The css element to click.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive click(String css) {
        return click(By.cssSelector(css));
    }

    /**
     * Click an element.
     * @param by The element to click.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive click(By by) {
        waitForElement(by).click();
        return this;
    }

    /**
     * Clears the text from a text field, and sets it.
     * @param css The css element to set the text of.
     * @param text The text that the element will have.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive setText(String css, String text) {
        return setText(By.cssSelector(css), text);
    }

    /**
     * Clears the text from a text field, and sets it.
     * @param by The element to set the text of.
     * @param text The text that the element will have.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive setText(By by, String text) {
        WebElement element = waitForElement(by);
        element.clear();
        element.sendKeys(text);
        return this;
    }

    /**
     * Hover over an element.
     * @param css The css element to hover over.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive hoverOver(String css) {
        return hoverOver(By.cssSelector(css));
    }

    /**
     * Hover over an element.
     * @param by The element to hover over.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive hoverOver(By by) {
        actions.moveToElement(driver.findElement(by)).perform();
        return this;
    }

    /**
     * Checks if the element is checked or not.
     * @param css The css selector for the checkbox
     * @return <i>this method is not meant to be used fluently.</i><br><br>
     * Returns <code>true</code> if the element is checked. and <code>false</code> if it's not.
     */
    public boolean isChecked(String css) {
        return isChecked(By.cssSelector(css));
    }

    /**
     * Checks if the element is checked or not.
     * @param by The element
     * @return <i>this method is not meant to be used fluently.</i><br><br>
     * Returns <code>true</code> if the element is checked. and <code>false</code> if it's not.
     */
    public boolean isChecked(By by) {
        return waitForElement(by).isSelected();
    }

    /**
     * Checks if the element is present or not.<br>
     * @param css The css selector for the element
     * @return <i>this method is not meant to be used fluently.</i><br><br>.
     * Returns <code>true</code> if the element is present. and <code>false</code> if it's not.
     */
    public boolean isPresent(String css) {
        return isPresent(By.cssSelector(css));
    }

    /**
     * Checks if the element is present or not.<br>
     * @param by The element
     * @return <i>this method is not meant to be used fluently.</i><br><br>.
     * Returns <code>true</code> if the element is present. and <code>false</code> if it's not.
     */
    public boolean isPresent(By by) {
        return driver.findElements(by).size() > 0;
    }

    /**
     * Get the text of an element.
     * <blockquote>This is a consolidated method that works on input's, as select boxes, and fetches the value rather than the innerHTMl.</blockquote>
     * @param css The css selector of the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public String getText(String css) {
        return getText(By.cssSelector(css));
    }

    /**
     * Get the text of an element.
     * <blockquote>This is a consolidated method that works on input's, as select boxes, and fetches the value rather than the innerHTMl.</blockquote>
     * @param by The element to get the text from
     * @return <code>AutomationTest</code> (for fluency)
     */
    public String getText(By by) {
        String text;
        WebElement e = waitForElement(by);
        
        if (e.getTagName().equalsIgnoreCase("input") || e.getTagName().equalsIgnoreCase("select"))
            text = e.getAttribute("value");
        else
            text = e.getText();
        
        return text;
    }

    /**
     * Get an attribute of an element
     * @param css The css element to get from
     * @param attribute The attribute to get
     * @return <code>AutomationTest</code> (for fluency)
     */
    public String getAttribute(String css, String attribute) {
        return getAttribute(By.cssSelector(css), attribute);
    }

    /**
     * Get an attribute of an element
     * @param by The element to get from
     * @param attribute The attribute to get
     * @return <code>AutomationTest</code> (for fluency)
     */
    public String getAttribute(By by, String attribute) {
        return waitForElement(by).getAttribute(attribute);
    }

    /**
     * Check a checkbox, or radio button
     * @param css The css element to check
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive check(String css) {
        return check(By.cssSelector(css));
    }

    /**
     * Check a checkbox, or radio button
     * @param by The element to check
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive check(By by) {
        if (!isChecked(by)) {
            waitForElement(by).click();
            assertTrue(by.toString() + " did not check!", isChecked(by));
        }
        return this;
    }

    /**
     * Uncheck a checkbox, or radio button
     * @param css The css element to uncheck
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive uncheck(String css) {
        return uncheck(By.cssSelector(css));
    }

    /**
     * Uncheck a checkbox, or radio button.
     * @param by The element to uncheck.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive uncheck(By by) {
        if (isChecked(by)) {
            waitForElement(by).click();
            assertFalse(by.toString() + " did not uncheck!", isChecked(by));
        }
        return this;
    }

    /**
     * Selects an option from a dropdown ({@literal <select> tag}) based on the text displayed.
     * @param css The css selector for the element
     * @param text The text that is displaying.
     * @see #selectOptionByValue(By, String)
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive selectOptionByText(String css, String text) {
        return selectOptionByText(By.cssSelector(css), text);
    }

    /**
     * Selects an option from a dropdown ({@literal <select> tag}) based on the text displayed.
     * @param by The element
     * @param text The text that is displaying.
     * @see #selectOptionByValue(By, String)
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive selectOptionByText(By by, String text) {
        Select box = new Select(waitForElement(by));
        box.selectByVisibleText(text);
        return this;
    }

    /**
     * Selects an option from a dropdown ({@literal <select> tag}) based on the value.
     * @param css The css selector for the element
     * @param value The <code>value</code> attribute of the option.
     * @see #selectOptionByText(By, String)
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive selectOptionByValue(String css, String value) {
        return selectOptionByValue(By.cssSelector(css), value);
    }

    /**
     * Selects an option from a dropdown ({@literal <select> tag}) based on the value.
     * @param by The element
     * @param value The <code>value</code> attribute of the option.
     * @see #selectOptionByText(By, String)
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive selectOptionByValue(By by, String value) {
        Select box = new Select(waitForElement(by));
        box.selectByValue(value);
        return this;
    }
    
    /* Window / Frame Switching */
    
    /**
     * Waits for a window to appear, then switches to it.
     * @param regex Regex enabled. Url of the window, or title.
     * @return <code>AutomationTest</code> (for fluency)
     */
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
    
    /**
     * Switch's to a window that is already in existance.
     * @param regex Regex enabled. Url of the window, or title.
     * @return <code>AutomationTest</code> (for fluency)
     */
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
    
    /**
     * Close an open window.
     * <br>
     * If you have opened only 1 external window, then when you call this method, the context will switch back to the window you were using before.<br>
     * <br>
     * If you had more than 2 windows displaying, then you will need to call {@link #switchToWindow(String)} to switch back context.
     * @param regex The title of the window to close (regex enabled). You may specify <code>null</code> to close the active window. If you specify <code>null</code> then the context will switch back to the initial window.
     * @return <code>AutomationTest</code> (for fluency)
     */
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
    
    /**
     * Closes the current active window.  Calling this method will return the context back to the initial window.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive closeWindow() {
        return closeWindow(null);
    }
    
    /**
     * Switches to a frame or iframe.
     * @param idOrName The id or name of the frame.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive switchToFrame(String idOrName) {
        try {
            driver.switchTo().frame(idOrName);
        } catch (Exception x) {
            fail("Couldn't switch to frame with id or name [" + idOrName + "]");
        }
        return this;
    }

    /**
     * Switches to a frame based on the index it comes. (starting at <code>0</code>)
     * @param index The index of the frame in which it appears
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive switchToFrame(int index) {
        try {
            driver.switchTo().frame(index);
        } catch (Exception x) {
            fail("Couldn't switch to frame with an index of [" + index + "]");
        }
        return this;
    }

    /**
     * Switch back to the default content (the first window / frame that you were on before switching)
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive switchToDefaultContent() {
        driver.switchTo().defaultContent();
        return this;
    }
    
    /* ************************ */
    
    /* Validation Functions for Testing */

    /**
     * Validates that an element is present.
     * @param css The css selector of the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validatePresent(String css) {
        return validatePresent(By.cssSelector(css));
    }

    /**
     * Validates that an element is present.
     * @param by The css selector of the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validatePresent(By by) {
        waitForElement(by);
        assertTrue("Element " + by.toString() + " does not exist!",
                isPresent(by));
        return this;
    }

    /**
     * Validates that an element is not present.
     * @param css The css selector for the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateNotPresent(String css) {
        return validateNotPresent(By.cssSelector(css));
    }

    /**
     * Validates that an element is not present.
     * @param by The css selector of the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateNotPresent(By by) {
        assertFalse("Element " + by.toString() + " exists!", isPresent(by));
        return this;
    }

    /**
     * Validate that the text of an element is correct.
     * @param css The css element to validate the text of.
     * @param text The text to validate.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateText(String css, String text) {
        return validateText(By.cssSelector(css), text);
    }

    /**
     * Validate that the text of an element is correct.
     * @param by The element to validate the text of.
     * @param text The text to validate.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateText(By by, String text) {
        String actual = getText(by);
        
        assertTrue(String.format("Text does not match! [expected: %s] [actual: %s]", text, actual), text.equals(actual));
        return this;
    }

    /**
     * Validate that the text of an element is not matching text.
     * @param css The css element to validate the text of.
     * @param text The text to validate.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateTextNot(String css, String text) {
        return validateTextNot(By.cssSelector(css), text);
    }

    /**
     * Validate that the text of an element is not matching text.
     * @param by The element to validate the text of.
     * @param text The text to validate.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateTextNot(By by, String text) {
        String actual = getText(by);

        assertFalse(String.format("Text matches! [expected: %s] [actual: %s]", text, actual), text.equals(actual));
        return this;
    }

    /**
     * Validate that text is present somewhere on the page.
     * @param text The text to ensure is on the page.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateTextPresent(String text) {
        assertTrue(driver.getPageSource().contains(text));
        return this;
    }

    /**
     * Validate that some text is nowhere on the page.
     * @param text The text to ensure is not on the page.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateTextNotPresent(String text) {
        assertFalse(driver.getPageSource().contains(text));
        return this;
    }

    /**
     * Validate that a checkbox or a radio button is checked.
     * @param css The css selector for the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateChecked(String css) {
        return validateChecked(By.cssSelector(css));
    }

    /**
     * Validate that a checkbox or a radio button is checked.
     * @param by The css selector of the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateChecked(By by) {
        assertTrue(by.toString() + " is not checked!", isChecked(by));
        return this;
    }

    /**
     * Validate that a checkbox or a radio button is unchecked.
     * @param css The css selector for the element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateUnchecked(String css) {
        return validateUnchecked(By.cssSelector(css));
    }

    /**
     * Validate that a checkbox or a radio button is unchecked.
     * @param by The element
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateUnchecked(By by) {
        assertFalse(by.toString() + " is not unchecked!", isChecked(by));
        return this;
    }

    /**
     * Validates an attribute of an element.<br><br>
     * Example:<br>
     * <blockquote>
     * {@literal <input type="text" id="test" />}
     * <br><br>
     * <code>.validateAttribute("input#test", "type", "text") // validates that the "type" attribute equals "test"</code>
     * </blockquote>
     * @param css The css selector of the element
     * @param attr The attribute you'd like to validate
     * @param regex What the attribute <b>should</b> be.  (this method supports regex)
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateAttribute(String css, String attr, String regex) {
        return validateAttribute(By.cssSelector(css), attr, regex);
    }

    /**
     * Validates an attribute of an element.<br><br>
     * Example:<br>
     * <blockquote>
     * {@literal <input type="text" id="test" />}
     * <br><br>
     * <code>.validateAttribute(css("input#test"), "type", "text") // validates that the "type" attribute equals "test"</code>
     * </blockquote>
     * @param by The element
     * @param attr The attribute you'd like to validate
     * @param regex What the attribute <b>should</b> be.  (this method supports regex)
     * @return <code>AutomationTest</code> (for fluency)
     */
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

    /**
     * Validate the Url
     * @param regex Regular expression to match
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateUrl(String regex) {
        p = Pattern.compile(regex);
        m = p.matcher(driver.getCurrentUrl());
        
        assertTrue("Url does not match regex [" + regex + "] (actual is: \""+driver.getCurrentUrl()+"\")", m.find());
        return this;
    }

    /**
     * Validates that a specific condition is true
     * @param condition The condition that is expected to be true
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateTrue(boolean condition) {
        assertTrue(condition);
        return this;
    }

    /**
     * Validates that a specific condition is false
     * @param condition The condition that is expected to be false
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive validateFalse(boolean condition) {
        assertFalse(condition);
        return this;
    }
    
    /* ================================ */
    
    /**
     * Navigates the browser back one page.
     * Same as <code>driver.navigate().back()</code>
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive goBack() {
        driver.navigate().back();
        return this;
    }
    
    /**
     * Navigates to an absolute or relative Url.
     * @param url Use cases are:<br>
     * <blockquote>
     * <code>navigateTo("/login") // navigate to a relative url. slash meaning start fresh from the base url.</code><br><br>
     * <code>navigateTo("path") // navigate to a relative url. will simply append "path" to the current url.</code><br><br>
     * <code>navigateTo("http://google.com") // navigates to an absolute url.</code>
     * </blockquote>
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive navigateTo(String url) {
        // absolute url
        if (url.contains("://"))      driver.navigate().to(url);
        else if (url.startsWith("/")) driver.navigate().to(baseUrl.concat(url));
        else                          driver.navigate().to(driver.getCurrentUrl().concat(url));
        
        return this;
    }

    /**
     * Put a variable in the data warehouse.
     * @param key The key to put.
     * @param value The value to put.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive store(String key, String value) {
        vars.put(key, value);
        return this;
    }

    /**
     * Get a variable from the data warehouse.<br><br>
     * If the key is not set, then use {@link #get(String, String)}
     * @param key The key to fetch.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public String get(String key) {
        return get(key, null);
    }

    /**
     * Get a variable from the data warehouse.
     * @param key The key to fetch.
     * @param defaultValue The value to return if the variable is not set.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public String get(String key, String defaultValue) {
        if (Strings.isNullOrEmpty(vars.get(key))) {
            return defaultValue;
        } else
            return vars.get(key);
    }

    /**
     * Log something as information
     * @param object What to log.
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive log(Object object) {
        return logInfo(object);
    }

    /**
     * Log something as information
     * @param object What to log
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive logInfo(Object object) {
        log.info(object);
        return this;
    }

    /**
     * Log something as a warning
     * @param object What to log
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive logWarn(Object object) {
        log.warn(object);
        return this;
    }

    /**
     * Log something as an error
     * @param object What to log
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive logError(Object object) {
        log.error(object);
        return this;
    }

    /**
     * Log something as debug
     * @param object What to log
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive logDebug(Object object) {
        log.debug(object);
        return this;
    }

    /**
     * Log something as fatal
     * @param object What to log
     * @return <code>AutomationTest</code> (for fluency)
     */
    public Locomotive logFatal(Object object) {
        log.fatal(object);
        return this;
    }
}
