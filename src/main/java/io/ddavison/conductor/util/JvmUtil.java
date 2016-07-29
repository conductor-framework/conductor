package io.ddavison.conductor.util;

/**
 * Created on 7/29/16.
 */
public class JvmUtil {

    /**
     * Get a Jvm property / environment variable
     * @param prop the property to get
     * @return the property value
     */
    public static String getJvmProperty(String prop) {
        return (System.getProperty(prop, System.getenv(prop)));
    }

}
