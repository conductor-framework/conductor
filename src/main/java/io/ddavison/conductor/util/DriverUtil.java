package io.ddavison.conductor.util;

import io.ddavison.conductor.ConductorConfig;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.pmw.tinylog.Logger;

public class DriverUtil {

    public static WebDriver getDriver(ConductorConfig config) {
        return getDriver(config, null);
    }

    public static WebDriver getDriver(ConductorConfig config, DesiredCapabilities desiredCapabilities) {
        WebDriver driver = null;
        Capabilities capabilities;

        boolean isLocal = config.getHub() == null;
        try {
            switch (config.getBrowser()) {
                case CHROME:
                    ChromeOptions chromeOptions = new ChromeOptions();
                    setCustomCapabilities(config, chromeOptions, desiredCapabilities);
                    capabilities = chromeOptions;

                    if (isLocal) {
                        ChromeDriverManager.getInstance().setup();
                        driver = new ChromeDriver(chromeOptions);
                    }
                    break;
                case FIREFOX:
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    setCustomCapabilities(config, firefoxOptions, desiredCapabilities);
                    capabilities = firefoxOptions;

                    if (isLocal) {
                        FirefoxDriverManager.getInstance().setup();
                        driver = new FirefoxDriver(firefoxOptions);
                    }
                    break;
                case INTERNET_EXPLORER:
                    InternetExplorerOptions internetExplorerOptions = new InternetExplorerOptions();
                    setCustomCapabilities(config, internetExplorerOptions, desiredCapabilities);
                    capabilities = internetExplorerOptions;

                    if (isLocal) {
                        InternetExplorerDriverManager.getInstance().setup();
                        driver = new InternetExplorerDriver(internetExplorerOptions);
                    }
                    break;
                case EDGE:
                    EdgeOptions edgeOptions = new EdgeOptions();
                    setCustomCapabilities(config, edgeOptions, desiredCapabilities);
                    capabilities = edgeOptions;

                    if (isLocal) {
                        EdgeDriverManager.getInstance().setup();
                        driver = new EdgeDriver(edgeOptions);
                    }
                    break;
                case SAFARI:
                    SafariOptions safariOptions = new SafariOptions();

                    capabilities = setCustomCapabilities(config, safariOptions, desiredCapabilities);

                    if (isLocal) {
                        driver = new SafariDriver(safariOptions);
                    }
                    break;
                default:
                    Logger.error("Unknown browser: " + config.getBrowser());
                    return null;
            }

            if (!isLocal) {
                try {
                    driver = new RemoteWebDriver(config.getHub(), capabilities);
                } catch (Exception e) {
                    Logger.error(e, "Couldn't connect to hub: " + config.getHub().toString());
                }
            }
        } catch (Exception e) {
            Logger.error(e, "Probably a WebDriver issue, see https://github.com/bonigarcia/webdrivermanager");
            System.exit(1);
        }
        return driver;
    }

    public static MutableCapabilities setCustomCapabilities(ConductorConfig config, MutableCapabilities capabilities, DesiredCapabilities customDesiredCapabilities) {
        MutableCapabilities newCapabilities = new MutableCapabilities(capabilities);

        if (!config.getCustomCapabilities().isEmpty()) {
            for (String key : config.getCustomCapabilities().keySet()) {
                newCapabilities.setCapability(key, config.getCustomCapabilities().get(key));
            }
        }

        if (customDesiredCapabilities != null) {
            for (String key : customDesiredCapabilities.asMap().keySet()) {
                newCapabilities.setCapability(key, config.getCustomCapabilities().get(key));
            }
        }

        return newCapabilities;
    }

}
