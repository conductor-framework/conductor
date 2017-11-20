package io.ddavison.conductor;

import org.junit.Before;

@Config(browser = Browser.CHROME)
public class SuperClassTest extends Locomotive {

    @Before
    public void navigateToLocalHtml() {
        navigateTo(FrameworkTest.TEST_HTML_FILE);
    }
}
