package io.ddavison.conductor.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 6/21/17.
 */
public class PropertiesUtil {

    private static final Logger log = LogManager.getLogger(PropertiesUtil.class);
    private static final String DEFAULT_PROPERTIES_FILE_NAME = "default.properties";

    public Properties loadDefault() {
        return load(DEFAULT_PROPERTIES_FILE_NAME);
    }

    public Properties load(String name) {
        Properties props = new Properties();
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            props.load(in);
            in.close();
        } catch (Exception e) {
            log.fatal("Couldn't loadDefault in default properties");
        }
        return props;
    }

}
