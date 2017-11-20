package io.ddavison.conductor;

import okhttp3.HttpUrl;
import org.pmw.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConductorConfig {

    // Default YAML
    private static final String DEFAULT_CONFIG_FILE = "/config.yaml";

    // JVM env args
    static final String CONDUCTOR_CURRENT_SCHEMES = "conductorCurrentSchemes";
    static final String CONDUCTOR_BASE_URL = "conductorBaseUrl";

    // YAML Keys
    private static final String DEFAULTS = "defaults";
    private static final String CURRENT_SCHEMES = "currentSchemes";
    private static final String CUSTOM_CAPABILITIES = "customCapabilities";

    // Conductor properties
    private String baseUrl = "";
    private String path = "";
    private String[] currentSchemes;
    private int timeout = 5;
    private int retries = 5;
    private boolean screenshotOnFail = true;
    private Map<String, String> customCapabilities = new HashMap<>();

    // Selenium Properties
    private Browser browser = Browser.NONE;
    private String hub;

    public ConductorConfig() {
        this(DEFAULT_CONFIG_FILE, null);
    }

    public ConductorConfig(String configPath) {
        this(configPath, null);
    }

    public ConductorConfig(Config classConfig) {
        this(DEFAULT_CONFIG_FILE, classConfig);
    }

    public ConductorConfig(String configPath, Config classConfig) {
        try {
            InputStream is = this.getClass().getResourceAsStream(configPath);
            if (is != null) {
                readConfig(is, classConfig);
            }
        } catch (Exception e) {
            Logger.error(e, "Couldn't load default conductor config!");
        }
    }

    private void readConfig(InputStream is, Config classConfig) {
        if (is == null) {
            throw new NullPointerException("InputStream parameter to readConfig cannot be null");
        }

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(is);

        // Read Defaults
        Map<String, Object> defaults = (Map<String, Object>) config.get(DEFAULTS);
        if (defaults != null) {
            readProperties(defaults);
        }

        // Read Schemes
        String environmentSchemes = System.getProperty(CONDUCTOR_CURRENT_SCHEMES);
        if (environmentSchemes != null) {
            currentSchemes = environmentSchemes.split(",");
        } else {
            List<String> schemesList = (List<String>) config.get(CURRENT_SCHEMES);
            if (schemesList != null) {
                currentSchemes = new String[schemesList.size()];
                schemesList.toArray(currentSchemes);
            }
        }

        // Override defaults with class configs
        if (classConfig != null) {
            readClassProperties(classConfig);
        }

        // If schemes not empty, override defaults
        if (currentSchemes != null) {
            for (String scheme : currentSchemes) {
                Map<String, Object> schemeData = (Map<String, Object>) config.get(scheme);
                if (schemeData != null) {
                    readProperties(schemeData);
                }
            }
        }

        // Override base url from env var
        String baseUrl = System.getProperty(CONDUCTOR_BASE_URL);
        if (baseUrl != null) {
            setBaseUrl(baseUrl);
        }
    }

    private void readProperties(Map<String, Object> properties) {
        for (String key : properties.keySet()) {
            if (key.equals(CUSTOM_CAPABILITIES)) {
                setCustomCapabilities(properties.get(key));
            } else {
                setProperty(key, properties.get(key).toString());
            }
        }
    }

    private void setProperty(String propertyName, String propertyValue) {
        Method[] methods = this.getClass().getMethods();

        String capPropertyKey = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        String methodName = "set" + capPropertyKey;
        Method foundMethod = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                foundMethod = method;
                break;
            }
        }

        if (foundMethod != null) {
            try {
                if (foundMethod.getParameterTypes()[0] == String.class) {
                    foundMethod.invoke(this, propertyValue);
                } else if (foundMethod.getParameterTypes()[0] == boolean.class) {
                    boolean value = Boolean.parseBoolean(propertyValue);
                    foundMethod.invoke(this, value);
                } else if (foundMethod.getParameterTypes()[0] == int.class) {
                    int value = Integer.parseInt(propertyValue);
                    foundMethod.invoke(this, value);
                } else if (foundMethod.getParameterTypes()[0] == Browser.class) {
                    Browser value = Browser.valueOf(propertyValue);
                    foundMethod.invoke(this, value);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.warn(e,"Could not invoke method '%s'", methodName);
            }
        }
    }

    private void readClassProperties(Config classConfig) {
        if (classConfig.browser() != Browser.NONE) {
            setBrowser(classConfig.browser());
        }
        if (!classConfig.path().isEmpty()) {
            setPath(classConfig.path());
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getCurrentSchemes() {
        return currentSchemes;
    }

    public void setCurrentSchemes(String[] currentSchemes) {
        this.currentSchemes = currentSchemes;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean isScreenshotOnFail() {
        return screenshotOnFail;
    }

    public void setScreenshotOnFail(boolean screenshotOnFail) {
        this.screenshotOnFail = screenshotOnFail;
    }

    public Browser getBrowser() {
        return browser;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public URL getHub() {
        URL url = null;
        if (hub != null) {
            try {
                url = new URL(hub);
            } catch (MalformedURLException e) {
                Logger.error(e,"Failure parsing url");
            }
        }

        return url;
    }

    public void setHub(String hub) {
        this.hub = hub;
    }

    public Map<String, String> getCustomCapabilities() {
        return customCapabilities;
    }

    public void setCustomCapabilities(Object keyCustomCapabilities) {
        Map<String, String> custom = new HashMap<>();
        if (keyCustomCapabilities instanceof Map) {
            for (String key : ((Map<String, Object>) keyCustomCapabilities).keySet()) {
                Object value = ((Map) keyCustomCapabilities).get(key);
                if (value instanceof String) {
                    custom.put(key, (String) value);
                } else {
                    throw new ClassCastException(String.format("%s is expected to be a String", value));
                }
            }
        } else {
            throw new ClassCastException(String.format("%s is expected to be a String list of key/value pairs", CUSTOM_CAPABILITIES));
        }
        setCustomCapabilities(custom);
    }

    public void setCustomCapabilities(Map<String, String> customCapabilities) {
        this.customCapabilities.putAll(customCapabilities);
    }

    public String getUrl() {
        return HttpUrl.parse(getBaseUrl())
                .newBuilder()
                .addPathSegment(getPath().startsWith("/") ? getPath().substring(1) : getPath())
                .build()
                .toString();
    }
}
