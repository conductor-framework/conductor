package io.ddavison.conductor;

import com.google.common.base.Strings;
import io.ddavison.conductor.util.ScreenShotUtil;
import org.apache.commons.lang3.StringUtils;
import org.assertj.swing.assertions.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Locomotive implements Conductor<Locomotive> {

    public ConductorConfig configuration;
    public WebDriver driver;
    private int attempts = 0;
    public Actions actions;
    private Map<String, String> vars = new HashMap<String, String>();

    private Pattern p;
    private Matcher m;

    public Locomotive() {
        Config testConfiguration = getClass().getAnnotation(Config.class);

        configuration = new ConductorConfig(testConfiguration);
        driver = DriverUtil.getDriver(configuration);

        Logger.debug(String.format("\n=== Configuration ===\n" +
                "\tURL:     %s\n" +
                "\tBrowser: %s\n" +
                "\tHub:     %s\n" +
                "\tBase url: %s\n", configuration.getUrl(), configuration.getBrowser().moniker, configuration.getHub(), configuration.getBaseUrl()));

        actions = new Actions(driver);

        // Automatically start test on url
        if (StringUtils.isNotEmpty(configuration.getUrl())) {
            driver.navigate().to(configuration.getUrl());
        }
    }

    @Rule
    public TestRule watchman = new TestWatcher() {
        boolean failure;
        Throwable e;
        Description description;


        @Override
        protected void failed(Throwable e, Description description) {
            if (configuration.isScreenshotOnFail()) {
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
            if (configuration.isScreenshotOnFail()) {
                if (failure) {
                    ScreenShotUtil.take(Locomotive.this,
                            description.getDisplayName(),
                            e.getMessage() != null ? e.getMessage() : e.toString());
                }
                Locomotive.this.driver.quit();
            }
        }
    };

    @After
    public void teardown() {
        if (!configuration.isScreenshotOnFail()) {
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
                    configuration.getRetries()));
        } else {
            // If an element is found then scroll to it.
            scrollTo(elements.get(0));
        }

        if (size > 1) {
            Logger.error("WARN: There are more than 1 %s 's!", by.toString());
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
            while (attempts <= configuration.getRetries()) {
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
        return waitForCondition(condition, configuration.getTimeout());
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
        final WebElement element = waitForElement(by);

        // position mouse over element before click.
        moveToElement(element);
        element.click();

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

    public boolean isInView(String css) {
        return isInView(By.cssSelector(css));
    }

    public boolean isInView(By by) {
        return isInView(driver.findElement(by));
    }

    /**
     * Returns whether the provided element is in the current view.
     */
    public boolean isInView(WebElement element) {
        return (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var element = arguments[0],                                " +
                        "  box = element.getBoundingClientRect(),           " +
                        "  centerX = box.left + box.width / 2,              " +
                        "  centerY = box.top + box.height / 2,              " +
                        "  e = document.elementFromPoint(centerX, centerY); " +
                        "for (; e; e = e.parentElement) {                   " +
                        "  if (e === element)                               " +
                        "    return true;                                   " +
                        "}                                                  " +
                        "return false;                                      "
                , element);
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
                if (attempts <= configuration.getRetries()) {
                    attempts++;

                    try {
                        Thread.sleep(1000);
                    } catch (Exception x) {
                        Logger.error(x);
                    }

                    return waitForWindow(regex);
                } else {
                    Assertions.fail("Window with url|title: " + regex + " did not appear after " + configuration.getRetries() + " tries. Exiting.", e);
                }
            }
        }

        // when we reach this point, that means no window exists with that title..
        if (attempts == configuration.getRetries()) {
            Assertions.fail("Window with title: " + regex + " did not appear after " + configuration.getRetries() + " tries. Exiting.");
            return this;
        } else {
            Logger.info("#waitForWindow() : Window doesn't exist yet. [%s] Trying again. %s/%s", regex, (attempts + 1), configuration.getRetries());
            attempts++;
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                Logger.error(e);
            }
            return waitForWindow(regex);
        }
    }

    public Locomotive switchToWindow(String regex) {
        Set<String> windows = driver.getWindowHandles();

        for (String window : windows) {
            driver.switchTo().window(window);
            Logger.info("#switchToWindow() : title=%s ; url=%s",
                    driver.getTitle(),
                    driver.getCurrentUrl());

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

    /**
     * Scroll to a specified element
     *
     * @param css Css locator for the element to scroll to
     * @return This instance for method chaining.
     */
    public Locomotive scrollTo(String css) {
        return scrollTo(By.cssSelector(css));
    }

    /**
     * Scroll to a specified element
     *
     * @param by Locator for the element to scroll to
     * @return This instance for method chaining.
     */
    public Locomotive scrollTo(By by) {
        // Find the element to scroll to. Cannot use waitForElement() because it would create an infinite loop.s
        return scrollTo(driver.findElement(by));
    }

    /**
     * Scroll to a specified element
     *
     * @param element to scroll to
     * @return This instance for method chaining.
     */
    public Locomotive scrollTo(WebElement element) {
        // Execute javascript to scroll to the element.
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView()", element);

        return this;
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

    /**
     * Validate Text ignores white spaces
     */
    public Locomotive validateText(By by, String text) {
        Assertions.assertThat(text).isEqualToIgnoringWhitespace(getText(by));
        return this;
    }

    public Locomotive validateTextIgnoreCase(String css, String text) {
        return validateTextIgnoreCase(By.cssSelector(css), text);
    }

    /**
     * Validate Text ignores case and white spaces
     */
    public Locomotive validateTextIgnoreCase(By by, String text) {
        Assertions.assertThat(text.toLowerCase()).isEqualToIgnoringWhitespace(getText(by).toLowerCase());
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
            Assertions.fail(String.format("Attribute not fount! [Attribute: %s] [Desired value: %s] [Actual value: %s] [Element: %s] [Message: %s]",
                    attr,
                    regex,
                    actual,
                    element.toString(),
                    e.getMessage()), e);
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
            driver.navigate().to(configuration.getBaseUrl().concat(url));
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
        Logger.debug(object);
        return this;
    }

    public Locomotive logWarn(Object object) {
        Logger.warn(object);
        return this;
    }

    public Locomotive logError(Object object) {
        Logger.error(object);
        return this;
    }

    public Locomotive logDebug(Object object) {
        Logger.debug(object);
        return this;
    }

    public Locomotive logFatal(Object object) {
        Logger.error(object);
        return this;
    }
}
