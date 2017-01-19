/*
 * Copyright 2014-2016 Daniel Davison (http://github.com/ddavison) and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package io.ddavison.conductor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.logging.Level;

import org.junit.Test;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

/**
 * @author {supernevi}
 * @since Oct 11, 2016
 */
@Config(browser = Browser.CHROME, url="http://ddavison.io/tests/logging-test.htm")
public class LoggingTest extends Locomotive {
	@Test
	public void loggingTest() {
		LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
		Iterator<LogEntry> it = logEntries.iterator();
		assertNotNull(it);
		assertNotNull(it.hasNext());
		LogEntry entry = null;
		
		// There should be 5 console-outputs
		// 1. "console.log("Simple log");"
		assertNotNull(it.hasNext());
		entry = it.next();
		System.out.println(entry);
		assertEquals(Level.INFO, entry.getLevel());
		assertTrue(entry.getMessage().endsWith("Simple log"));
				
		// 2. "console.debug("Debug log");"
		assertNotNull(it.hasNext());
		entry = it.next();
		System.out.println(entry);
		assertEquals(Level.FINE, entry.getLevel());
		assertTrue(entry.getMessage().endsWith("Debug log"));
		
		// 3. "console.warn("Warn log");"
		assertNotNull(it.hasNext());
		entry = it.next();
		System.out.println(entry);
		assertEquals(Level.WARNING, entry.getLevel());
		assertTrue(entry.getMessage().endsWith("Warn log"));
		
		// 4. "console.error("Error log");"
		assertNotNull(it.hasNext());
		entry = it.next();
		System.out.println(entry);
		assertEquals(Level.SEVERE, entry.getLevel());
		assertTrue(entry.getMessage().endsWith("Error log"));
		
		// 5. "console.info("Info log");"
		assertNotNull(it.hasNext());
		entry = it.next();
		System.out.println(entry);
		assertEquals(Level.INFO, entry.getLevel());
		assertTrue(entry.getMessage().endsWith("Info log"));
	}
}
