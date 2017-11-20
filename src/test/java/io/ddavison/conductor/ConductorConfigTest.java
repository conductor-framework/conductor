package io.ddavison.conductor;

import io.ddavison.conductor.test.SimpleClassConfig;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ConductorConfigTest {

    private String envCurrentSchemes, envBaseUrl;

    @Before
    public void before() {
        envCurrentSchemes = System.getProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES);
        envBaseUrl = System.getProperty(ConductorConfig.CONDUCTOR_BASE_URL);

        System.clearProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES);
        System.clearProperty(ConductorConfig.CONDUCTOR_BASE_URL);
    }

    @After
    public void tearDown() {
        if (envCurrentSchemes != null) {
            System.setProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES, envCurrentSchemes);
        }
        if (envBaseUrl != null) {
            System.setProperty(ConductorConfig.CONDUCTOR_BASE_URL, envBaseUrl);
        }
    }

    @Test
    public void no_config_reads_default_yaml()  {
        ConductorConfig config = new ConductorConfig();
        Assertions.assertThat(config.getBrowser())
                .isEqualByComparingTo(Browser.CHROME);
    }

    @Test
    public void config_supplied_reads_supplied_config() {
        ConductorConfig config = new ConductorConfig("/test_yaml/simple.yaml");

        String[] expectedSchemes = {"test_scheme1", "test_scheme2"};

        Assertions.assertThat(config.getBrowser())
                .isEqualByComparingTo(Browser.CHROME);
        Assertions.assertThat(config.getCurrentSchemes())
                .isEqualTo(expectedSchemes);
    }

    @Test
    public void config_reads_defaults() {
        ConductorConfig config = new ConductorConfig("/test_yaml/simple_defaults.yaml");

        Map<String, String> customCapabilities = new HashMap<>();
        customCapabilities.put("foo", "bar");
        customCapabilities.put("fizz", "buzz");

        Assertions.assertThat(config.getBrowser())
                .isEqualByComparingTo(Browser.SAFARI);
        Assertions.assertThat(config.getBaseUrl())
                .isEqualTo("https://willowtreeapps.com/");
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(8);
        Assertions.assertThat(config.getRetries())
                .isEqualTo(10);
        Assertions.assertThat(config.getHub().toString())
                .isEqualTo("http://hub.com/wd/kad");
        Assertions.assertThat(config.isScreenshotOnFail())
                .isTrue();
        Assertions.assertThat(config.getCustomCapabilities())
                .containsAllEntriesOf(customCapabilities);
    }

    /**
     * This test will clear the current schemes environment variable to verify yaml schemes override defaults
     */
    @Test
    public void config_overrides_with_current_schemes_excluding_environment_variables() {
        System.clearProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES);

        ConductorConfig config = new ConductorConfig("/test_yaml/schemes.yaml");

        Assertions.assertThat(config.getBrowser())
                .isEqualByComparingTo(Browser.SAFARI);
        Assertions.assertThat(config.getBaseUrl())
                .isEqualTo("https://wta-stage.herokuapp.com");
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(20);
        Assertions.assertThat(config.getRetries())
                .isEqualTo(3);
        Assertions.assertThat(config.getHub().toString())
                .isEqualTo("http://hub.com/wd/kad");
        Assertions.assertThat(config.isScreenshotOnFail())
                .isTrue();
    }

    @Test
    public void config_overrides_schemes_in_order() {
        ConductorConfig config = new ConductorConfig("/test_yaml/override_schemes.yaml");

        Assertions.assertThat(config.getCurrentSchemes())
                .isEqualTo(new String[] { "longer_timeouts", "even_longer_timeouts" });
        Assertions.assertThat(config.getRetries())
                .isEqualTo(32);
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(40);
        Assertions.assertThat(config.getBaseUrl())
                .isEqualTo("http://google.com");
        Assertions.assertThat(config.getBrowser())
                .isEqualTo(Browser.EDGE);

        Map<String, String> customCapabilities = new HashMap<>();
        customCapabilities.put("foo", "buzz");
        customCapabilities.put("fizz", "blerg");
        customCapabilities.put("boop", "boop");

        Assertions.assertThat(config.getCustomCapabilities())
                .containsAllEntriesOf(customCapabilities);
    }

    @Test
    public void environment_platform_name_overrides_config() {
        System.setProperty(ConductorConfig.CONDUCTOR_BASE_URL, "http://iamawesome.com/");
        ConductorConfig config = new ConductorConfig("/test_yaml/all.yaml");

        Assertions.assertThat(config.getBaseUrl())
                .isEqualTo("http://iamawesome.com/");
        Assertions.assertThat(config.getBrowser())
                .isEqualTo(Browser.CHROME);

        System.clearProperty(ConductorConfig.CONDUCTOR_BASE_URL);
    }

    @Test
    public void environment_schemes_overrides_config() {
        System.setProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES, "shorter_timeouts,stage-dev,firefox");
        ConductorConfig config = new ConductorConfig("/test_yaml/all.yaml");

        Assertions.assertThat(config.getBrowser())
                .isEqualTo(Browser.FIREFOX);
        Assertions.assertThat(config.getBaseUrl())
                .isEqualTo("https://wta-stage.herokuapp.com");
        Assertions.assertThat(config.getRetries())
                .isEqualTo(8);
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(5);
        Assertions.assertThat(config.isScreenshotOnFail())
                .isTrue();

        System.clearProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES);
    }

    @Test
    public void empty_environment_schemes_clears_yaml_schemes() {
        System.setProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES, "");
        ConductorConfig config = new ConductorConfig("/test_yaml/all.yaml");

        Assertions.assertThat(config.getBrowser())
                .isEqualTo(Browser.CHROME);
        Assertions.assertThat(config.getBaseUrl())
                .isEqualTo("https://willowtreeapps.com");
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(8);
        Assertions.assertThat(config.getRetries())
                .isEqualTo(10);
        Assertions.assertThat(config.isScreenshotOnFail())
                .isTrue();

        System.clearProperty(ConductorConfig.CONDUCTOR_CURRENT_SCHEMES);
    }

    @Test
    public void class_config() {
        Config classConfig = new SimpleClassConfig();
        ConductorConfig config = new ConductorConfig(classConfig);

        Assertions.assertThat(config.getPath())
                .isEqualTo("/buzz");
    }

    @Test
    public void class_config_overrides_defaults() {
        Config classConfig = new SimpleClassConfig();
        ConductorConfig config = new ConductorConfig("/test_yaml/simple.yaml", classConfig);

        Assertions.assertThat(config.getBrowser())
                .isEqualTo(Browser.SAFARI);
    }

    @Test
    public void class_config_is_overridden_by_yaml_schemes() {
        Config classConfig = new SimpleClassConfig();
        ConductorConfig config = new ConductorConfig("/test_yaml/browser_schemes.yaml", classConfig);

        Assertions.assertThat(config.getBrowser())
                .isEqualTo(Browser.FIREFOX);
    }

}
