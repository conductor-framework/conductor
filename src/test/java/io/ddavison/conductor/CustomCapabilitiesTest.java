package io.ddavison.conductor;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class CustomCapabilitiesTest {

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
    public void custom_capabilities_are_being_added() {
        ConductorConfig config = new ConductorConfig("/test_yaml/simple_defaults.yaml");
        ChromeOptions options = new ChromeOptions();
        DriverUtil.setCustomCapabilities(config, options);

        Map<String, String> expectedCapabilities = new HashMap<>();
        expectedCapabilities.put("fizz", "buzz");
        expectedCapabilities.put("foo", "bar");

        Assertions.assertThat(options.asMap())
                .containsAllEntriesOf(expectedCapabilities);
    }

    @Test
    public void current_scheme_custom_capabilities_are_being_added() {
        ConductorConfig config = new ConductorConfig("/test_yaml/all.yaml");
        ChromeOptions options = new ChromeOptions();
        DriverUtil.setCustomCapabilities(config, options);

        Map<String, String> expectedCapabilities = new HashMap<>();
        expectedCapabilities.put("foo", "bar");
        expectedCapabilities.put("fizz", "buzz");
        expectedCapabilities.put("bar", "foo");

        Assertions.assertThat(options.asMap())
                .containsAllEntriesOf(expectedCapabilities);
    }
}
