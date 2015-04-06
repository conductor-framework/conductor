/*
 * Copyright 2014 Daniel Davison (http://github.com/ddavison)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package io.ddavison.conductor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author ddavison
 * @since v2
 * @since Apr 03, 2015
 */
public interface Conductor<Test> {
    WebElement waitForElement(By by);

    /*
        Actions
     */
    /**
     * Click an element.
     * @param css/by The element to click.
     * @return The implementing class for fluency
     */
    Test click(String css);
    Test click(By by);

    /**
     * Clears the text from a text field, and sets it.
     * @param css/by The element to set the text of.
     * @param text The text that the element will have.
     * @return The implementing class for fluency
     */
    Test setText(String css, String text);
    Test setText(By by, String text);

    /**
     * Hover over an element.
     * @param css/by The element to hover over.
     * @return The implementing class for fluency
     */
    Test hoverOver(String css);
    Test hoverOver(By by);

    /**
     * Checks if the element is checked or not.
     * @param css/by The checkbox
     * @return <i>this method is not meant to be used fluently.</i><br><br>
     * Returns <code>true</code> if the element is checked. and <code>false</code> if it's not.
     */
    boolean isChecked(String css);
    boolean isChecked(By by);

    /**
     * Checks if the element is present or not.<br>
     * @param css/by The element
     * @return <i>this method is not meant to be used fluently.</i><br><br>.
     * Returns <code>true</code> if the element is present. and <code>false</code> if it's not.
     */
    boolean isPresent(String css);
    boolean isPresent(By by);

    /**
     * Get the text of an element.
     * <blockquote>This is a consolidated method that works on input's, as select boxes, and fetches the value rather than the innerHTMl.</blockquote>
     * @param css/by The element
     * @return The implementing class for fluency
     */
    String getText(String css);
    String getText(By by);

    /**
     * Get an attribute of an element
     * @param css/by The element to get the attribute from
     * @param attribute The attribute to get
     * @return The implementing class for fluency
     */
    String getAttribute(String css, String attribute);
    String getAttribute(By by, String attribute);

    /**
     * Check a checkbox, or radio button
     * @param css/by The element to check
     * @return The implementing class for fluency
     */
    Test check(String css);
    Test check(By by);

    /**
     * Uncheck a checkbox, or radio button
     * @param css/by The element to uncheck
     * @return The implementing class for fluency
     */
    Test uncheck(String css);
    Test uncheck(By by);

    /**
     * Selects an option from a dropdown ({@literal <select> tag}) based on the text displayed.
     * @param css/by The element
     * @param text The text that is displaying.
     * @see #selectOptionByValue(By, String)
     * @return The implementing class for fluency
     */
    Test selectOptionByText(String css, String text);
    Test selectOptionByText(By by, String text);

    /**
     * Selects an option from a dropdown ({@literal <select> tag}) based on the value.
     * @param css/by The element
     * @param value The <code>value</code> attribute of the option.
     * @see #selectOptionByText(By, String)
     * @return The implementing class for fluency
     */
    Test selectOptionByValue(String css, String value);
    Test selectOptionByValue(By by, String value);

    /**
     * Waits for a window to appear, then switches to it.
     * @param regex Regex enabled. Url of the window, or title.
     * @return The implementing class for fluency
     */
    Test waitForWindow(String regex);

    /**
     * Switch's to a window that is already in existance.
     * @param regex Regex enabled. Url of the window, or title.
     * @return The implementing class for fluency
     */
    Test switchToWindow(String regex);

    /**
     * Close an open window.
     * <br>
     * If you have opened only 1 external window, then when you call this method, the context will switch back to the window you were using before.<br>
     * <br>
     * If you had more than 2 windows displaying, then you will need to call {@link #switchToWindow(String)} to switch back context.
     * @param regex The title of the window to close (regex enabled). You may specify <code>null</code> to close the active window. If you specify <code>null</code> then the context will switch back to the initial window.
     * @return The implementing class for fluency
     */
    Test closeWindow(String regex);

    /**
     * Closes the current active window.  Calling this method will return the context back to the initial window.
     * @return The implementing class for fluency
     */
    Test closeWindow();

    /**
     * Switches to a frame or iframe.
     * @param idOrName The id or name of the frame.
     * @return The implementing class for fluency
     */
    Test switchToFrame(String idOrName);

    /**
     * Switches to a frame based on the index it comes. (starting at <code>0</code>)
     * @param index The index of the frame in which it appears
     * @return The implementing class for fluency
     */
    Test switchToFrame(int index);

    /**
     * Switch back to the default content (the first window / frame that you were on before switching)
     * @return The implementing class for fluency
     */
    Test switchToDefaultContent();

    /*
        Validations
     */
    /**
     * Validates that an element is present.
     * @param css/by The element
     * @return The implementing class for fluency
     */
    Test validatePresent(String css);
    Test validatePresent(By by);

