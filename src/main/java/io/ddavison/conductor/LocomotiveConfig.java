package io.ddavison.conductor;

import io.ddavison.conductor.util.JvmUtil;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Properties;

/**
 * Created on 7/27/16.
 * <p>
 * Order of overrides:
 * <ol>
 * <li>JVM Arguments</li>
 * <li>Test</li>
 * <li>Default properties</li>
 * </ol>
 */
public class LocomotiveConfig implements Config {

    public static int DEFAULT_MAX_RETRIES = 5;
    public static int DEFAULT_MAX_TIMEOUT = 5;

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
        HttpUrl url = null;
        if (!StringUtils.isEmpty(baseUrl())) {
            url = HttpUrl.parse(baseUrl())
                    .newBuilder()
                    .addPathSegment(path().startsWith("/") ? path().substring(1) : path())
                    .build();
        } else {
            if (!StringUtils.isEmpty(properties.getProperty(Constants.DEFAULT_PROPERTY_URL))) {
                url = HttpUrl.parse(properties.getProperty(Constants.DEFAULT_PROPERTY_URL));
            }
            if (testConfig != null && (!StringUtils.isEmpty(testConfig.url()))) {
                url = HttpUrl.parse(testConfig.url());
            }
            if (!StringUtils.isEmpty(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_URL))) {
                url = HttpUrl.parse(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_URL));
            }
        }
        return url == null ? "" : url.toString();
    }

    @Override
    public Browser browser() {
        Browser browser = Browser.NONE;
        if (!StringUtils.isEmpty(properties.getProperty(Constants.DEFAULT_PROPERTY_BROWSER))) {
            browser = Browser.valueOf(properties.getProperty(Constants.DEFAULT_PROPERTY_BROWSER).toUpperCase());
        }
        if (testConfig != null && testConfig.browser() != Browser.NONE) {
            browser =  testConfig.browser();
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
        HttpUrl url = null;
        if (!StringUtils.isEmpty(properties.getProperty(Constants.DEFAULT_PROPERTY_BASE_URL))) {
            url = HttpUrl.parse(properties.getProperty(Constants.DEFAULT_PROPERTY_BASE_URL));
        }
        if (testConfig != null && !StringUtils.isEmpty(testConfig.baseUrl())) {
            url = HttpUrl.parse(testConfig.baseUrl());
        }
        if (!StringUtils.isEmpty(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_BASE_URL))) {
            url = HttpUrl.parse(JvmUtil.getJvmProperty(Constants.JVM_CONDUCTOR_BASE_URL));
        }
        return url != null ? url.toString() : "";
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
    public int timeout() {
        return getIntValue(Constants.DEFAULT_PROPERTY_TIMEOUT,
                testConfig == null ? null : testConfig.timeout(),
                Constants.JVM_CONDUCTOR_TIMEOUT,
                DEFAULT_MAX_TIMEOUT);
    }

    @Override
    public int retries() {
        return getIntValue(Constants.DEFAULT_PROPERTY_RETRIES,
                testConfig == null ? null : testConfig.retries(),
                Constants.JVM_CONDUCTOR_RETRIES,
                DEFAULT_MAX_RETRIES);
    }

    public boolean screenshotsOnFail() {
        return getBooleanValue(Constants.DEFAULT_PROPERTY_SCREENSHOTS_ON_FAIL,
                Constants.JVM_CONDUCTOR_SCREENSHOTS_ON_FAIL);
    }

    private int getIntValue(String defaultPropertyKey, Integer testConfigValue, String jvmParamKey, int defaultValue) {
        int value = defaultValue;
        String defaultPropValue = getProperty(defaultPropertyKey, String.valueOf(defaultValue));
        String jvmValue = JvmUtil.getJvmProperty(jvmParamKey);

        if(defaultPropValue != null && !StringUtils.isEmpty(defaultPropValue)) {
            value = Integer.valueOf(defaultPropValue);
        }
        if(testConfigValue != null) {
            value = testConfigValue;
        }
        if(jvmValue != null && !StringUtils.isEmpty(jvmValue)) {
            value = Integer.valueOf(jvmValue);
        }
        return value;
    }

    private boolean getBooleanValue(String defaultPropertyKey, String jvmParamKey) {
        boolean value = false;
        String defaultValue = getProperty(defaultPropertyKey, Boolean.FALSE.toString());
        String jvmValue = JvmUtil.getJvmProperty(jvmParamKey);

        if(defaultValue != null && !StringUtils.isEmpty(defaultValue)) {
            value = Boolean.valueOf(defaultValue);
        }
        if(jvmValue != null && !StringUtils.isEmpty(jvmValue)) {
            value = Boolean.valueOf(jvmValue);
        }
        return value;
    }

    private String getProperty(String key, String defaultValue) {
        return properties == null ? "" : properties.getProperty(key, defaultValue);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
