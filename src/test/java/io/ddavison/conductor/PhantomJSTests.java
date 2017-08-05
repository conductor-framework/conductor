package io.ddavison.conductor;

import org.junit.Test;

/**
 * Created by dj on 1/18/17.
 */
@Config(browser = Browser.PHANTOMJS, url="http://ddavison.io/tests/getting-started-with-selenium.htm")
public class PhantomJSTests extends Locomotive {
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
}
