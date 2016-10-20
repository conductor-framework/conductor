package io.ddavison.conductor;

import java.lang.annotation.Annotation;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.ddavison.conductor.util.JvmUtil;

/**
 * Created on 7/27/16.
 *
 * Order of overrides:
 * <ol>
 * <li>JVM Arguments</li>
 * <li>Test</li>
 * <li>Default properties</li>
 * </ol>
 */
public class LocomotiveConfig implements Config {

    private Config testConfig;
    private Properties properties;

    public LocomotiveConfig(Config testConfig, Properties properties) {
        this.testConfig = testConfig;
        this.properties = properties;
    }

    /**
     * Url that automated tests will be testing.
     *
     * @return If a base url is provided it'll return the base url + path, otherwise it'll fallback to the normal url params.
     */
    @Override
    public String url() {
        String url = "";
        if (!StringUtils.isEmpty(baseUrl())) {
            url = baseUrl() + path();
        } else {
            if (!StringUtils.isEmpty(properties.getProperty(Constants.DEFAULT_PROPERTY_URL))) {
                url = properties.getProperty(Constants.DEFAULT_PROPERTY_URL);
            }
            if (testConfig != null && (!StringUtils.isEmpty(testConfig.url()))) {
                url = testConfig.url();
            }
            if (!StringUtils.isEmpty(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_URL))) {
                url = JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_URL);
            }
        }
        return url;
    }

    @Override
    public Browser browser() {
        Browser browser = Browser.NONE;
        if (!StringUtils.isEmpty(properties.getProperty(Constants.DEFAULT_PROPERTY_BROWSER))) {
            browser = Browser.valueOf(properties.getProperty(Constants.DEFAULT_PROPERTY_BROWSER).toUpperCase());
        }
        if (testConfig != null && testConfig.browser() != Browser.NONE) {
            return testConfig.browser();
        }
        if (!StringUtils.isEmpty(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_BROWSER))) {
            browser = Browser.valueOf(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_BROWSER).toUpperCase());
        }
        return browser;
    }

    @Override
    public String hub() {
        String hub = "";
        if (!StringUtils.isEmpty(properties.getProperty(Constants.DEFAULT_PROPERTY_HUB))) {
            hub = properties.getProperty(Constants.DEFAULT_PROPERTY_HUB);
        }
        if (testConfig != null && (!StringUtils.isEmpty(testConfig.hub()))) {
            hub = testConfig.hub();
        }
        if (!StringUtils.isEmpty(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_HUB))) {
            hub = JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_HUB);
        }
        return hub;
    }

    @Override
    public String baseUrl() {
        String baseUrl = "";
        if (!StringUtils.isEmpty(properties.getProperty(Constants.DEFAULT_PROPERTY_BASE_URL))) {
            baseUrl = properties.getProperty(Constants.DEFAULT_PROPERTY_BASE_URL);
        }
        if (testConfig != null && !StringUtils.isEmpty(testConfig.baseUrl())) {
            baseUrl = testConfig.baseUrl();
        }
        if (!StringUtils.isEmpty(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_BASE_URL))) {
            baseUrl = JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_BASE_URL);
        }
        return baseUrl;
    }

    @Override
    public String path() {
        String path = "";
        if (testConfig != null && !StringUtils.isEmpty(testConfig.path())) {
            path = testConfig.path();
        }
        return path;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

	@Override
	public Class<? extends Capabilities> capabilities() {
		if (testConfig != null) {
			return testConfig.capabilities();
		}
		return DesiredCapabilities.class;
	}
}
