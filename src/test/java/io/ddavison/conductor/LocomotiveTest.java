package io.ddavison.conductor;

import org.assertj.swing.assertions.Assertions;
import org.junit.Test;

/**
 * Created on 11/17/16.
 */
@Config(
        baseUrl = "http://ddavison.io",
        path = "/tests/getting-started-with-selenium.htm"
)
public class LocomotiveTest extends Locomotive {

    @Test
    public void test_default_boolean_property() {
        Assertions.assertThat(this.configuration.screenshotsOnFail()).isTrue();
    }

}