    /**
     * Validates that an element is not present.
     * @param css/by The element
     * @return The implementing class for fluency
     */
    Test validateNotPresent(String css);
    Test validateNotPresent(By by);

    /**
     * Validate that the text of an element is correct.
     * @param css/by The element to validate the text of.
     * @param text The text the element should have.
     * @return The implementing class for fluency
     */
    Test validateText(String css, String text);
    Test validateText(By by, String text);

    /**
     * Validate that the text of an element is not matching text.
     * @param css/by The element to validate the text of.
     * @param text The text the element should not have.
     * @return The implementing class for fluency
     */
    Test validateTextNot(String css, String text);
    Test validateTextNot(By by, String text);

    /**
     * Validate that text is present somewhere on the page.
     * @param text The text to ensure is on the page.
     * @return The implementing class for fluency
     */
    Test validateTextPresent(String text);

    /**
     * Validate that some text is nowhere on the page.
     * @param text The text to ensure is not on the page.
     * @return The implementing class for fluency
     */
    Test validateTextNotPresent(String text);

    /**
     * Validate that a checkbox or a radio button is checked.
     * @param css/by The element
     * @return The implementing class for fluency
     */
    Test validateChecked(String css);
    Test validateChecked(By by);

    /**
     * Validate that a checkbox or a radio button is unchecked.
     * @param css/by The element
     * @return The implementing class for fluency
     */
    Test validateUnchecked(String css);
    Test validateUnchecked(By by);

    /**
     * Validates an attribute of an element.<br><br>
     * Example:<br>
     * <blockquote>
     * {@literal <input type="text" id="test" />}
     * <br><br>
     * <code>.validateAttribute("input#test", "type", "text") // validates that the "type" attribute equals "test"</code>
     * </blockquote>
     * @param css/by The element
     * @param attr The attribute you'd like to validate
     * @param regex What the attribute <b>should</b> be.  (this method supports regex)
     * @return The implementing class for fluency
     */
    Test validateAttribute(String css, String attr, String regex);
    Test validateAttribute(By by, String attr, String regex);

    /**
     * Validate the Url
     * @param regex Regular expression to match
     * @return The implementing class for fluency
     */
    Test validateUrl(String regex);

    /**
     * Validates that a specific condition is true
     * @param condition The condition that is expected to be true
     * @return The implementing class for fluency
     */
    Test validateTrue(boolean condition);

    /**
     * Validates that a specific condition is false
     * @param condition The condition that is expected to be false
     * @return The implementing class for fluency
     */
    Test validateFalse(boolean condition);

    /**
     * Set and validate the text in a single step
     * @param css/by The element to set and validate the text of.
     * @param text The text to set and validate
     * @return The implementing class for fluency
     */
    Test setAndValidateText(String css, String text);
    Test setAndValidateText(By by, String text);

    /*
        End Validations
     */

    /*
        Navigation
     */
    /**
     * Navigates to an absolute or relative Url.
     * @param url Use cases are:<br>
     * <blockquote>
     * <code>navigateTo("/login") // navigate to a relative url. slash meaning start fresh from the base url.</code><br><br>
     * <code>navigateTo("path") // navigate to a relative url. will simply append "path" to the current url.</code><br><br>
     * <code>navigateTo("http://google.com") // navigates to an absolute url.</code>
     * </blockquote>
     * @return The implementing class for fluency
     */
    Test navigateTo(String url);

    /**
     * Navigates the browser back one page.
     * Same as <code>driver.navigate().back()</code>
     * @return The implementing class for fluency
     */
    Test goBack();

    /*
        Test collections
     */
    /**
     * Put a variable in the data warehouse.
     * @param key The key to put.
     * @param value The value to put.
     * @return The implementing class for fluency
     */
    Test store(String key, String value);

    /**
     * Get a variable from the data warehouse.<br><br>
     * If the key is not set, then use {@link #get(String, String)}
     * @param key The key to fetch.
     * @return The implementing class for fluency
     */
    String get(String key);

    /**
     * Get a variable from the data warehouse.
     * @param key The key to fetch.
     * @param defaultValue The value to return if the variable is not set.
     * @return The implementing class for fluency
     */
    String get(String key, String defaultValue);

    /*
        Logging
     */
    /**
     * Log something as information
     * @param object What to log.
     * @return The implementing class for fluency
     */
    Test log(Object object);

    /**
     * Log something as information
     * @param object What to log
     * @return The implementing class for fluency
     */
    Test logInfo(Object object);

    /**
     * Log something as a warning
     * @param object What to log
     * @return The implementing class for fluency
     */
    Test logWarn(Object object);

    /**
     * Log something as an error
     * @param object What to log
     * @return <code>AutomationTest</code> (for fluency)
     */
    Test logError(Object object);

    /**
     * Log something as debug
     * @param object What to log
     * @return The implementing class for fluency
     */
    Test logDebug(Object object);

    /**
     * Log something as fatal
     * @param object What to log
     * @return The implementing class for fluency
     */
    Test logFatal(Object object);
}
