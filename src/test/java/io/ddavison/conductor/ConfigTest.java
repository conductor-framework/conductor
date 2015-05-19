/*
 * Copyright 2014 Daniel Davison (http://github.com/ddavison)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package io.ddavison.conductor;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author ddavison
 * @since Apr 03, 2015
 */
@Ignore
public class ConfigTest {
    @Test
    public void testJVMOverrides() {
        String url = "http://ddavison.io";
        String browser = "CHROME";
        String hub = "http://some-hub:4444";

        System.setProperty("default.url", url);
        System.setProperty("default.browser", browser);
        System.setProperty("default.hub", hub);
        TestWithoutConfig testWithoutConfig = new TestWithoutConfig();

        assertTrue(testWithoutConfig.configuration.url(),
                   testWithoutConfig.configuration.url().equals(url));

        assertTrue(testWithoutConfig.configuration.browser().moniker,
                   testWithoutConfig.configuration.browser().moniker.equalsIgnoreCase(browser));

        assertTrue(testWithoutConfig.configuration.hub(),
                   testWithoutConfig.configuration.hub().equals(hub));

    }

    @Ignore
    @Test
    public void testTestOverrides() {
        String jvmUrl = "http://ddavison.io";
        String jvmBrowser = "CHROME";
        String jvmHub = "http://some-hub:4444";

        System.setProperty("default.url", jvmUrl);
        System.setProperty("default.browser", jvmBrowser);
        System.setProperty("default.hub", jvmHub);

        TestWithConfig testWithConfig = new TestWithConfig();

        assertFalse(testWithConfig.configuration.url(),
                   testWithConfig.configuration.url().equals(jvmUrl));

        assertFalse(testWithConfig.configuration.browser().moniker,
        testWithConfig.configuration.browser().moniker.equalsIgnoreCase(jvmBrowser));

        assertFalse(testWithConfig.configuration.hub(),
                   testWithConfig.configuration.hub().equals(jvmHub));

    }

    @Ignore
    @Test
    public void testDefaultProperties() {
        System.clearProperty("default.url");
        System.clearProperty("default.hub");
        System.clearProperty("default.browser");
        Properties props = new Properties();
        try {props.load(getClass().getResourceAsStream("/default.properties"));}
        catch(Exception x) {
            x.printStackTrace();
            fail();
        }

        TestWithoutConfig testWithoutConfig = new TestWithoutConfig();

        assertTrue(testWithoutConfig.configuration.url(),
                   testWithoutConfig.configuration.url().equals(props.getProperty("url")));

        assertTrue(testWithoutConfig.configuration.browser().moniker,
                   testWithoutConfig.configuration.browser().moniker.equalsIgnoreCase(props.getProperty("browser")));

        assertTrue(String.format("%s should be %s", testWithoutConfig.configuration.hub(),props.getProperty("hub")),
                   testWithoutConfig.configuration.hub().equals(props.getProperty("hub")));

        testWithoutConfig.teardown();
    }
}

class TestWithoutConfig extends Locomotive {}
@Config(url="http://google.com", hub="http://some-other-hub:4444", browser = Browser.FIREFOX)
class TestWithConfig extends Locomotive {}
