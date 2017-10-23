/*
 * Copyright 2014-2016 Daniel Davison (http://github.com/ddavison) and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package io.ddavison.conductor;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Config(
        browser = Browser.CHROME,
        url = "http://ddavison.io/tests/getting-started-with-selenium.htm")
public class FrameworkTest extends Locomotive {

    private static final String NEW_TAB_LINK_CSS = "a[href='http://google.com']";

    @Test
    public void testClick() throws Exception {
        click("#click")
                .validatePresent("#click.success"); // adds the .success class after click
    }

    @Test
    public void testSetText() throws Exception {
        setText("#setTextField", "test")
                .validateText("#setTextField", "test");
    }

    @Test
    public void testTextIgnoringWhiteSpaces() {
        setText("#setTextField", " test  ignoring white spaces")
                .validateText("#setTextField", "test      ignoring     white    spaces    ");
    }

    @Test
    public void testCheckUncheck() throws Exception {
        check("#checkbox")
                .validateChecked("#checkbox")
                .uncheck("#checkbox")
                .validateUnchecked("#checkbox");
    }

    @Test
    public void testSelectOption() throws Exception {
        selectOptionByText("#select", "Third")
                .validateText("#select", "3")
                .selectOptionByValue("#select", "2")
                .validateText("#select", "2");
    }

    @Test
    public void testFrames() throws Exception {
        switchToFrame("frame")
                .validatePresent("#frame_content")
                .switchToDefaultContent()
                .validateNotPresent("#frame_content");
    }

    @Test
    public void testWindowSwitching() throws Exception {
        click(NEW_TAB_LINK_CSS)
                .waitForWindow(".*Google.*")
                .validatePresent("[name='q']")
                .closeWindow()
                .validateNotPresent("[name='q']");
    }

    @Test
    public void testValidatingAttributes() throws Exception {
        validateAttribute("#click", "class", "^box$")
                .click("#click")
                .validateAttribute("#click", "class", ".*success.*");
    }

    @Test
    public void testVariables() throws Exception {
        store("initial_text", getText("#setTextField"))
                .validateTrue(get("initial_text").equals("some text")); // the text box defaults to the text "some text"
    }

    @Test
    public void testWaitingFor() throws Exception {
        By checkbox = By.cssSelector("#checkbox");
        check(checkbox)
                .waitForCondition(ExpectedConditions.elementSelectionStateToBe(
                        checkbox, true
                ));
    }

    @Test
    public void testGetTextFromTextArea() throws Exception {
        setText("#textArea", "some text")
                .validateText("#textArea", "some text");
    }

    /**
     * Test for {@link #scrollTo(WebElement)}.
     *
     * Verifies an out of view element can only be clicked if scrolled to.
     */
    @Test
    public void testScrollTo() {
        final WebElement newTabLink               = waitForElement(NEW_TAB_LINK_CSS);
        final String     expectedExceptionMessage = "Element is not clickable";

        // setup the test page to make scrolling necessary: expect to not be able to click on the open new tab link
        driver.manage().window().setSize(new Dimension(100, 50));

        // expect an exception for a click on the out of view link
        try {
            newTabLink.click();
            fail(format("Expected WebDriverException with message '%s' to be thrown.", expectedExceptionMessage));
        } catch (WebDriverException exception) {
            assertTrue(format("Expected exception message to include '%s'.", expectedExceptionMessage),
                    exception.getMessage().contains(expectedExceptionMessage));
        }

        // scroll to the link
        scrollTo(NEW_TAB_LINK_CSS);

        // verify the open new tab link is now clickable without throwing an exception.
        newTabLink.click();
    }

}
