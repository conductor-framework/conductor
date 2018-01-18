package io.ddavison.conductor;

import org.testng.annotations.BeforeMethod;

@Config(browser = Browser.CHROME)
public class SuperClassTest extends Locomotive {

    @BeforeMethod
    public void navigateToLocalHtml() {
        navigateTo(FrameworkTest.TEST_HTML_FILE);
    }
}
