/*
 * Copyright 2014 Daniel Davison (http://github.com/ddavison)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package io.ddavison.selenium;

import org.junit.Test;

@Config(browser = Browser.CHROME, url="http://ddavison.io/tests/getting-started-with-selenium.htm")
public class FrameworkTest extends AutomationTest {
    @Test
    public void testClick() throws Exception {
        log("Test started")
        .click("#click")
        .validatePresent("#click.success"); // adds the .success class after click
    }

    @Test
    public void testSetText() throws Exception {
        setText("#setTextField", "test")
        .validateText("#setTextField", "test");
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
        click("a[href='http://google.com']")
        .waitForWindow(".*Google.*")
        .validatePresent("[name='q']")
        .closeWindow()
        .validateNotPresent("[name='q']");
    }

    @Test
    public void testVariables() throws Exception {
        store("initial_text", getText("#setTextField"))
        .validateTrue(get("initial_text").equals("some text")); // the text box defaults to the text "some text"
    }
}
