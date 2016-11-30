package io.ddavison.conductor;

import org.assertj.swing.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 11/17/16.
 */
public class ConfigTest {

    private Config testConfig;
    private Properties defaultProperties = new Properties();

    @Before
    public void setup() {
        try {
            defaultProperties.load(getClass().getResourceAsStream("/default.properties"));
        } catch (Exception e) {
            Assertions.fail("Couldn't load in default properties");
        }
        defaultProperties.setProperty(Constants.DEFAULT_PROPERTY_TIMEOUT, String.valueOf(15));

        testConfig = mock(Config.class);
        when(testConfig.timeout()).thenReturn(10);
        when(testConfig.screenshotsOnFail()).thenReturn(false);
    }

    @After
    public void teardown() {
        System.clearProperty(Constants.JVM_CONDUCTOR_SCREENSHOTS_ON_FAIL);
        System.clearProperty(Constants.JVM_CONDUCTOR_TIMEOUT);
        System.clearProperty(Constants.JVM_CONDUCTOR_RETRIES);
    }

    @Test
    public void test_default_boolean_properties() {
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.screenshotsOnFail())
                .isTrue();
    }

    @Test
    public void test_config_override_default_boolean_properties() {
        LocomotiveConfig config = new LocomotiveConfig(testConfig, defaultProperties);
        Assertions.assertThat(config.screenshotsOnFail())
                .isFalse();
    }

    @Test
    public void test_jvm_overrides_test_config_boolean_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_SCREENSHOTS_ON_FAIL, Boolean.TRUE.toString());
        LocomotiveConfig config = new LocomotiveConfig(testConfig, null);
        Assertions.assertThat(config.screenshotsOnFail())
                .isTrue();
    }

    @Test
    public void test_jvm_overrides_default_boolean_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_SCREENSHOTS_ON_FAIL, Boolean.FALSE.toString());
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.screenshotsOnFail())
                .isFalse();
    }
}
