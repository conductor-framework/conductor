package io.ddavison.conductor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * 
 * @author kubisch
 *
 * this class is an example to set the log level of chrome to all
 */
public class ChromeCapabilitiesExample extends DesiredCapabilities {
	private static final long serialVersionUID = -5387232422671404105L;

	private static Map<String, Object> capMap = new HashMap<String, Object>();
	
	static {
		// fill the HashMap with your capabilities
		capMap.put(CapabilityType.LOGGING_PREFS, getLogPrefs());
	}
	
	public ChromeCapabilitiesExample() {
		super(capMap);
	}
	
    private static LoggingPreferences getLogPrefs()
    {
    	LoggingPreferences logPrefs = new LoggingPreferences();
    	logPrefs.enable(LogType.BROWSER, Level.ALL);
    	return logPrefs;
    }
}
